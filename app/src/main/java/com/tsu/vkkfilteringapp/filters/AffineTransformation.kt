package com.tsu.vkkfilteringapp.filters

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.graphics2d.Point2D
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D
import com.tsu.vkkfilteringapp.matrices.Matrix3x3
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class AffineTransformation {

    fun transformBitmapByTriangles(bitmap: Bitmap, taskViewModel: TaskViewModel) : Bitmap {

        val transformationMatrix = getTransformationMatrixByTriangles(taskViewModel.affineToolOrigTriangle, taskViewModel.affineToolTransTriangle)
        transformationMatrix.rows[0][2] = 0F
        transformationMatrix.rows[1][2] = 0F

        return transformBitmapByMatrix(bitmap, transformationMatrix, taskViewModel)
    }

    private fun transformBitmapByMatrix(bitmap: Bitmap, transformationMatrix: Matrix3x3, taskViewModel: TaskViewModel, scaling: Boolean = false) : Bitmap {

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
        if (newBitmapSize < 4000000 || scaling) {

            var currentPoint: Point2D

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
                    currentPoint = Point2D(xi.toFloat() + topLeftCorner.x, yi.toFloat() + topLeftCorner.y)
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

            taskViewModel.affineToolCroppedImageBorders = getResultingImageBorders(bitmap, newBitmap)

            return newBitmap
        }
        return bitmap
    }

    private fun getResultingImageBorders(origBitmap: Bitmap, newBitmap: Bitmap) : List<Float> {

        val center = Point2D(newBitmap.width / 2F, newBitmap.height / 2F)
        val corners = listOf(center.copy(), center.copy(), center.copy(), center.copy())
        val delta = origBitmap.height.toFloat() / origBitmap.width.toFloat()

        var inBounds = true
        while (inBounds && corners[0].x > 0 && corners[0].y > 0) {
            for (ci in 0..3) {
                if (newBitmap.getPixel(corners[ci].x.toInt(), corners[ci].y.toInt()).alpha == 0) {
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

        return listOf(
            min(abs(corners[0].x), abs(corners[3].x)) + 1F,
            max(abs(corners[0].x), abs(corners[3].x)) - 1F,
            min(abs(corners[0].y), abs(corners[3].y)) + 1F,
            max(abs(corners[0].y), abs(corners[3].y)) - 1F)

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

        return getLinearInterpolatedColor(topPixel, bottomPixel, point.y - floor(point.y))
    }

    private fun getLinearInterpolatedColor(color1: Int, color2: Int, interpolation: Float) : Int {

        return Color.argb((color1.alpha + (color2.alpha - color1.alpha) * interpolation).toInt(),
            (color1.red + (color2.red - color1.red) * interpolation).toInt(),
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

    fun getTransformationMatrixByTriangles(origTriangle: Triangle2D, transTriangle: Triangle2D) : Matrix3x3 {
        return matrixMultiply(Matrix3x3(transTriangle), Matrix3x3(origTriangle).getInvertedMatrix())
    }

    fun getCropDemo(bitmap: Bitmap, taskViewModel: TaskViewModel) : Bitmap {

        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        var currentPixel: Color
        for (yi in 0..<newBitmap.height) {
            for (xi in 0..<newBitmap.width) {
                if (!(yi > taskViewModel.affineToolCroppedImageBorders[2] && yi < taskViewModel.affineToolCroppedImageBorders[3] &&
                    xi > taskViewModel.affineToolCroppedImageBorders[0] && xi < taskViewModel.affineToolCroppedImageBorders[1])) {

                    currentPixel = Color.valueOf(bitmap.getPixel(xi, yi))
                    newBitmap.setPixel(xi, yi, Color.argb(0.5F, currentPixel.red() / 5, currentPixel.green() / 5, currentPixel.blue() / 5))
                }
            }
        }

        return newBitmap
    }

    fun getCroppedImage(bitmap: Bitmap, taskViewModel: TaskViewModel) : Bitmap {

        val newBitmap = Bitmap.createBitmap((taskViewModel.affineToolCroppedImageBorders[1] - taskViewModel.affineToolCroppedImageBorders[0]).toInt(),
            (taskViewModel.affineToolCroppedImageBorders[3] - taskViewModel.affineToolCroppedImageBorders[2]).toInt(), Bitmap.Config.ARGB_8888)

        for (yi in 0..<newBitmap.height) {
            for (xi in 0..<newBitmap.width) {
                newBitmap.setPixel(xi, yi, bitmap.getPixel(
                    (taskViewModel.affineToolCroppedImageBorders[0] + xi).toInt(),
                    (taskViewModel.affineToolCroppedImageBorders[2] + yi).toInt()
                ))
            }
        }

        return newBitmap
    }

    fun rotateImage(bitmap: Bitmap, rotationAngle: Float, taskViewModel: TaskViewModel) : Bitmap {

        val rotationMatrix = Matrix3x3(mutableListOf(
            mutableListOf(cos(rotationAngle), -sin(rotationAngle), 0F),
            mutableListOf(sin(rotationAngle), cos(rotationAngle), 0F),
            mutableListOf(0F, 0F, 1F)
        ))

        return transformBitmapByMatrix(bitmap, rotationMatrix, taskViewModel)
    }

    fun scaleImage(bitmap: Bitmap, scale: Float, taskViewModel: TaskViewModel) : Bitmap {

        val scaleMatrix = Matrix3x3()
        scaleMatrix.rows[0][0] = scale
        scaleMatrix.rows[1][1] = scale
        scaleMatrix.rows[2][2] = 1F

        return transformBitmapByMatrix(bitmap, scaleMatrix, taskViewModel, true)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AffineTransformation()
    }
}