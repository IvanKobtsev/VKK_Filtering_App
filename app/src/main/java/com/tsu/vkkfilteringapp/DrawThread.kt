package com.tsu.vkkfilteringapp

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.FillType
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.google.gson.Gson
import java.io.IOException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class DrawThread() : Thread() {

    private var surfaceHolder: SurfaceHolder? = null
    private var resources: Resources? = null

    // Temporary !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private val height = 2000
    private val width = 1080

    // Chosen preferences
    var blankShape = false
    var chosenShape = 1

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

    // Pictures of numbers
    private val pictures = listOf(R.drawable.astral_chain,
        R.drawable.sylvain,
        R.drawable.link,
        R.drawable.iconoclasts,
        R.drawable.blaster_master,
        R.drawable.mario_maker,
        R.drawable.chrom,
        R.drawable.number8,
        R.drawable.number9,
        R.drawable.number10,
        R.drawable.number11,
        R.drawable.number12,
        R.drawable.number13,
        R.drawable.number14,
        R.drawable.number15,
        R.drawable.number16,
        R.drawable.number17,
        R.drawable.number18,
        R.drawable.number19,
        R.drawable.number20
    )

    private val shapes = listOf(
        "d4",
        "d6",
        "d8",
        "d10",
        "d12",
        "d20"
    )

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
    private var triangleToProject = Triangle3D()
    private var firstTriangleLine = Line3D(Point3D(), Point3D())
    private var secondTriangleLine = Line3D(Point3D(), Point3D())
    private var normal = Point3D()
    private var trianglesToDraw: MutableList<Triangle2D> = mutableListOf()
    private var triangleToDemonstrate = Triangle2D()

    constructor(givenSurfaceHolder: SurfaceHolder, givenResources: Resources) : this() {

        surfaceHolder = givenSurfaceHolder
        resources = givenResources

    }

    var running = false
    private var prevTime: Long = 0
    private var canvas: Canvas? = null
    override fun run() {

        while (running) {
            try {
                canvas = surfaceHolder!!.lockCanvas()
                synchronized(surfaceHolder!!) {

                    clearTheCanvas(canvas!!)

                    fillTrianglesToDraw()

                    if (blankShape) {
                        renderTriangles(canvas!!)
                    }
                    else {
                        if (shapeToShow.hasTriangularFaces) {
                            drawImages(canvas!!)
                        }
                        else {
                            drawImages(canvas!!, true)
                        }
                    }

                }
            } finally {
                if (canvas != null) {
                    surfaceHolder!!.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    // My technical staff

    fun init(drawBlank: Boolean) {
        paint.apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2F
            textSize = 100F
        }
        loadShapeToShow()
        fillTrianglesToDraw()
        blankShape = drawBlank
        running = true
        start()
    }

    fun loadShapeToShow() {
        shapeToShow = get3DObjectJson(shapes[chosenShape])
    }

    private fun get3DObjectJson(objectName: String): Mesh {
        val jsonString: String
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

    private fun clamp(value: Float, min: Float, max: Float) : Float {
        return max(min(value, max), min)
    }

    private fun clamp(value: Int, min: Int, max: Int) : Int {
        return max(min(value, max), min)
    }

    // Painting functions

    private fun fillUpsideTriangle(triangle: Triangle2D, transformMatrix: Matrix3x3, bitmap: Bitmap, canvas: Canvas) {

        val currentPoint = Point2D(triangle.vertices[0].x, triangle.vertices[0].y)

        var leftBorder = currentPoint.x
        var rightBorder = currentPoint.x + 1F

        val maxWidth = bitmap.width - 1
        val maxHeight = bitmap.height - 1

        val rightDelta = (triangle.vertices[2].x - triangle.vertices[0].x) / (triangle.vertices[2].y - triangle.vertices[0].y)
        val leftDelta = (triangle.vertices[1].x - triangle.vertices[0].x) / (triangle.vertices[1].y - triangle.vertices[0].y)

        while (currentPoint.y < triangle.vertices[2].y) {

            while (currentPoint.x < rightBorder) {

                paint.color = bitmap.getPixel(clamp(currentPoint.getTransformedX(transformMatrix).toInt(), 0, maxWidth),
                    clamp(currentPoint.getTransformedY(transformMatrix).toInt(), 0, maxHeight))

                canvas.drawPoint(currentPoint.x, currentPoint.y, paint)
                ++currentPoint.x
            }

            leftBorder += leftDelta
            rightBorder += rightDelta

            currentPoint.x = leftBorder

            ++currentPoint.y
        }
    }

    private fun fillDownsideTriangle(triangle: Triangle2D, transformMatrix: Matrix3x3, bitmap: Bitmap, canvas: Canvas) {

        val currentPoint = Point2D(triangle.vertices[0].x, triangle.vertices[0].y)

        val maxWidth = bitmap.width - 1
        val maxHeight = bitmap.height - 1

        var leftBorder = currentPoint.x
        var rightBorder = triangle.vertices[1].x

        val leftDelta = (triangle.vertices[2].x - triangle.vertices[0].x) / (triangle.vertices[2].y - triangle.vertices[0].y)
        val rightDelta = (triangle.vertices[2].x - triangle.vertices[1].x) / (triangle.vertices[2].y - triangle.vertices[1].y)

        while (currentPoint.y < triangle.vertices[2].y) {

            while (currentPoint.x < rightBorder) {

                paint.color = bitmap.getPixel(clamp(currentPoint.getTransformedX(transformMatrix).toInt(), 0, maxWidth),
                    clamp(currentPoint.getTransformedY(transformMatrix).toInt(), 0, maxHeight))

                canvas.drawPoint(currentPoint.x, currentPoint.y, paint)
                ++currentPoint.x
            }

            leftBorder += leftDelta
            rightBorder += rightDelta

            currentPoint.x = leftBorder

            ++currentPoint.y
        }
    }

    private fun fillTriangle(triangle: Triangle2D, transformMatrix: Matrix3x3, bitmap: Bitmap, canvas: Canvas) {

        paint.color = Color.valueOf(triangle.lightAmount, triangle.lightAmount, triangle.lightAmount).toArgb()

        triangle.vertices.sortBy { it.y }

        if (triangle.vertices[0].y == triangle.vertices[1].y) {
            fillDownsideTriangle(triangle, transformMatrix, bitmap, canvas)
        }
        else if (triangle.vertices[1].y == triangle.vertices[2].y) {
            fillUpsideTriangle(triangle, transformMatrix, bitmap, canvas)
        }
        else {

            val secondMiddlePoint = Point2D(
                triangle.vertices[0].x +
                        ((triangle.vertices[1].y - triangle.vertices[0].y) /
                                (triangle.vertices[2].y - triangle.vertices[0].y)) *
                        (triangle.vertices[2].x - triangle.vertices[0].x),
                triangle.vertices[1].y)

            if (secondMiddlePoint.x > triangle.vertices[1].x) {
                fillUpsideTriangle(Triangle2D(mutableListOf(
                    triangle.vertices[0],
                    triangle.vertices[1],
                    secondMiddlePoint
                )), transformMatrix, bitmap, canvas)
                fillDownsideTriangle(Triangle2D(mutableListOf(
                    triangle.vertices[1],
                    secondMiddlePoint,
                    triangle.vertices[2]
                )), transformMatrix, bitmap, canvas)
            }
            else {
                fillUpsideTriangle(Triangle2D(mutableListOf(
                    triangle.vertices[0],
                    secondMiddlePoint,
                    triangle.vertices[1]
                )), transformMatrix, bitmap, canvas)
                fillDownsideTriangle(Triangle2D(mutableListOf(
                    secondMiddlePoint,
                    triangle.vertices[1],
                    triangle.vertices[2]
                )), transformMatrix, bitmap, canvas)
            }
        }

    }



    // 2D Object Operations ----------------------------------------------------------------------

    private fun hypotenuse(cathetus1: Float, cathetus2: Float): Float {
        return sqrt(abs(cathetus1).pow(2) + abs(cathetus2).pow(2))
    }

    private fun tanBetweenTwoLines(vector1: Point2D, vector2: Point2D) : Float {

        if (vector1.x == 0F || vector2.x == 0F) {
            return 0F
        }

        val k1 = vector1.y / vector1.x
        val k2 = vector2.y / vector2.x

        return (k2 - k1) / (1 + k1 * k2)
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

    private fun drawTriangle(triangle: Triangle2D, canvas: Canvas, affineTransform: Matrix3x3, image: Bitmap) {
        fillTriangle(triangle, affineTransform, image, canvas)
    }

    private fun drawTriangle(triangle: Triangle2D, triangleColor: Color, canvas: Canvas) {

        val path = Path()
        path.fillType = FillType.EVEN_ODD
        path.moveTo(triangle.vertices[0].x, triangle.vertices[0].y)
        path.lineTo(triangle.vertices[1].x, triangle.vertices[1].y)
        path.lineTo(triangle.vertices[2].x, triangle.vertices[2].y)
        path.close()

        paint.color = triangleColor.toArgb()

        canvas.drawPath(path, paint)
    }

    private fun fillTrianglesToDraw() {
        updateRotationXMatrix()
        updateRotationYMatrix()
        updateRotationZMatrix()

        trianglesToDraw = mutableListOf()

        for (triangleInd in 0..<shapeToShow.triangles.size) {
            triangleToProject.vertices = shapeToShow.triangles[triangleInd].copyVertices()

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
                projectedTriangle.imageId = triangleInd

                // Transforming from 3D to 2D

                projectedTriangle.changeProjectionFrom3DByMatrix(triangleToProject, projectionMatrix)

                // Applying scaling and centering for better view
                projectedTriangle.scale(shapeScale)
                projectedTriangle.center(width, height)
                trianglesToDraw.add(projectedTriangle)
            }
        }

//        For overlapping polygons
//        trianglesToDraw.sortBy { it.depth }
    }

    private fun renderTriangles(canvas: Canvas) {
        for (triangleInd in trianglesToDraw.size - 1 downTo 0) {
            drawTriangle(trianglesToDraw[triangleInd],
                Color.valueOf(
                    trianglesToDraw[triangleInd].lightAmount,
                    trianglesToDraw[triangleInd].lightAmount,
                    trianglesToDraw[triangleInd].lightAmount),
                canvas)
        }
    }

    private fun drawImages(canvas: Canvas, squared: Boolean = false) {

        for (triangle in trianglesToDraw) {

            val bitmap: Bitmap

            if (squared) {
                if (triangle.imageId % 2 == 0) {
                    bitmap = BitmapFactory.decodeResource(resources, pictures[triangle.imageId / 2])
                    triangleToDemonstrate.setToProjectImageTop(bitmap)
                }
                else {
                    bitmap = BitmapFactory.decodeResource(resources, pictures[(triangle.imageId - 1) / 2])
                    triangleToDemonstrate.setToProjectImageBottom(bitmap)
                }
            }
            else {
                bitmap = BitmapFactory.decodeResource(resources, pictures[triangle.imageId])
                triangleToDemonstrate.setToProjectWholeImage(bitmap)
            }

            var transformedTriangle = triangle

            val origTriMatrix = Matrix3x3(triangleToDemonstrate)
            val transformedTriMatrix = Matrix3x3(transformedTriangle)

            val invertedMatrix = origTriMatrix.getInvertedMatrix()

            val affineTransformMatrix = matrixMultiply(transformedTriMatrix, invertedMatrix).getInvertedMatrix()
//            val affineTransformMatrix = matrixMultiply(transformedTriMatrix, invertedMatrix)

            //        paint.apply { color = Color.YELLOW }
            //        canvas.drawCircle(origTriangle.vertices[0].x, origTriangle.vertices[0].y, 10F, paint)
            //        canvas.drawCircle(origTriangle.vertices[1].x, origTriangle.vertices[1].y, 10F, paint)
            //        canvas.drawCircle(origTriangle.vertices[2].x, origTriangle.vertices[2].y, 10F, paint)
            //
            //        paint.apply { color = Color.GREEN }
            //        canvas.drawCircle(transformedTriangle.vertices[0].x, transformedTriangle.vertices[0].y, 10F, paint)
            //        canvas.drawCircle(transformedTriangle.vertices[1].x, transformedTriangle.vertices[1].y, 10F, paint)
            //        canvas.drawCircle(transformedTriangle.vertices[2].x, transformedTriangle.vertices[2].y, 10F, paint)

//            transformedTriangle = Triangle2D(matrixMultiply(affineTransformMatrix, origTriMatrix))

            drawTriangle(transformedTriangle, canvas, affineTransformMatrix, bitmap)

//            printTransformedBitmap(bitmap, affineTransformMatrix, canvas, !squared, triangle.imageId % 2 != 0)

//            For debug purposes
//            paint.color = Color.WHITE
//            canvas.drawText(triangle.imageId.toString(),
//                triangle.getCentroid().x,
//                triangle.getCentroid().y, paint)
        }
    }

    // For touch handling
    private var lastX = 0F
    private var lastY = 0F
    private var currentScale = 0F
    private var lastScale = 0F
    private var currentZRotationVector = Point2D()
    private var lastZRotationVector = Point2D()
    private var lastTouchCount = 0

    private fun cancelTouchEvents(event: MotionEvent) {
        lastX = event.x
        lastY = event.y
        lastTouchCount = event.pointerCount

        if (event.pointerCount == 2) {
            lastScale = hypotenuse(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
            lastZRotationVector.setCoordinates(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
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
                    }
                    2 -> {
                        currentScale = hypotenuse(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
                        shapeScale = clamp(shapeScale + (currentScale - lastScale), 100F, 1700F)

                        lastScale = currentScale

                        currentZRotationVector.setCoordinates(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))

                        shapeRotation.z -= tanBetweenTwoLines(currentZRotationVector, lastZRotationVector)
                        lastZRotationVector.copyPointsFrom(currentZRotationVector)
                    }
                }

                true
            }
            MotionEvent.ACTION_UP -> {
                cancelTouchEvents(event)
                lastTouchCount = 0
                true
            }
            else -> false
        }
    }

    private fun clearTheCanvas(canvas: Canvas, givenColor: Int = Color.TRANSPARENT) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
    }
}