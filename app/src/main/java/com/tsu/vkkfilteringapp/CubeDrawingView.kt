package com.tsu.vkkfilteringapp

import android.R.attr
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.FillType
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import java.io.IOException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan


class Draw2D(context: Context?) : View(context) {

    // Technical stuff
    private val paint: Paint = Paint()
    private val gson = Gson()

    // FOV-related
    private var fovNear = 0.1F
    private var fovFar = 1000F
    private var fovDeg = 90F
    private var fovRad = 1F / tan(fovDeg * 0.5F / 180F * Math.PI).toFloat()

    // Shape transform
    private var shapeRotation = Point3D()
    private var shapeTranslation = Point3D(0F, 0F, 2F)
    private var shapeScale = 1000F

    // Lighting
    private var lightDirection = Point3D(0F, 0F, -1F)

    // Matrices' declaration
    private var projectionMatrix = Matrix4x4(mutableListOf(
        mutableListOf(fovRad, 0F, 0F, 0F),
        mutableListOf(0F, fovRad, 0F, 0F),
        mutableListOf(0F, 0F, fovFar / (fovFar - fovNear), 1F),
        mutableListOf(0F, 0F, (-fovFar * fovNear) / (fovFar - fovNear), 0F)
    ))

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

    private var rotationYMatrix = Matrix4x4(mutableListOf(
        mutableListOf(0F, 0F, 0F, 0F),
        mutableListOf(0F, 1F, 0F, 0F),
        mutableListOf(0F, 0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F, 1F)
    ))

    private var shapeToShow = Mesh()

    // Variables used in onDraw()
    private var projectedTriangle = Triangle2D()
    private var triangleToProject = Triangle3D()
    private var firstTriangleLine = Line3D(Point3D(), Point3D())
    private var secondTriangleLine = Line3D(Point3D(), Point3D())
    private var normal = Point3D()
    private var normalLength = 0F
    private var lightDP = 0F

    // Timer
    val timer = object: CountDownTimer(20000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {

        }
    }

    // My technical staff

    fun init() {
        paint.apply {
            isAntiAlias = true
        }
        shapeToShow = get3DObjectJson("cube")
    }

    private fun get3DObjectJson(objectName: String): Mesh {
        var jsonString: String? = null
        try {
            jsonString = MainActivity.applicationContext().assets
                .open("${objectName}.json")
                .bufferedReader().use {
                    it.readText()
                }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Mesh()
        }

        return gson.fromJson(jsonString, Mesh::class.java)
    }

    // 2D Object Operations ----------------------------------------------------------------------

    private fun hypotenuse(cathetus1: Float, cathetus2: Float): Float {
        return sqrt(abs(cathetus1).pow(2) + abs(cathetus2).pow(2))
    }

    // 3D Object Operations

    private fun dotProduct(point1: Point3D, point2: Point3D) : Float {
        return point1.x * point2.x + point1.y * point2.y + point1.z * point2.z
    }

    // Matrices Updaters -------------------------------------------------------------------------

    private fun updateProjectionMatrix() {
        projectionMatrix.rows[0][0] = fovRad
        projectionMatrix.rows[1][1] = fovRad
        projectionMatrix.rows[2][2] = fovFar / (fovFar - fovNear)
        projectionMatrix.rows[3][2] =(-fovFar * fovNear) / (fovFar - fovNear)
    }

    private fun updateRotationXMatrix() {
        rotationXMatrix.rows[1][1] = cos(shapeRotation.x)
        rotationXMatrix.rows[1][2] = sin(shapeRotation.x)
        rotationXMatrix.rows[2][1] = -sin(shapeRotation.x)
        rotationXMatrix.rows[2][2] = cos(shapeRotation.x)
    }

    private fun updateRotationYMatrix() {
        rotationYMatrix.rows[2][2] = cos(shapeRotation.y)
        rotationYMatrix.rows[0][2] = sin(shapeRotation.y)
        rotationYMatrix.rows[2][0] = -sin(shapeRotation.y)
        rotationYMatrix.rows[0][0] = cos(shapeRotation.y)
    }
    private fun updateRotationZMatrix() {
        rotationZMatrix.rows[0][0] = cos(shapeRotation.z)
        rotationZMatrix.rows[0][1] = sin(shapeRotation.z)
        rotationZMatrix.rows[1][0] = -sin(shapeRotation.z)
        rotationZMatrix.rows[1][1] = cos(shapeRotation.z)
    }

    // Other matrix operations ------------------------------------------------------------

    private fun matrixMultiply(firstMatrix: Matrix3x3, secondMatrix: Matrix3x3) : Matrix3x3 {

        var resultMatrix = Matrix3x3()

        for (yi in 0..2) {
            for (xi in 0..2) {
                for (i in 0..2) {
                    resultMatrix.rows[yi][xi] += firstMatrix.rows[yi][i] * secondMatrix.rows[i][xi]
                }
            }
        }

        return resultMatrix
    }

    // Draw functions ---------------------------------------------------------------------

    private fun drawShape(canvas: Canvas) {
        updateRotationXMatrix()
        updateRotationYMatrix()
        updateRotationZMatrix()

        var trianglesToDraw: MutableList<Triangle2D> = mutableListOf()

        for (triangle in shapeToShow.triangles) {
            triangleToProject.vertices = triangle.copyVertices()

            // Applying rotations
            triangleToProject.rotateByMatrix(rotationXMatrix)
            triangleToProject.rotateByMatrix(rotationYMatrix)
            triangleToProject.rotateByMatrix(rotationZMatrix)

            // Translate for better view
            triangleToProject.translate(0F, 0F, shapeTranslation.z)

            // Check for normal
            firstTriangleLine.setNewPoints(triangleToProject.vertices[0], triangleToProject.vertices[1])
            secondTriangleLine.setNewPoints(triangleToProject.vertices[0], triangleToProject.vertices[2])
            normal = firstTriangleLine.getNormalToOtherLine(secondTriangleLine)
            normal.scaleByDivision(normal.getVectorLength())

            if (dotProduct(normal, triangleToProject.vertices[0]) < 0F) {

                // Instancing new triangle to project
                var projectedTriangle = Triangle2D()

                // Calculating shadowing
                lightDirection.scaleByDivision(lightDirection.getVectorLength())
                projectedTriangle.lightAmount = dotProduct(normal, lightDirection)
                projectedTriangle.depth = triangleToProject.getCentroid().z

                // Transforming from 3D to 2D

                projectedTriangle.changeProjectionFrom3DByMatrix(triangleToProject, projectionMatrix)

                // Applying scaling and centering for better view
                projectedTriangle.scale(shapeScale)
                projectedTriangle.center(width, height)
                trianglesToDraw.add(projectedTriangle)
            }
        }

        trianglesToDraw.sortBy { it.depth }

        for (triangleInd in trianglesToDraw.size - 1 downTo 0) {
            drawTriangle(trianglesToDraw[triangleInd],
                Color.valueOf(
                    trianglesToDraw[triangleInd].lightAmount,
                    trianglesToDraw[triangleInd].lightAmount,
                    trianglesToDraw[triangleInd].lightAmount),
                canvas)
        }
    }

    private fun drawImage(canvas: Canvas) {

        val bm = BitmapFactory.decodeResource(resources, R.drawable.phoenix_wright)
        canvas.drawBitmap(bm, 0F, 0F, paint)

        var origTriMatrix = Matrix3x3(origTriangle)
        var transformedTriMatrix = Matrix3x3(transformedTriangle)

        var invertedMatrix = origTriMatrix.getInvertedMatrix()

        var affineTransformMatrix = matrixMultiply(transformedTriMatrix, invertedMatrix)
//        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        fillTheCanvas(canvas)
        drawShape(canvas)

//        drawImage(canvas)
    }

    // For touch handling
    private var lastX = 0F
    private var lastY = 0F
    private var currentScale = 0F
    private var lastScale = 0F
    private var lastTouchCount = 0

    // For affine transformations
    private var origTriangle = Triangle2D()
    private var transformedTriangle = Triangle2D()
    private var currentVertexToPlace = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {

        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                true
            }

            MotionEvent.ACTION_MOVE -> {

                if (event.pointerCount != lastTouchCount) {
                    cancelTouchEvents(event)
                }

                when (event.pointerCount) {
                    1 -> {
                        shapeRotation.x += (event.y - lastY) / 100
                        shapeRotation.y += (event.x - lastX) / 100

                        lastX = event.x
                        lastY = event.y

//                        when (currentVertexToPlace) {
//                            0 -> {
//                                origTriangle.vertices[0].x = event.x
//                                origTriangle.vertices[0].y = event.y
//
//                                currentVertexToPlace = 1
//                            }
//                            1 -> {
//                                origTriangle.vertices[1].x = event.x
//                                origTriangle.vertices[1].y = event.y
//
//                                currentVertexToPlace = 2
//                            }
//                            2 -> {
//                                origTriangle.vertices[2].x = event.x
//                                origTriangle.vertices[2].y = event.y
//
//                                currentVertexToPlace = 3
//                            }
//                            3 -> {
//                                transformedTriangle.vertices[0].x = event.x
//                                transformedTriangle.vertices[0].y = event.y
//
//                                currentVertexToPlace = 4
//                            }
//                            4 -> {
//                                transformedTriangle.vertices[1].x = event.x
//                                transformedTriangle.vertices[1].y = event.y
//
//                                currentVertexToPlace = 5
//                            }
//                            5 -> {
//                                transformedTriangle.vertices[2].x = event.x
//                                transformedTriangle.vertices[2].y = event.y
//
//                                currentVertexToPlace = 0
//                            }
//                        }

                    }
                    2 -> {
                        currentScale = hypotenuse(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
                        shapeScale += (currentScale - lastScale)
                        lastScale = currentScale
                    }
                }

                invalidate()

                true
            }
            MotionEvent.ACTION_UP -> {
                cancelTouchEvents(event)
                lastTouchCount = 0
                true
            }
            else -> super.onTouchEvent(event)
        }

    }

    private fun cancelTouchEvents(event: MotionEvent) {
        lastX = event.x
        lastY = event.y
        lastTouchCount = event.pointerCount

        if (event.pointerCount == 2) {
            lastScale = hypotenuse(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
        }
    }

    private fun fillTheCanvas(canvas: Canvas) {

        paint.apply {
            style = Paint.Style.FILL
            color = Color.CYAN
        }
        canvas.drawPaint(paint)

    }

    private fun drawTriangle(triangle: Triangle2D, triangleColor: Color, canvas: Canvas) {

//        For debug purposes
//        paint.apply { color = Color.BLUE }

//        canvas.drawLine(triangle.vertices[0].x, triangle.vertices[0].y, triangle.vertices[1].x, triangle.vertices[1].y, paint)
//        canvas.drawLine(triangle.vertices[1].x, triangle.vertices[1].y, triangle.vertices[2].x, triangle.vertices[2].y, paint)
//        canvas.drawLine(triangle.vertices[2].x, triangle.vertices[2].y, triangle.vertices[0].x, triangle.vertices[0].y, paint)

        paint.apply { color = triangleColor.toArgb() }

        val path = Path()
        path.fillType = FillType.EVEN_ODD
        path.moveTo(triangle.vertices[0].x, triangle.vertices[0].y)
        path.lineTo(triangle.vertices[1].x, triangle.vertices[1].y)
        path.lineTo(triangle.vertices[2].x, triangle.vertices[2].y)
        path.close()

        canvas.drawPath(path, paint)
    }
}