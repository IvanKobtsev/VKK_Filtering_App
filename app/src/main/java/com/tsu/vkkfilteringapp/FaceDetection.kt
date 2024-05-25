package com.tsu.vkkfilteringapp

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import pl.droidsonroids.gif.InputSource.AssetSource
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.coroutineContext


class FaceDetection {

    fun convertToGrayScale(input: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY)
        return gray
    }

    private lateinit var cascadeClassifier: CascadeClassifier

    fun detectFaces(input: Bitmap, resources: Resources, context: Context): Bitmap {

        val imageMat = Mat(input.getWidth(), input.getHeight(), CvType.CV_8UC4)
        val myBitmap32: Bitmap = input.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(myBitmap32, imageMat)

        getCascadeClassifier(resources, context)
        Log.e("blya", cascadeClassifier.empty().toString())

        val faces = MatOfRect()
        cascadeClassifier.detectMultiScale(imageMat, faces)

        val bitmapToReturn = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imageMat, bitmapToReturn)

        for (rect in faces.toArray()) {
            for (yi in 0..<rect.height) {
                for (xi in 0..<rect.width) {
                    bitmapToReturn.setPixel(rect.x + xi, rect.y + yi, Color.argb(0F, 0F, 0F, 0F))
                }
            }
        }

        return bitmapToReturn
    }

    private fun getCascadeClassifier(resources: Resources, context: Context) {
        try {
            // Load the cascade classifier file from resources
            val inputStream: InputStream = resources.openRawResource(R.raw.face_front_cascade)
            val cascadeDir = context.getDir("cascade", MODE_PRIVATE)
            val cascadeFile = File(cascadeDir, "face_front_cascade.xml")
            val outputStream = FileOutputStream(cascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()

            // Load the cascade classifier
            cascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)

            // Check if the classifier is loaded correctly
            if (cascadeClassifier.empty()) {
                Log.e("MainActivity", "Failed to load cascade classifier")
            } else {
                Log.d("MainActivity", "Cascade classifier loaded successfully")
            }

            // Clean up
            cascadeDir.delete()

        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading cascade classifier", e)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FaceDetection()
    }
}