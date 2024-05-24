package com.tsu.vkkfilteringapp

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D
import pl.droidsonroids.gif.InputSource.AssetSource
import java.lang.reflect.Type

class ImageViewMotionOverride(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    private lateinit var taskViewModel: TaskViewModel
    private val paint = Paint()
    private val basePaint = Paint()
    private val letters = listOf("A", "B", "C")
    private val origTriangleColor = Color.argb(0.7F, 1F, 0F, 0F)
    private val transTriangleColor = Color.argb(0.7F, 0F, 0F, 1F)
    private val vertexRadius = 120F

    fun init(view: ViewModelStoreOwner, assets: AssetManager) {
        taskViewModel = ViewModelProvider(view)[TaskViewModel::class.java]

        val myTypeFace = Typeface.createFromAsset(assets, "font/tektur_semibold.ttf")

        paint.apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            textSize = 60F
            textAlign = Paint.Align.CENTER
            strokeWidth = vertexRadius
            typeface = myTypeFace
        }
        basePaint.apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4F
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        taskViewModel.motionEvent.value = event
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)



        if (this::taskViewModel.isInitialized && taskViewModel.canDrawOnImageView) {

            when (taskViewModel.selectedTool) {
                0 -> {
                }
                4->{

                }
                6 -> {

                    if (taskViewModel.affineToolCanApply.value!!) {
                        paint.apply {
                            color = origTriangleColor
                            strokeWidth = vertexRadius / 4
                        }
                        drawTriangleOutline(canvas, taskViewModel.affineToolOrigTriangle)
                    }

                    paint.strokeWidth = vertexRadius
                    for (vi in 0..2) {
                        if (taskViewModel.affineToolOrigTriangle.vertices[vi].x != 0F && taskViewModel.affineToolOrigTriangle.vertices[vi].y != 0F) {
                            paint.color = origTriangleColor
                            canvas.drawPoint(taskViewModel.affineToolOrigTriangle.vertices[vi].x, taskViewModel.affineToolOrigTriangle.vertices[vi].y, paint)
                            paint.color = Color.WHITE
                            canvas.drawText("1" + letters[vi], taskViewModel.affineToolOrigTriangle.vertices[vi].x, taskViewModel.affineToolOrigTriangle.vertices[vi].y + paint.strokeWidth / 6, paint)
                        }
                    }

                    if (taskViewModel.affineToolCanApply.value!!) {
                        paint.apply {
                            color = transTriangleColor
                            strokeWidth = vertexRadius / 4
                        }
                        drawTriangleOutline(canvas, taskViewModel.affineToolTransTriangle)
                    }

                    paint.strokeWidth = vertexRadius
                    for (vi in 0..2) {
                        if (taskViewModel.affineToolTransTriangle.vertices[vi].x != 0F && taskViewModel.affineToolTransTriangle.vertices[vi].y != 0F) {
                            paint.color = transTriangleColor
                            canvas.drawPoint(taskViewModel.affineToolTransTriangle.vertices[vi].x, taskViewModel.affineToolTransTriangle.vertices[vi].y, paint)
                            paint.color = Color.WHITE
                            canvas.drawText("2" + letters[vi], taskViewModel.affineToolTransTriangle.vertices[vi].x, taskViewModel.affineToolTransTriangle.vertices[vi].y + paint.strokeWidth / 6, paint)
                        }
                    }

                }
            }
        }
    }

    private fun drawTriangleOutline(canvas: Canvas, triangle: Triangle2D) {

        canvas.drawLine(triangle.vertices[0].x, triangle.vertices[0].y,
            triangle.vertices[1].x, triangle.vertices[1].y, paint)
        canvas.drawLine(triangle.vertices[1].x, triangle.vertices[1].y,
            triangle.vertices[2].x, triangle.vertices[2].y, paint)
        canvas.drawLine(triangle.vertices[2].x, triangle.vertices[2].y,
            triangle.vertices[0].x, triangle.vertices[0].y, paint)

    }
}