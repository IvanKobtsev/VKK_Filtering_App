package com.tsu.vkkfilteringapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.util.Log
import kotlin.math.exp

class GaussianBlur(img: Bitmap, sigma: Double, private val radius: Int) {
    private var sigma = 0.0
    private var img: Bitmap? = img
    private var blurImg: Bitmap? = null

    init {
        this.sigma = 2 * sigma * sigma
        blurImg = img.copy(Bitmap.Config.ARGB_8888, true)
        blur()
    }

    private fun blur() {
        val matrixWidth = 2 * radius + 1
        val matrix = Array(matrixWidth) {
            DoubleArray(
                matrixWidth
            )
        }
        var sum = 0.0
        //take matrixKernel;
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                val eExpression = exp(-(i * i + j * j)/sigma)
                val matrixValue = eExpression / (sigma * Math.PI)

                matrix[i + radius][j + radius] = matrixValue
                sum += matrixValue
            }
        }
        Log.d("array", "height: " + toString(matrix))
        //matrix normalization
        for (i in 0 until matrixWidth) {
            for (j in 0 until matrixWidth) {
                matrix[i][j] /= sum
            }
        }
        Log.d("array", "height: $sum")
        Log.d("array", "height: " + toString(matrix))

        for (i in radius until (img!!.getWidth() - radius)) {

            for (j in radius until (img!!.getHeight() - radius)) {
                var red = 0.0
                var green = 0.0
                var blue = 0.0

                for (x in -radius..radius) {
                    for (y in -radius..radius) {
                        val matrixValue = matrix[x + radius][y + radius]

                        red += red(img!!.getPixel(i + x, + j + y)) * matrixValue
                        green += green(img!!.getPixel(i + x, + j + y)) * matrixValue
                        blue += blue(img!!.getPixel(i + x, + j + y)) * matrixValue
                    }
                }

                blurImg!!.setPixel(i, j, Color.rgb(red.toInt(), green.toInt(),blue.toInt() ))

            }
        }
    }

    private fun toString(matrix: Array<DoubleArray>): String {
        var string = "["
        for(i in 0 until  matrix.size) {
            string+="["
            for (j in 0 until matrix[i].size) {
                string+= matrix[i][j].toString() + " "
            }
            string+="]"
        }
        string+="]"
        return string
    }


    private fun getSigma(): Double {
        return sigma
    }

    private fun setSigma(sigma: Float) {
        this.sigma = sigma.toDouble()
    }

    fun setImg(img: Bitmap?) {
        this.img = img
        blur()
    }

    fun toARGB(pixel: Int): IntArray {
        val argb = IntArray(3)
        argb[2] = red(pixel)
        argb[1] = green(pixel)
        argb[0] = blue(pixel)
        argb[0] = blue(pixel)
        return argb
    }

    fun BitmapToByteRgbNaive(bmp: Bitmap?): ArrayList<ArrayList<ArrayList<Int>>> {
        val width = bmp!!.getWidth()
        val height = bmp.getHeight()
        val res = ArrayList<ArrayList<ArrayList<Int>>>(height)
        for (y in 0 until height) {
            res.add(ArrayList(width))
            for (x in 0 until width) {
                res[y].add(ArrayList(3))

            }
            Log.d("array", ""+y)
        }
        Log.d("array", "139")

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = toARGB(bmp.getPixel(x, y))
                res[y][x].add(color[0])
                res[y][x].add(color[1])
                res[y][x].add(color[2])

            }
            Log.d("array", ""+y)
        }
        Log.d("array", "139")
        return res
    }

    fun getImg(): Bitmap? {
        return blurImg
    }
}