package com.tsu.vkkfilteringapp.filters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.tsu.vkkfilteringapp.fragments.RotationToolFragment
import org.opencv.core.Mat
import org.opencv.core.Point
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin

class RotationFilter {

    private fun bicubicInterpolate(bitmap: Bitmap, x: Double, y: Double): Int {
        val x0 = floor(x).toInt()
        val y0 = floor(y).toInt()

        val u = x - x0
        val v = y - y0

        var sumRed = 0.0
        var sumGreen = 0.0
        var sumBlue = 0.0

        for (i in -1..2) {
            for (j in -1..2) {
                val cx = cubicWeight(u - i)
                val cy = cubicWeight(v - j)

                val color = getPixelColor(bitmap, y0 + j, x0 + i)
                val red = Color.red(color)
                val green = Color.green(color)
                val blue = Color.blue(color)

                sumRed += red * cx * cy
                sumGreen += green * cx * cy
                sumBlue += blue * cx * cy
            }
        }

        return Color.rgb(sumRed.toInt(), sumGreen.toInt(), sumBlue.toInt())
    }

    private fun cubicWeight(t: Double): Double {
        val a = -0.5
        val at = abs(t)
        return if (at <= 1) {
            (a + 2) * at.pow(3) - (a + 3) * at.pow(2) + 1
        } else if (at < 2) {
            a * at.pow(3) - 5 * a * at.pow(2) + 8 * a * at - 4 * a
        } else {
            0.0
        }
    }

    private fun bicubicResize(bitmap: Bitmap, targetWidth: Double, targetHeight: Double): Bitmap {
        val imgWidth = bitmap.width
        val imgHeight = bitmap.height

        val resizedBitmap = Bitmap.createBitmap(targetWidth.toInt(), targetHeight.toInt(), Bitmap.Config.ARGB_8888)

        val scaleX = imgWidth.toDouble() / targetWidth
        val scaleY = imgHeight.toDouble() / targetHeight

        for (i in 0 until targetHeight.toInt()) {
            for (j in 0 until targetWidth.toInt()) {
                val x = j * scaleX
                val y = i * scaleY
                val color = bicubicInterpolate(bitmap, x, y)
                setPixelColor(resizedBitmap, i, j, color)
            }
        }

        return resizedBitmap
    }

    private fun getPixelColor(bitmap: Bitmap, y: Int, x: Int): Int {
        return if (y < 0 || y >= bitmap.height || x < 0 || x >= bitmap.width) Color.TRANSPARENT else bitmap.getPixel(x, y)
    }

    private fun setPixelColor(bitmap: Bitmap, y: Int, x: Int, color: Int) {
        bitmap.setPixel(x, y, color)
    }

    fun rotateImage(
        bm: Bitmap,
        angle: Double,
        point: Point,
        scale: Double,
        parameter: Double,
    ): Bitmap {

        val scaleX = scale.toFloat()
        val scaleY = scale.toFloat()

        val radians = Math.toRadians(angle)
        val sinValue = sin(radians).toFloat()
        val cosValue = cos(radians).toFloat()

        val rotatedWidth = (bm.width)
        val rotatedHeight = (bm.height)

        val bm2 = bicubicResize(bm, rotatedWidth / parameter.pow(1), rotatedWidth / parameter.pow(1))
        val rotatedBitmap = Bitmap.createBitmap((bm.width), (bm.height), bm2.config)
        var xStart = 0
        var xEnd = rotatedBitmap.width
        var yStart = 0
        var yEnd = rotatedBitmap.height
        if (parameter != 1.0) {
            xStart += 100
            yStart += 100
            xEnd -= 100
            yEnd -= 100
        }
        for (x in 0 until rotatedWidth) {
            for (y in 0 until rotatedHeight) {
                val newX = (cosValue * (x - xStart - point.x) + sinValue * (y - yStart - point.y) / scaleX + point.x).toInt()
                val newY = (sinValue * (x - xStart - point.x) - cosValue * (y - yStart - point.y) / scaleY + point.y).toInt()

                // Проверяю границы в исх. пространстве
                if (x >= xStart - 50 && y >= yStart - 50 && newX >= 0 && newX < bm2.width && newY >= 0 && newY < bm2.height) {
                    val pixel = bm2.getPixel(newX, newY)
                    rotatedBitmap.setPixel(x, y, pixel)
                }
            }
        }

        return rotatedBitmap
    }

    companion object {
        @JvmStatic
        fun newInstance() = RotationFilter()
    }

}