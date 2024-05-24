package com.example.algo_hits

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvException
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import android.os.Handler
import android.os.Looper

private val handler = Handler(Looper.getMainLooper())

class MainActivity : AppCompatActivity() {
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV is not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val m =
            Utils.loadResource(this@MainActivity, R.drawable.kotlin_image)
        var bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.RGB_565)
        Utils.matToBitmap(m, bm)
        originalBitmap = bm

        val imageView: ImageView = findViewById(R.id.image_output_1)
        imageView.setImageBitmap(originalBitmap)

        val scaleButton: Button = findViewById(R.id.scale_button)
        val inputScale: TextInputEditText = findViewById((R.id.input_scale))

        scaleButton.setOnClickListener {
            try {
                val scaleFactor = inputScale.text.toString().toDouble()
                val scaledBitmap = bicubicResize(
                    originalBitmap,
                    originalBitmap.width.toDouble() * scaleFactor,
                    originalBitmap.height.toDouble() * scaleFactor
                )
                imageView.setImageBitmap(scaledBitmap)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Enter the number of the scale.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun bicubicResize(bitmap: Bitmap, targetWidth: Double, targetHeight: Double): Bitmap? {
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

    private fun bicubicInterpolate(bitmap: Bitmap, x: Double, y: Double): Int {
        val x0 = Math.floor(x).toInt()
        val y0 = Math.floor(y).toInt()

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
        val at = Math.abs(t)
        return if (at <= 1) {
            (a + 2) * at.pow(3) - (a + 3) * at.pow(2) + 1
        } else if (at < 2) {
            a * at.pow(3) - 5 * a * at.pow(2) + 8 * a * at - 4 * a
        } else {
            0.0
        }
    }



//    fun bilinearResize(bitmap: Bitmap, targetWidth: Double, targetHeight: Double): Bitmap? {
//        val imgWidth = bitmap.width
//        val imgHeight = bitmap.height
//
//        val resizedBitmap = Bitmap.createBitmap(targetWidth.toInt(), targetHeight.toInt(), Bitmap.Config.ARGB_8888)
//
//        val xRatio:Double = if (targetWidth > 1) (imgWidth - 1).toFloat() / (targetWidth - 1) else 0f.toDouble()
//        val yRatio:Double = if (targetHeight > 1) (imgHeight - 1).toFloat() / (targetHeight - 1) else 0f.toDouble()
//
//        for (i in 0 until targetHeight.toInt()) {
//            for (j in 0 until targetWidth.toInt()) {
//
//                val xL = floor(j * xRatio).toInt()
//                val yL = floor(i * yRatio).toInt()
//                val xH = ceil(j * xRatio).toInt()
//                val yH = ceil(i * yRatio).toInt()
//
//                val xWeight = (j * xRatio) - xL.toFloat()
//                val yWeight = (i * yRatio) - yL.toFloat()
//
//                val a = getPixelColor(bitmap, yL, xL)
//                val b = getPixelColor(bitmap, yL, xH)
//                val c = getPixelColor(bitmap, yH, xL)
//                val d = getPixelColor(bitmap, yH, xH)
//
//                val pixel = (a * (1 - xWeight) * (1 - yWeight) +
//                        b * xWeight * (1 - yWeight) +
//                        c * yWeight * (1 - xWeight) +
//                        d * xWeight * yWeight).toInt()
//
//                setPixelColor(resizedBitmap, i, j, pixel)
//            }
//        }
//        Toast.makeText(this, "Original bitmap" + bitmap.width.toString() + " " + bitmap.height.toString(), Toast.LENGTH_LONG).show()
//        Toast.makeText(this, "Resized bitmap" + resizedBitmap.width.toString() + " " + resizedBitmap.height.toString(), Toast.LENGTH_LONG).show()
//        return resizedBitmap
//    }

    private fun getPixelColor(bitmap: Bitmap, y: Int, x: Int): Int {
        return if (y < 0 || y >= bitmap.height || x < 0 || x >= bitmap.width) Color.TRANSPARENT else bitmap.getPixel(x, y)
    }

    private fun setPixelColor(bitmap: Bitmap, y: Int, x: Int, color: Int) {
        bitmap.setPixel(x, y, color)
    }

}