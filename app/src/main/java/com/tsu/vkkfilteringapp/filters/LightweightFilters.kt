package com.tsu.vkkfilteringapp.filters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.exp
import kotlin.math.max

class LightweightFilters {
    fun illumination(img: Bitmap, degree: Double): Bitmap {
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        var imgArray = IntArray(img.width * img.height)
        var newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)
        runBlocking {
            (0 until img.height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in 0 until img.width) {
                            val iter = i * img.width + j

                            newImgArray[iter] =
                                Color.rgb(
                                    toRange(255, 0,(Color.red(imgArray[iter])*degree).toInt()),
                                    toRange(255, 0,(Color.green(imgArray[iter])*degree).toInt()),
                                    toRange(255, 0,(Color.blue(imgArray[iter])*degree).toInt())
                                )
                            //newImgArray[iter] = Color.rgb((int)(red), (int) (green), (int) (blue));
                        }
                        Log.i("unsharp", "i: " + i);
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage
    }

    private fun toRange(max: Int, min: Int, number: Int): Int {
        return if (number > max) max
        else max(number.toDouble(), min.toDouble()).toInt()
    }

    fun blackout(img: Bitmap, degree: Double): Bitmap {
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        var imgArray = IntArray(img.width * img.height)
        var newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)
        runBlocking {
            (0 until img.height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in 0 until img.width) {
                            val iter = i * img.width + j

                            newImgArray[iter] =
                                Color.rgb(
                                    toRange(255, 0,(Color.red(imgArray[iter])/degree).toInt()),
                                    toRange(255, 0,(Color.green(imgArray[iter])/degree).toInt()),
                                    toRange(255, 0,(Color.blue(imgArray[iter])/degree).toInt())
                                )

                        }
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage
    }

    fun colorEnhancement(img: Bitmap, degree: Double,color: Char): Bitmap {
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        var imgArray = IntArray(img.width * img.height)
        var newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)
        runBlocking {
            (0 until img.height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in 0 until img.width) {
                            val iter = i * img.width + j
                            when(color) {
                                'r' -> newImgArray[iter] =
                                    Color.rgb(
                                        toRange(
                                            255,
                                            0,
                                            (Color.red(imgArray[iter]) * (1 + degree)).toInt()
                                        ),
                                        toRange(255, 0, (Color.green(imgArray[iter]))),
                                        toRange(255, 0, (Color.blue(imgArray[iter])))
                                    )

                                'g' -> {
                                    Color.rgb(
                                        toRange(255, 0, (Color.red(imgArray[iter]))),
                                        toRange(
                                            255,
                                            0,
                                            (Color.green(imgArray[iter]) * (1 + degree)).toInt()
                                        ),
                                        toRange(255, 0, (Color.blue(imgArray[iter])))
                                    )
                                }

                                'b' -> {
                                    Color.rgb(
                                        toRange(255, 0, (Color.red(imgArray[iter]))),
                                        toRange(255, 0, (Color.green(imgArray[iter]))),
                                        toRange(
                                            255,
                                            0,
                                            (Color.green(imgArray[iter]) * (1 + degree)).toInt()
                                        )
                                    )
                                }
                            }

                        }
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage
    }

    fun contrast(img: Bitmap, degree: Int): Bitmap {
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        val imgArray = IntArray(img.width * img.height)
        val newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)
        val ratio = 259 * (degree + 255) /
                (255 * (259 - degree))
        var core = if(degree==1)getFirstContrastMatrix() else getSecondContrastMatrix()

        runBlocking {
            (1 until img.height-1).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in 1 until img.width-1) {
                            val iter = i * img.width + j
                            var red = 0
                            var green = 0
                            var blue = 0
                            for(filterX in i-1 until i+1) {
                                var localCoreX = 0
                                for (filterY in j - 1 until j + 1) {
                                    var localCoreY = 0
                                    var localIter = filterX * img.width + filterY
                                    red += Color.red(imgArray[localIter]*core[localCoreX][localCoreY])
                                    green += Color.green(imgArray[localIter]*core[localCoreX][localCoreY])
                                    blue += Color.blue(imgArray[localIter]*core[localCoreX][localCoreY])

                                    localCoreY++
                                }
                                localCoreX++
                            }
                            newImgArray[iter] =
                                Color.rgb(
                                    toRange(255, 0,red),
                                    toRange(255, 0,green),
                                    toRange(255, 0,blue)
                                )

                        }
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage
    }

    fun commonBlur(img: Bitmap,radius: Int, compromise: Double): Bitmap{
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        val imgArray = IntArray(img.width * img.height)
        val newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)

        runBlocking {
            (radius until img.height- radius).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in radius until  img.width- radius) {
                            val iter = i * img.width + j
                            var red = 0.0
                            var green = 0.0
                            var blue = 0.0
                            var sum = 0
                            for (coreI in -radius until radius) {
                                for (coreJ in -radius until radius) {
                                    val iCore = i + coreI
                                    val jCore = j + coreJ
                                    val coreIterator = iCore * img.width + jCore
                                    red += Color.red(imgArray[coreIterator])
                                    green += Color.green(imgArray[coreIterator])
                                    blue += Color.blue(imgArray[coreIterator])
                                    sum++
                                }
                            }
                            newImgArray[iter] =
                                Color.rgb(
                                    findCompromise((red / sum).toInt(),0,imgArray[iter],compromise),
                                    findCompromise((green / sum).toInt(),1,imgArray[iter],compromise),
                                    findCompromise((blue / sum).toInt(),2,imgArray[iter],compromise)
                                )
                        }
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage

    }

    fun mosaicFilter(img: Bitmap,radius: Int, compromise: Double): Bitmap {
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        val imgArray = IntArray(img.width * img.height)
        val newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray, 0, img.width, 0, 0, img.width, img.height)


        for (i in  radius until  img.height - radius step radius) {
            for (j in radius until img.width - radius step radius) {
                val iter = i * img.width + j
                val red = Color.red(imgArray[iter])
                val green = Color.green(imgArray[iter])
                val blue = Color.blue(imgArray[iter])

                for (coreI in -radius until radius) {
                    for (coreJ in -radius until radius) {
                        val iCore = i + coreI
                        val jCore = j + coreJ
                        val coreIterator = iCore * img.width + jCore
                        newImgArray[coreIterator] =
                            Color.rgb(
                                findCompromise(
                                    (red),
                                    0,
                                    imgArray[iter],
                                    compromise
                                ),
                                findCompromise(
                                    (green),
                                    1,
                                    imgArray[iter],
                                    compromise
                                ),
                                findCompromise(
                                    (blue),
                                    2,
                                    imgArray[iter],
                                    compromise
                                )
                            )
                    }
                }
            }
        }

        newImage.setPixels(newImgArray, 0, img.width, 0, 0, img.width, img.height)
        return newImage

    }

    fun inversion(img: Bitmap): Bitmap{
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        val imgArray = IntArray(img.width * img.height)
        val newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)

        runBlocking {
            (0 until img.height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in 0 until  img.width) {
                            val iter = i * img.width + j
                            val red = Color.red(imgArray[iter])
                            val green = Color.green(imgArray[iter])
                            val blue = Color.blue(imgArray[iter])
                            newImgArray[iter] =
                                Color.rgb(
                                    toRange(255,0,255 -red ),
                                    toRange(255,0,255 -green),
                                    toRange(255,0,255- blue )
                                )
                        }
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage

    }
    fun saturation(img: Bitmap,value: Int): Bitmap{
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        val imgArray = IntArray(img.width * img.height)
        val newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)

        runBlocking {
            (0 until img.height).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in 0 until  img.width) {
                            val iter = i * img.width + j
                            val red = Color.red(imgArray[iter])
                            val green = Color.green(imgArray[iter])
                            val blue = Color.blue(imgArray[iter])

                            val gray = (red * 0.2126 + green * 0.7152 + blue * 0.0722)

                            newImgArray[iter] =
                                Color.rgb(
                                    toRange(255,0,
                                        (red+(red-gray)*value/255).toInt()
                                    ),
                                    toRange(255,0,
                                        (green+(green-gray)*value/255).toInt()),
                                    toRange(255,0,
                                        (blue+(blue-gray)*value/255).toInt())
                                )
                        }
                    }
                }

            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage

    }
    fun blurGaussian(img: Bitmap,radius: Int, compromise: Double,sigma: Double): Bitmap {
        val newImage = img.copy(Bitmap.Config.ARGB_8888, true)
        val imgArray = IntArray(img.width * img.height)
        val newImgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray,0, img.width, 0, 0, img.width, img.height)

        val core = gaussianCore(radius, sigma)

        runBlocking {
            (radius until img.height - radius).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in radius until img.width - radius) {
                            val iter = i * img.width + j
                            var red = 0.0
                            var green = 0.0
                            var blue = 0.0

                            var localI = 0;
                            for(coreI in i-radius until i+radius){
                                var localJ = 0
                                for(coreJ in j - radius until j+radius){

                                    red+=Color.red((imgArray[iter]* core[localI][localJ]).toInt())
                                    green+=Color.green((imgArray[iter]* core[localI][localJ]).toInt())
                                    blue+=Color.blue((imgArray[iter]* core[localI][localJ]).toInt())
                                    localJ++
                                }
                                localI++
                            }

                            newImgArray[iter] = Color.rgb(
                                findCompromise((red  ).toInt(),0,imgArray[iter],compromise),
                                findCompromise((green).toInt(),1,imgArray[iter],compromise),
                                findCompromise((blue ).toInt(),2,imgArray[iter],compromise)
                            )
                        }
                    }
                }
            }.awaitAll()
        }

        newImage.setPixels(newImgArray,0, img.width, 0, 0, img.width, img.height)
        return  newImage
    }

    private fun gaussianCore(radius: Int, sigma: Double): ArrayList<ArrayList<Double>> {
        val core = ArrayList<ArrayList<Double>>()
        val matrixSize = 2 * radius + 1

        var sum = 0.0

        for (i in 0 until matrixSize) {
            core.add(ArrayList())
            for (j in 0 until matrixSize) {
                val matrixI = ((2 * i / (matrixSize - 1) - 1).toDouble())
                val matrixJ = ((2 * j / (matrixSize - 1) - 1).toDouble())
                core[i].add(gaussianFun(matrixI, matrixJ, sigma))
                sum += core[i][j]
            }
        }

        for (i in 0 until matrixSize) for (j in 0 until matrixSize) core[i][j] = core[i][j] / sum

        return core
    }
    private fun gaussianFun(x: Double, y: Double, sigma: Double): Double {
        val numerator = exp(-(x * x + y * y) / (2 * sigma * sigma))
        val denominator = 2 * Math.PI * sigma
        return numerator / denominator
    }

    private fun findCompromise(
        color: Int, type: Int,
        oldColor: Int, compromise: Double ): Int {

        return if (type == 0)
                (color * compromise + (1 - compromise) * Color.red(oldColor)).toInt()
        else if (type == 1)
                (color * compromise + (1 - compromise) * Color.green(oldColor)).toInt()
        else
                (color * compromise + (1 - compromise) * Color.blue(oldColor)).toInt()
    }

    private fun getFirstContrastMatrix(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(0,-1,0),
            arrayOf(-1,5,-1),
            arrayOf(0,-1,0)
        )
    }

    private fun getSecondContrastMatrix(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(-1 ,-1, -1),
            arrayOf(-1 , 9 , -1),
            arrayOf(-1 ,-1,  -1)
        )
    }

    private fun getFirstRobertsMask(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(-1 ,0),
            arrayOf(0 , 1),
        )
    }


    private fun getSecondRobertsMask(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(0 ,-1),
            arrayOf(1 , 0),
        )
    }
    private fun getFirstPrewittMatrix():Array<Array<Int>>{
        return arrayOf(
            arrayOf(-1 ,0, 1),
            arrayOf(-1 ,0, 1),
            arrayOf(-1 ,0, 1)
        )
    }

    private fun getSecondPrewittMatrxi():Array<Array<Int>>{
        return arrayOf(
            arrayOf(-1 ,-1, -1),
            arrayOf( 0 , 0,  0),
            arrayOf(-1 ,-1, -1)
        )
    }

    private fun getFirstSobelMatrix():Array<Array<Int>>{
        return arrayOf(
            arrayOf(-1 ,-2, -1),
            arrayOf(0 ,0, 0),
            arrayOf(-1 ,-2, -1)
        )
    }
    private fun getSecondSobelMatrix():Array<Array<Int>>{
        return arrayOf(
            arrayOf(-1 ,0, -1),
            arrayOf(-2 ,0, -2),
            arrayOf(-1 ,0, -1)
        )
    }

}