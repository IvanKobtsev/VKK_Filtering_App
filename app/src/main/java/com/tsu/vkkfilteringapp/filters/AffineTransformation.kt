package com.tsu.vkkfilteringapp.filters

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.set
import com.tsu.vkkfilteringapp.graphics2d.Point2D
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D
import com.tsu.vkkfilteringapp.matrices.Matrix3x3
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AffineTransformation {

    fun transformBitmapByTriangles(bitmap: Bitmap, origTriangle: Triangle2D, transTriangle: Triangle2D) : Bitmap {

        val transformationMatrix = getTranformationMatrixByTriangles(origTriangle, transTriangle)
        transformationMatrix.rows[0][2] = 0F
        transformationMatrix.rows[1][2] = 0F

        val affineMatrix = transformationMatrix.getInvertedMatrix()

        val points = listOf(
            Point2D(),
            Point2D(bitmap.width.toFloat(), 0F),
            Point2D(0F, bitmap.height.toFloat()),
            Point2D(bitmap.width.toFloat(), bitmap.height.toFloat()))

        for (pi in 0..3) {
            points[pi].transformByMatrix(transformationMatrix)
        }

        val xSortedPoints = points.sortedBy { it.x }
        val ySortedPoints = points.sortedBy { it.y }

        val topLeftCorner = Point2D(xSortedPoints[0].x, ySortedPoints[0].y)
        val bottomRightCorner = Point2D(xSortedPoints[3].x, ySortedPoints[3].y)

        val newBitmapWidth = bottomRightCorner.x - topLeftCorner.x
        val newBitmapHeight = bottomRightCorner.y - topLeftCorner.y

        val newBitmapSize = newBitmapWidth * newBitmapHeight
        val origBitmapSize = bitmap.width * bitmap.height

        var newBitmap: Bitmap
        if (newBitmapSize < 4000000) {

            var currentPoint: Point2D
            var transparency: Int

            if (newBitmapSize < origBitmapSize) {
                // Use trilinear
                var sizeDifference = sqrt(origBitmapSize / newBitmapSize).roundToInt()
                var bilinearCount = 0
                while (sizeDifference > 2) {
                    ++bilinearCount
                    sizeDifference /= 2
                }

                val scalingMatrix = Matrix3x3(mutableListOf(
                    mutableListOf(affineMatrix.rows[0][0], 0F, 0F),
                    mutableListOf(0F, affineMatrix.rows[1][1], 0F),
                    mutableListOf(0F, 0F, 1F)))

                for (i in 0..<bilinearCount) {
                    scalingMatrix.rows[0][0] = sqrt(scalingMatrix.rows[0][0])
                    scalingMatrix.rows[1][1] = sqrt(scalingMatrix.rows[1][1])
                }

                var tempBitmap: Bitmap
                newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                var tempBitmapWidth = newBitmap.width.toFloat()
                var tempBitmapHeight = newBitmap.height.toFloat()

                for (i in 0..<bilinearCount - 1) {

                    tempBitmapWidth /= scalingMatrix.rows[0][0]
                    tempBitmapHeight /= scalingMatrix.rows[1][1]

                    tempBitmap = Bitmap.createBitmap(tempBitmapWidth.toInt(), tempBitmapHeight.toInt(), Bitmap.Config.ARGB_8888)

                    for (yi in 1..<tempBitmapHeight.toInt() - 1) {
                        for (xi in 1..<tempBitmapWidth.toInt() - 1) {

                            currentPoint = Point2D(xi.toFloat(), yi.toFloat()).getTransformedPoint(scalingMatrix)

                            tempBitmap.setPixel(xi, yi, getPixelWithBilinearFiltering(newBitmap, currentPoint))
                        }
                    }

                    newBitmap = tempBitmap
                }
            }

            newBitmap = Bitmap.createBitmap(newBitmapWidth.toInt(), newBitmapHeight.toInt(), Bitmap.Config.ARGB_8888)

            // Use bilinear
            for (yi in 0..<newBitmapHeight.toInt()) {
                for (xi in 0..<newBitmapWidth.toInt()) {
                    currentPoint = Point2D(xi.toFloat(), yi.toFloat())
                    currentPoint.translate(topLeftCorner.x, topLeftCorner.y)
//                    transparency = if (currentPoint.x > resultingImageBorders[0] && currentPoint.x < resultingImageBorders[1]
//                        && currentPoint.y > resultingImageBorders[2] && currentPoint.y < resultingImageBorders[3]) {
//                        255
//                    } else {
//                        120
//                    }

                    currentPoint.transformByMatrix(affineMatrix)

                    if (currentPoint.x > 0 && currentPoint.x < bitmap.width - 1
                        && currentPoint.y > 0 && currentPoint.y < bitmap.height - 1) {
                        newBitmap.setPixel(xi , yi, getPixelWithBilinearFiltering(bitmap, currentPoint))
                    }
                    else {
                        newBitmap.setPixel(xi, yi, Color.TRANSPARENT)
                    }
                }
            }

            return newBitmap
        }
        return bitmap
    }

    // val resultingImageBorders = getResultingImageBorders(newBitmapWidth, newBitmapHeight, bitmap.width.toFloat(), bitmap.height.toFloat(), affineMatrix, topLeftCorner)
    private fun getResultingImageBorders(newWidth: Float, newHeight: Float, origWidth: Float, origHeight: Float, transformation: Matrix3x3, topLeftCorner: Point2D) : List<Float> {

        val center = Point2D(newWidth / 2, newHeight / 2)
        val corners = listOf(center.copy(), center.copy(), center.copy(), center.copy())

        for (ci in 0..3) {
            corners[ci].translate(topLeftCorner.x, topLeftCorner.y)
        }

        val transformedCorners = mutableListOf(center, center, center, center)
        val delta = origHeight / origWidth

        var inBounds = true
        while (inBounds) {
            for (ci in 0..3) {
                transformedCorners[ci] = corners[ci].getTransformedPoint(transformation)
                if (!(transformedCorners[ci].x > 0 && transformedCorners[ci].x < origWidth
                    && transformedCorners[ci].y > 0 && transformedCorners[ci].y < origHeight)) {
                    inBounds = false
                }
            }

            corners[0].x--
            corners[1].x--
            corners[2].x++
            corners[3].x++
            corners[0].y -= delta
            corners[1].y += delta
            corners[2].y -= delta
            corners[3].y += delta
        }

        return listOf(corners[0].x, corners[3].x, corners[0].y, corners[3].y)

    }

    private fun getPixelWithBilinearFiltering(bitmap: Bitmap, point: Point2D) : Int {

        // top linear
        val topLeftColor = bitmap.getPixel(point.x.toInt(), point.y.toInt())
        val topRightColor = bitmap.getPixel(point.x.toInt() + 1, point.y.toInt())
        val topPixel = getLinearInterpolatedColor(topLeftColor, topRightColor, point.x - floor(point.x))

        // bottom linear
        val bottomLeftColor = bitmap.getPixel(point.x.toInt(), point.y.toInt() + 1)
        val bottomRightColor = bitmap.getPixel(point.x.toInt() + 1, point.y.toInt() + 1)
        val bottomPixel = getLinearInterpolatedColor(bottomLeftColor, bottomRightColor, point.x - floor(point.x))

        Log.i("color", "$bottomLeftColor $bottomRightColor: $bottomPixel")

        return getLinearInterpolatedColor(topPixel, bottomPixel, point.y - floor(point.y))
    }

    private fun getLinearInterpolatedColor(color1: Int, color2: Int, interpolation: Float) : Int {

        return Color.rgb((color1.red + (color2.red - color1.red) * interpolation).toInt(),
            (color1.green + (color2.green - color1.green) * interpolation).toInt(),
            (color1.blue + (color2.blue - color1.blue) * interpolation).toInt())
    }

    fun clamp(value: Int, min: Int, max: Int) : Int {
        return max(min(value, max), min)
    }

    fun clamp(value: Float, min: Float, max: Float) : Float {
        return max(min(value, max), min)
    }

    private fun matrixMultiply(firstMatrix: Matrix3x3, secondMatrix: Matrix3x3) : Matrix3x3 {

        val resultMatrix = Matrix3x3()

        for (yi in 0..2) {
            for (xi in 0..2) {
                for (i in 0..2) {
                    resultMatrix.rows[yi][xi] += firstMatrix.rows[yi][i] * secondMatrix.rows[i][xi]
                }
            }
        }

        return resultMatrix
    }

    fun getTranformationMatrixByTriangles(origTriangle: Triangle2D, transTriangle: Triangle2D) : Matrix3x3 {
        return matrixMultiply(Matrix3x3(transTriangle), Matrix3x3(origTriangle).getInvertedMatrix())
    }

    companion object {
        @JvmStatic
        fun newInstance() = AffineTransformation()
    }
}