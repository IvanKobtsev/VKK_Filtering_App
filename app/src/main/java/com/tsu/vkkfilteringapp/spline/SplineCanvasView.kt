package com.tsu.vkkfilteringapp.spline

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.res.ResourcesCompat
import com.tsu.vkkfilteringapp.R
import java.util.LinkedList
import kotlin.math.abs

private const val STROKE_WIDTH = 10f
private const val STROKE_SPLINE_WIDTH = 30f
class SplineCanvasView (context: Context, attributeSet: AttributeSet):View(context,attributeSet){

    // for caching
    private var imageMemory :LinkedList<Bitmap> = LinkedList<Bitmap>()
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var coordinateArrayList: ArrayList<ArrayList<Float>> = ArrayList()

    private var operatingModes = 0


    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private val arrayColor = ResourcesCompat.getColor(resources, R.color.colorArrow, null)

    private var path = Path()

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private val arrow = Paint().apply {
        color = arrayColor
        isAntiAlias = true

        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = convertPixelsToDp(context, STROKE_WIDTH) // defau
    }

    private val splinePath= Paint().apply {
        color = drawColor
        isAntiAlias = true

        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = convertPixelsToDp(context, STROKE_SPLINE_WIDTH)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)
        startCanvas(canvas)

        when(operatingModes){
            0->{startCanvas(canvas)
                drawPoints(canvas)
                drawPath(canvas)
            }
            2->{
                drawPoints(canvas)
                createBezierCurve(
                    canvas,
                        BezierCurve(coordinateArrayList)
                )
            }
            3-> {

                coordinateArrayList = ArrayList()
            }
            4-> {
                if (coordinateArrayList.size <= 2) {
                    drawPoints(canvas)
                    drawPath(canvas)
                    Toast.makeText(
                        context,
                        "There are not enough points, add more points", Toast.LENGTH_LONG
                    ).show()

                } else {
                    drawPoints(canvas)
                    createCloseBSpline(
                        canvas,
                        CloseBSpline(
                            coordinateArrayList
                        )
                    )

                }
            }
            5->{
                drawPoints(canvas)
                createSpline(canvas,
                    СubicSpline(
                        coordinateArrayList
                    )
                )
            }

        }
        operatingModes = 0


    }

    private fun createCloseBSpline(canvas: Canvas, closeBSpline: CloseBSpline) {
        for(i in 0 until coordinateArrayList.size) {
            var tempMatrix = closeBSpline.createIntermediateMatrix(i)
            var oldCoordinate = closeBSpline.getPoint(0f,tempMatrix)
            var t = 0f

            while (t<=1)
            {

                t+=0.01f
                var newCooradinate = closeBSpline.getPoint(t,tempMatrix)

                canvas.drawLine(oldCoordinate[0][0],oldCoordinate[0][1],
                    newCooradinate[0][0],newCooradinate[0][1],splinePath)
                oldCoordinate = newCooradinate

            }

        }
    }

    private fun createBezierCurve(canvas:Canvas,curve: BezierCurve) {
        var t=0F
        var old = curve.findCoordinate(0f)
        while (t <= 1) {
            t+=0.01F
            var new = curve.findCoordinate(t)
            canvas.drawLine(old[0],old[1],new[0],new[1],splinePath)
            old = new
        }

    }

    private fun createSpline(canvas: Canvas,spline : СubicSpline){
        for(point in 0 until coordinateArrayList.size-1 )
        {
            var i = coordinateArrayList[point][0]
            var oldY = spline.findY(coordinateArrayList[point][0],point)

            while (i<coordinateArrayList[point+1][0]) {
                i+=0.1F
                var newY = spline.findY(i,point)
                canvas.drawLine(i,oldY,i,newY,splinePath)
                oldY = newY
            }
        }
    }

    fun setOperatingModes(type:Int){
        operatingModes = type
        invalidate()
    }

    private fun drawPoints(canvas: Canvas){
        for (i in 0 until coordinateArrayList.size)
            canvas.drawCircle(coordinateArrayList[i][0],coordinateArrayList[i][1],
                convertPixelsToDp(context,30f),splinePath)
    }
    private fun drawPath(canvas: Canvas) {
        if(coordinateArrayList.isEmpty())return


        for (i in 1 until coordinateArrayList.size) {
            canvas.drawLine(
                coordinateArrayList[i - 1][0], coordinateArrayList[i - 1][1],
                coordinateArrayList[i][0], coordinateArrayList[i][1],
                splinePath
            )

        }
    }

    private fun startCanvas(canvas: Canvas){
        canvas.drawLine(convertPixelsToDp(context,30f), height-convertPixelsToDp(context,300f),
            width-convertPixelsToDp(context,50f),height-convertPixelsToDp(context,300f),arrow)

        canvas.drawLine(width-convertPixelsToDp(context,100f), height-convertPixelsToDp(context,350f),
            width-convertPixelsToDp(context,50f),height-convertPixelsToDp(context,300f),arrow)
        canvas.drawLine(width-convertPixelsToDp(context,100f), height-convertPixelsToDp(context,250f),
            width-convertPixelsToDp(context,50f),height-convertPixelsToDp(context,300f),arrow)

        canvas.drawLine(convertPixelsToDp(context,160f), height-convertPixelsToDp(context,50f),
            convertPixelsToDp(context,160f),convertPixelsToDp(context,25f),arrow)

        canvas.drawLine(convertPixelsToDp(context,110f), convertPixelsToDp(context,75f),
            convertPixelsToDp(context,160f),convertPixelsToDp(context,25f),arrow)
        canvas.drawLine(convertPixelsToDp(context,210f),convertPixelsToDp(context,75f),
            convertPixelsToDp(context,160f),convertPixelsToDp(context,25f),arrow)
    }

    private var currentX = 0f
    private var currentY = 0f

    private fun createPointWithoutSort(){
        val pointArray = ArrayList<Float>()
        pointArray.add(motionTouchEventX)
        pointArray.add(motionTouchEventY)
        coordinateArrayList.add(pointArray)
        invalidate()
    }

    private fun createPoint(){
        if(coordinateArrayList.isEmpty()) {
            coordinateArrayList.add(ArrayList())
            coordinateArrayList[0].add(motionTouchEventX)
            coordinateArrayList[0].add(motionTouchEventY)
        }
        else
            for(i in 0 ..  coordinateArrayList.size) {
                if(i == coordinateArrayList.size){
                    coordinateArrayList.add(ArrayList())
                    coordinateArrayList[i].add(motionTouchEventX)
                    coordinateArrayList[i].add(motionTouchEventY)
                    break
                }

                if (coordinateArrayList[i][0] < motionTouchEventX)
                    continue

                Log.e("splineDraw",""+i)
                coordinateArrayList.add(i,ArrayList())
                coordinateArrayList[i].add(motionTouchEventX)
                coordinateArrayList[i].add(motionTouchEventY)
                break
            }

        Log.e("splineDraw",""+coordinateArrayList)


        invalidate()
    }
    private fun touchStart() {
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            //extraCanvas.drawPath(path, paint)
        }

    }

    private fun touchUp() {
        createPointWithoutSort()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y


        when (event.action) {
            //MotionEvent.ACTION_DOWN -> touchStart()
            //MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }
}

fun convertDpToPixels(context: Context, dp: Float) =
    dp * context.resources.displayMetrics.density

fun convertPixelsToDp(context: Context, pixels: Float) =
    pixels / context.resources.displayMetrics.density
