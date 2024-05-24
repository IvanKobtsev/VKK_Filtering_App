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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvException
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.*
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

class MainActivity : AppCompatActivity() {
    @SuppressLint("CutPasteId", "MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV is not found.", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "OpenCV is SUCCESSFULLY found!", Toast.LENGTH_SHORT).show()
        }

        val folder = File("/")
        if (!folder.exists()) {
            Toast.makeText(this, "OpenCV is not found.", Toast.LENGTH_SHORT).show()
        }




        val m =
            Utils.loadResource(this@MainActivity, R.drawable.kotlin_image)

        var bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.RGB_565)
        Utils.matToBitmap(m, bm)
        val imageViewWidth = m.width()
        val imageViewHeight = m.height()


        val ivOutput: ImageView = findViewById(R.id.image_output_1)
        val leftBtn: ImageButton = findViewById(R.id.left_rotation_button)
        val rightBtn: ImageButton = findViewById(R.id.right_rotation_button)


        var mainAngle = 0.0
        var checkAngleRight45 = 0.0
        var checkAngleLeft45 = 0.0
        var scaledParameter = 1.0
        var currButton = 0
        var resizedRotatedImage = rotateImage(bm, mainAngle, Point(bm.width / 2 / 2.0, bm.height / 2 / 2.0), 1.0, 2.0,  m, this)
        ivOutput.setImageBitmap(resizedRotatedImage)

        leftBtn.setOnClickListener {

            mainAngle -= 5.0
            if (mainAngle <= 0.0)
                mainAngle = 360.0

            resizedRotatedImage = rotateImage(bm, mainAngle, Point(bm.width / 2 / 2.0, bm.height / 2 / 2.0), scaledParameter, 2.0, m, this)
            ivOutput.setImageBitmap(resizedRotatedImage)
        }

        rightBtn.setOnClickListener {
            if (mainAngle >= 360.0)
                mainAngle = 0.0
            mainAngle += 5.0

            resizedRotatedImage = rotateImage(bm, mainAngle, Point(bm.width / 2 / 2.0, bm.height / 2 / 2.0), scaledParameter, 2.0, m, this)
            ivOutput.setImageBitmap(resizedRotatedImage)
        }

    }
    fun bicubicResize(bitmap: Bitmap, targetWidth: Double, targetHeight: Double): Bitmap {
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

    private fun rotateImage(
        bm: Bitmap,
        angle: Double,
        point: Point,
        scale: Double,
        parameter: Double,
        mat: Mat,
        context: Context,
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

//    Utils.bitmapToMat(rotatedBitmap, mat)
//    val targetSize = Size(bm.width.toDouble() * 0.9, bm.height.toDouble() * 0.9)
//    mat.resize(targetSize, Imgproc.INTER_LINEAR)
//    val mat1 = Mat() // Assuming this is your input Mat object
//    val targetSize = Size(100.0, 100.0) // Target size for the resize operation
//    mat1.resize(targetSize, Imgproc.INTER_LINEAR)


        return rotatedBitmap

    }
}




//fun resizeImage(inputImage: Mat, context: Context): Mat {
//    // Проверяем, что изображение не пустое
//    if (inputImage.empty()) {
//        return Mat()
//    }
//
//    // Вычисляем новые размеры, уменьшая текущие размеры на 50 пикселей
//    val newWidth = inputImage.cols() - 250
//    val newHeight = inputImage.rows() - 250
//    try{
//        // Создаем новый объект Mat с новыми размерами
//        val resizedImage = Mat(newHeight, newWidth, inputImage.type())
//        // Используем метод resize для изменения размера изображения
//        Imgproc.resize(inputImage, resizedImage, Size(newWidth.toDouble(), newHeight.toDouble()))
//        Toast.makeText(context, "dhjds: " + resizedImage.width().toString() + " " + resizedImage.height().toString(), Toast.LENGTH_SHORT).show()
//        return resizedImage
//    }
//    catch (e: CvException) {
//        Toast.makeText(context, "dhjds: " + e, Toast.LENGTH_SHORT).show()
////        Log.d("Exception", e.message!!)
//    }
//    return inputImage
//}



