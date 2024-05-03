package com.tsu.vkkfilteringapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.concurrent.locks.Lock
import java.util.stream.Collectors.toList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


class Draw2D(context: Context?) : View(context) {
    private val paint: Paint = Paint()
    private var fovNear = 0.1F
    private var fovFar = 1000F
    private var fovDeg = 90F
    private var fovRad = 1F / tan(fovDeg * 0.5F / 180F * Math.PI).toFloat()
    private var cubeRotation = Point3D(0F, 0F, 0F)
    private var cubeTranslation = Point3D(0F, 0F, 0F)

    private var projectionMatrix = Matrix4x4(mutableListOf(
        mutableListOf(9F / 16F * fovRad, 0F, 0F, 0F),
        mutableListOf(0F, fovRad, 0F, 0F),
        mutableListOf(0F, 0F, fovFar / (fovFar - fovNear), 1F),
        mutableListOf(0F, 0F, (-fovFar * fovNear) / (fovFar - fovNear), 0F)))

    private var rotationXMatrix = Matrix4x4(mutableListOf(
        mutableListOf(1F, 0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F, 1F)
    ))

    private var rotationZMatrix = Matrix4x4(mutableListOf(
        mutableListOf(0F, 0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F, 0F),
        mutableListOf(0F, 0F, 1F, 0F),
        mutableListOf(0F, 0F, 0F, 1F)
    ))

//    private var rotationYMatrix = Matrix4x4(listOf(
//        listOf(1F, 0F, 0F, 0F),
//        listOf(1F, 0F, 0F, 0F),
//        listOf(1F, 0F, 0F, 0F),
//        listOf(1F, 0F, 0F, 0F)
//    ))

    private var cube = Mesh(mutableListOf(
        // Bottom
        Triangle3D(mutableListOf(Point3D(0F, 0F, 0F), Point3D(1F, 0F, 0F), Point3D(0F, 1F, 0F))),
        Triangle3D(mutableListOf(Point3D(1F, 1F, 0F), Point3D(1F, 0F, 0F), Point3D(0F, 1F, 0F))),
        // Top
        Triangle3D(mutableListOf(Point3D(0F, 0F, 1F), Point3D(1F, 0F, 1F), Point3D(0F, 1F, 1F))),
        Triangle3D(mutableListOf(Point3D(1F, 1F, 1F), Point3D(1F, 0F, 1F), Point3D(0F, 1F, 1F))),
        // Front
        Triangle3D(mutableListOf(Point3D(0F, 0F, 0F), Point3D(0F, 0F, 1F), Point3D(1F, 0F, 0F))),
        Triangle3D(mutableListOf(Point3D(0F, 0F, 1F), Point3D(1F, 0F, 0F), Point3D(1F, 0F, 1F))),
        // Back
        Triangle3D(mutableListOf(Point3D(1F, 1F, 0F), Point3D(0F, 1F, 0F), Point3D(1F, 1F, 1F))),
        Triangle3D(mutableListOf(Point3D(0F, 1F, 1F), Point3D(0F, 1F, 0F), Point3D(1F, 1F, 1F))),
        // Left
        Triangle3D(mutableListOf(Point3D(0F, 1F, 0F), Point3D(0F, 0F, 1F), Point3D(0F, 0F, 0F))),
        Triangle3D(mutableListOf(Point3D(0F, 1F, 0F), Point3D(0F, 0F, 1F), Point3D(0F, 1F, 1F))),
        // Right
        Triangle3D(mutableListOf(Point3D(1F, 0F, 0F), Point3D(1F, 0F, 1F), Point3D(1F, 1F, 0F))),
        Triangle3D(mutableListOf(Point3D(1F, 0F, 1F), Point3D(1F, 1F, 0F), Point3D(1F, 1F, 1F))),
    ))

    private var projectedTriangle = Triangle2D(mutableListOf(
        Point2D(0F, 0F),
        Point2D(0F, 0F),
        Point2D(0F, 0F)
    ))

    private var triangleToProject = Triangle3D(mutableListOf(
        Point3D(0F, 0F, 0F),
        Point3D(0F, 0F, 0F),
        Point3D(0F, 0F, 0F)
    ))

    val timer = object: CountDownTimer(20000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {

        }
    }

    private fun updateRotationXMatrix() {
        rotationXMatrix.rows[1][1] = cos(cubeRotation.x * 0.5F)
        rotationXMatrix.rows[1][2] = sin(cubeRotation.x * 0.5F)
        rotationXMatrix.rows[2][1] = -sin(cubeRotation.x * 0.5F)
        rotationXMatrix.rows[2][2] = cos(cubeRotation.x * 0.5F)
    }

    private fun updateRotationZMatrix() {
        rotationZMatrix.rows[0][0] = cos(cubeRotation.z)
        rotationZMatrix.rows[0][1] = sin(cubeRotation.z)
        rotationZMatrix.rows[1][0] = -sin(cubeRotation.z)
        rotationZMatrix.rows[1][1] = cos(cubeRotation.z)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        fillTheCanvas(canvas)

        paint.apply {
            isAntiAlias = true
            color = Color.WHITE
        }

        updateRotationXMatrix()
        updateRotationZMatrix()

        for (triangle in cube.triangles) {
            triangleToProject.vertices = triangle.vertices.toMutableList()
            triangleToProject.translate(0F, 0F, cubeTranslation.z)
            triangleToProject.rotateByMatrix(rotationXMatrix)
            triangleToProject.rotateByMatrix(rotationZMatrix)
            projectedTriangle.changeProjectionFrom3DByMatrix(triangleToProject, projectionMatrix)
            projectedTriangle.scale(0.3F, height, width)
            drawTriangle(projectedTriangle, canvas)
        }

    }

    private var lastX: Float = 0F
    private var lastY: Float = 0F
    private var touchCount: Int = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {

            MotionEvent.ACTION_POINTER_DOWN -> {
                ++touchCount
                Log.e("SomeTag", "Action was POINTER_DOWN TouchCount:$touchCount")
                true
            }
            MotionEvent.ACTION_DOWN -> {

                ++touchCount;
                lastX = event.x
                lastY = event.y

                Log.e("SomeTag", "Action was DOWN TouchCount:$touchCount")
                true
            }

            MotionEvent.ACTION_MOVE -> {


                if (!(abs(lastX - event.x) > 50 || abs(lastY - event.y) > 50)) {

                    when (touchCount) {
                        1 -> {
                            cubeRotation.x += (lastX - event.x) / 5000
                            cubeRotation.z += (lastY - event.y) / 5000
                            cubeTranslation.z = (lastX - event.x) / 100
                            Log.e("SomeTag", "X: " + cubeRotation.x + " Z:" + cubeRotation.z)
                        }
                        2 -> {
                            //cube.translate((event.x - lastX) / 1000, (event.y - lastY) / 1000, 0F)
                        }
                    }
                    lastX = event.x
                    lastY = event.y
                }

                invalidate()

                true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                --touchCount
                Log.e("SomeTag", "Action was UP TouchCount:$touchCount")
                true
            }
            MotionEvent.ACTION_UP -> {
                --touchCount
                Log.e("SomeTag", "Action was UP TouchCount:$touchCount")
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                --touchCount;
                true
            }
            MotionEvent.ACTION_OUTSIDE -> {
                true
            }
            else -> super.onTouchEvent(event)
        }

    }

    private fun fillTheCanvas(canvas: Canvas) {

        paint.apply {
            style = Paint.Style.FILL
            color = Color.BLACK
        }
        canvas.drawPaint(paint)

    }

    private fun drawTriangle(triangle: Triangle2D, canvas: Canvas) {
        canvas.drawLine(triangle.vertices[0].x, triangle.vertices[0].y, triangle.vertices[1].x, triangle.vertices[1].y, paint)
        canvas.drawLine(triangle.vertices[1].x, triangle.vertices[1].y, triangle.vertices[2].x, triangle.vertices[2].y, paint)
        canvas.drawLine(triangle.vertices[2].x, triangle.vertices[2].y, triangle.vertices[0].x, triangle.vertices[0].y, paint)
    }
}