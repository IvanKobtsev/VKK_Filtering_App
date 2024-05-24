package com.tsu.vkkfilteringapp.filters

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max

class UnsharpMasking {
    private var amount: Double? = null
    private var img: Bitmap? = null
    private lateinit var imgArray: IntArray
    private lateinit var redArray: IntArray

    private lateinit var greenArray: IntArray
    private lateinit var blueArray: IntArray
    private lateinit var newImgArray: IntArray
    private var newImg: Bitmap? = null
    private val threads: List<Thread>? = null
    private val countCores = CheckCores().numberOfCores - 1
    private var from = 0
    private val finish = BooleanArray(countCores)
    private var r = 0
    private var num = 0
    private var to = 0
    fun processImage(img: Bitmap, amount: Double?, r: Int): Bitmap {
        this.amount = amount
        this.r = r
        this.img = img
        this.to = (img.height - 2) / countCores
        this.from = 1
        imgArray = IntArray(img.width * img.height)
        img.getPixels(imgArray, 0, img.width, 0, 0, img.width, img.height)
        redArray = IntArray(img.width * img.height)
        greenArray = IntArray(img.width * img.height)
        blueArray = IntArray(img.width * img.height)
        convertedColor()
        newImgArray = imgArray.clone()

        blur2()
        unsharp()

        return getNewImg()
    }

    private fun check() {
        while (true) {
            var work = false
            for (i in threads!!.indices) {
                if (finish[i]) work = true
            }
            if (!work) {
                for (i in threads.indices) {
                    try {
                        threads[i].join()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                }
                return
            }
        }
    }

    private fun convertedColor() {
        for (i in imgArray.indices) {
            redArray[i] = (imgArray[i] shr 16) and 0xff
            greenArray[i] = (imgArray[i] shr 8) and 0xff
            blueArray[i] = (imgArray[i]) and 0xff
        }
    }

    private fun convertedColorLong() {
        for (i in imgArray.indices) {
            redArray[i] = Color.red(imgArray[i])
            greenArray[i] = Color.green(imgArray[i])
            blueArray[i] = Color.blue(imgArray[i])
        }
    }

    private fun createThreads(): List<Thread> {
        val threads: MutableList<Thread> = ArrayList()

        for (i in 0 until countCores) {
            threads.add(Thread { blur() })
            threads[i].start()

            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            to += (img!!.height - 2) / countCores
            from += (img!!.height - 2) / countCores
            num++

            Log.i("unsharp", "" + num)
        }
        return threads
    }

    fun blur() {
        val core = gaussianCore(r, -0.6)
        Log.i("unsharp", img!!.height.toString() + " : " + img!!.width)

        runBlocking {
            (0 until img!!.height - r).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in r until img!!.width - r) {
                            val iter = i * img!!.width + j
                            var red = 0.0
                            var green = 0.0
                            var blue = 0.0
                            val sum = 0
                            var coreI = i - r
                            var iCore = 0
                            while (coreI < i + r) {
                                var coreJ = j - r
                                var jCore = 0
                                while (coreJ < j + r) {
                                    //Log.d("unsharp","coreI: "+ coreI);
                                    //Log.d("unsharp","coreJ: "+ coreJ);
                                    val coreIter = coreI * img!!.width + coreJ
                                    red += core[iCore][jCore] * redArray[coreIter]
                                    green += core[iCore][jCore] * greenArray[coreIter]
                                    blue += core[iCore][jCore] * blueArray[coreIter]
                                    coreJ++
                                    jCore++
                                }
                                coreI++
                                iCore++
                            }

                            newImgArray[iter] = Color.rgb(
                                red.toInt(), green.toInt(), blue.toInt()
                            )
                        }
                    }
                }
            }.awaitAll()
        }

    }

    private fun blur2() {

        runBlocking {
            (r until img!!.height- r).chunked(4).map { rows ->
                async(Dispatchers.Default) {
                    for (i in rows) {
                        for (j in r until  img!!.width- r) {
                            val iter = i * img!!.width + j
                            var red = 0.0
                            var green = 0.0
                            var blue = 0.0
                            var sum = 0
                            for (coreI in -r until r) {
                                for (coreJ in -r until r) {
                                    val iCore = i + coreI
                                    val jCore = j + coreJ
                                    val coreIterator = iCore * img!!.width + jCore
                                    red += redArray[coreIterator].toDouble()
                                    green += greenArray[coreIterator].toDouble()
                                    blue += blueArray[coreIterator].toDouble()
                                    sum++
                                }
                            }
                            newImgArray[iter] =
                                Color.rgb(
                                    (red / sum).toInt(),
                                    (green / sum).toInt(),
                                    (blue / sum).toInt()
                                )
                        }
                    }
                }

            }.awaitAll()
        }



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
                //                Double matrixI =  i-radius;
                //              Double matrixJ =  j-radius;
                core[i.toInt()].add(gaussianFun(matrixI, matrixJ, sigma))
                sum += core[i.toInt()][j.toInt()]
            }
        }

        for (i in 0 until matrixSize) for (j in 0 until matrixSize) core[i][j] = core[i][j] / sum
        var s = 0.0
        for (i in 0 until matrixSize) for (j in 0 until matrixSize) {
            s += core[i][j]
            Log.e("unsharp", i.toString() + ", " + j + ": " + core[i][j])
        }
        Log.e("unsharp", "" + s)
        return core
    }

    private fun gaussianFun(x: Double, y: Double, sigma: Double): Double {
        val numerator = exp(-(x * x + y * y) / (2 * sigma * sigma))
        val denominator = 2 * Math.PI * sigma
        return numerator / denominator
    }

    private fun unsharp() {

        for (i in 0 until img!!.height) {
            for (j in 0 until img!!.width) {
                val original = imgArray[i * img!!.width + j]
                val blur = newImgArray[i * img!!.width + j]
                newImgArray[i * img!!.width + j] = Color.rgb(
                    toRange(
                        255, 0,
                        abs(
                            (Color.red(original) + amount!! * (Color.red(original) - Color.red(blur))).toInt()
                                .toDouble()
                        )
                            .toInt()
                    ),
                    toRange(
                        255, 0,
                        abs(
                            (Color.green(original) + amount!! * (Color.green(original) - Color.green(
                                blur
                            ))).toInt()
                                .toDouble()
                        )
                            .toInt()
                    ),
                    toRange(
                        255, 0,
                        abs(
                            (Color.blue(original) + amount!! * (Color.blue(original) - Color.blue(
                                blur
                            ))).toInt()
                                .toDouble()
                        )
                            .toInt()
                    )
                )
//
            }
        }
    }

    private fun toRange(max: Int, min: Int, number: Int): Int {
        return if (number > max) max
        else max(number.toDouble(), min.toDouble()).toInt()
    }

    fun getNewImg(): Bitmap {
        Log.i("unsharp", "fuck")
        newImg = Bitmap.createBitmap(img!!.width, img!!.height, Bitmap.Config.ARGB_8888)
        newImg!!.setPixels(newImgArray, 0, img!!.width, 0, 0, img!!.width, img!!.height)
        return newImg as Bitmap
    }

}