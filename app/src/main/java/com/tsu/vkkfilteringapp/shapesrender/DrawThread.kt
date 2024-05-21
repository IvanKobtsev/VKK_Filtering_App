package com.tsu.vkkfilteringapp.shapesrender

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace.Rgb
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.FillType
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.core.graphics.alpha
import com.google.gson.Gson
import com.tsu.vkkfilteringapp.MainActivity
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.filters.AffineTransformation
import com.tsu.vkkfilteringapp.graphics2d.Point2D
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D
import com.tsu.vkkfilteringapp.graphics3d.Line3D
import com.tsu.vkkfilteringapp.graphics3d.Mesh
import com.tsu.vkkfilteringapp.graphics3d.Point3D
import com.tsu.vkkfilteringapp.graphics3d.Triangle3D
import com.tsu.vkkfilteringapp.matrices.Matrix3x3
import com.tsu.vkkfilteringapp.matrices.Matrix4x4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    private val height = 2000
    private val width = 1080

    // Chosen preferences
    var showImages = false
    var chosenShape = 1

    // Technical stuff
    private val paint = Paint()
    private val gson = Gson()
    private val affineTransformation = AffineTransformation.newInstance()

    // FOV-related
    private var fovNear = 0.1F
    private var fovFar = 1000F
    private var fovDeg = 90F
    private var fovRad = 1F / tan(fovDeg * 0.5F / 180F * Math.PI).toFloat()

    // Shape transform
    var shapeRotation = Point3D()
    private var shapeTranslation = Point3D(0F, 0F, 2F)
    private var shapeScale = 500F

    // Lighting
    private var lightDirection = Point3D(0F, 0F, -1F)

    // Pictures of numbers
    private val pictures = listOf(
        R.drawable.mercie,
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4,
        R.drawable.image5,
        R.drawable.image6,
        R.drawable.image7,
        R.drawable.image8,
        R.drawable.image9,
        R.drawable.image10,
        R.drawable.image11,
        R.drawable.image12,
        R.drawable.image13,
        R.drawable.image14,
        R.drawable.image15,
        R.drawable.image16,
        R.drawable.image17,
        R.drawable.image18,
        R.drawable.image19,
        R.drawable.image20
    )

    private val numbers = listOf(
        R.drawable.number1,
        R.drawable.number2,
        R.drawable.number3,
        R.drawable.number4,
        R.drawable.number5,
        R.drawable.number6,
        R.drawable.number7,
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
    private var secretActivated = false
    private var prevTime: Long = 0
    private var canvas: Canvas? = null
    override fun run() {

        while (running) {
            try {
                canvas = surfaceHolder!!.lockCanvas()
                synchronized(surfaceHolder!!) {

                    clearTheCanvas(canvas!!)
                    fillTrianglesToDraw()

                    if (showImages) {
                        if (!secretActivated) {
                            drawImages(canvas!!, pictures, !shapeToShow.hasTriangularFaces)
                        }
                        else {
                            renderTriangles(canvas!!)
                            drawImages(canvas!!, numbers, !shapeToShow.hasTriangularFaces)
                        }
                    }
                    else {
                        renderTriangles(canvas!!)
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

    fun init(showImages: Boolean) {
        paint.apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2F
            textSize = 100F
        }
        loadShapeToShow()
        fillTrianglesToDraw()
        this.showImages = showImages
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

    // Painting functions

    private fun fillUpsideTriangle(triangle: Triangle2D, transformMatrix: Matrix3x3, bitmap: Bitmap, editedBitmap: Bitmap) {

        val currentPoint = Point2D(triangle.vertices[0].x, triangle.vertices[0].y)

        var leftBorder = currentPoint.x
        var rightBorder = currentPoint.x + 1F

        val maxWidth = bitmap.width - 1
        val maxHeight = bitmap.height - 1

        val rightDelta = (triangle.vertices[2].x - triangle.vertices[0].x) / (triangle.vertices[2].y - triangle.vertices[0].y)
        val leftDelta = (triangle.vertices[1].x - triangle.vertices[0].x) / (triangle.vertices[1].y - triangle.vertices[0].y)

        while (currentPoint.y < triangle.vertices[2].y) {

            while (currentPoint.x < rightBorder) {

                editedBitmap.setPixel(currentPoint.x.toInt(), currentPoint.y.toInt(), bitmap.getPixel(affineTransformation.clamp(currentPoint.getTransformedX(transformMatrix).toInt(), 0, maxWidth),
                    affineTransformation.clamp(currentPoint.getTransformedY(transformMatrix).toInt(), 0, maxHeight)))

                ++currentPoint.x
            }
            
            leftBorder += leftDelta
            rightBorder += rightDelta

            currentPoint.x = leftBorder

            ++currentPoint.y
        }
    }

    private fun fillDownsideTriangle(triangle: Triangle2D, transformMatrix: Matrix3x3, bitmap: Bitmap, editedBitmap: Bitmap) {

        val currentPoint = Point2D(triangle.vertices[0].x, triangle.vertices[0].y)

        val maxWidth = bitmap.width - 1
        val maxHeight = bitmap.height - 1

        var leftBorder = currentPoint.x
        var rightBorder = triangle.vertices[1].x

        val leftDelta = (triangle.vertices[2].x - triangle.vertices[0].x) / (triangle.vertices[2].y - triangle.vertices[0].y)
        val rightDelta = (triangle.vertices[2].x - triangle.vertices[1].x) / (triangle.vertices[2].y - triangle.vertices[1].y)

        while (currentPoint.y < triangle.vertices[2].y) {

            while (currentPoint.x < rightBorder) {

                editedBitmap.setPixel(currentPoint.x.toInt(), currentPoint.y.toInt(), bitmap.getPixel(affineTransformation.clamp(currentPoint.getTransformedX(transformMatrix).toInt(), 0, maxWidth),
                    affineTransformation.clamp(currentPoint.getTransformedY(transformMatrix).toInt(), 0, maxHeight)))

                ++currentPoint.x
            }

            leftBorder += leftDelta
            rightBorder += rightDelta

            currentPoint.x = leftBorder

            ++currentPoint.y
        }
    }

    private fun fillTriangleIntoBitmap(triangle: Triangle2D, transformMatrix: Matrix3x3, originalBitmap: Bitmap, editedBitmap: Bitmap) {

        paint.color = Color.valueOf(triangle.lightAmount, triangle.lightAmount, triangle.lightAmount).toArgb()

        triangle.vertices.sortBy { it.y }

        if (triangle.vertices[0].y == triangle.vertices[1].y) {
            fillDownsideTriangle(triangle, transformMatrix, originalBitmap, editedBitmap)
        }
        else if (triangle.vertices[1].y == triangle.vertices[2].y) {
            fillUpsideTriangle(triangle, transformMatrix, originalBitmap, editedBitmap)
        }
        else {

            val secondMiddlePoint = Point2D(
                triangle.vertices[0].x +
                        ((triangle.vertices[1].y - triangle.vertices[0].y) /
                                (triangle.vertices[2].y - triangle.vertices[0].y)) *
                        (triangle.vertices[2].x - triangle.vertices[0].x),
                triangle.vertices[1].y)

            if (secondMiddlePoint.x > triangle.vertices[1].x) {
                fillUpsideTriangle(
                    Triangle2D(mutableListOf(
                    triangle.vertices[0],
                    triangle.vertices[1],
                    secondMiddlePoint
                ), triangle.lightAmount), transformMatrix, originalBitmap, editedBitmap)
                fillDownsideTriangle(
                    Triangle2D(mutableListOf(
                    triangle.vertices[1],
                    secondMiddlePoint,
                    triangle.vertices[2]
                ), triangle.lightAmount), transformMatrix, originalBitmap, editedBitmap)
            }
            else {
                fillUpsideTriangle(
                    Triangle2D(mutableListOf(
                    triangle.vertices[0],
                    secondMiddlePoint,
                    triangle.vertices[1]
                ), triangle.lightAmount), transformMatrix, originalBitmap, editedBitmap)
                fillDownsideTriangle(
                    Triangle2D(mutableListOf(
                    secondMiddlePoint,
                    triangle.vertices[1],
                    triangle.vertices[2]
                ), triangle.lightAmount), transformMatrix, originalBitmap, editedBitmap)
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

    // Draw functions ---------------------------------------------------------------------
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
                val projectedTriangle = Triangle2D()

                // Calculating shadowing
                lightDirection.scaleByDivision(lightDirection.getVectorLength())
                projectedTriangle.lightAmount = dotProduct(normal, lightDirection)
                projectedTriangle.depth = triangleToProject.getCentroid().z
                projectedTriangle.imageId = triangleInd

                // Transforming from 3D to 2D

                projectedTriangle.changeProjectionFrom3DByMatrix(triangleToProject, projectionMatrix)

                // Applying scaling and centering for better view
                projectedTriangle.scale(shapeScale)
                trianglesToDraw.add(projectedTriangle)
            }
        }

//        For overlapping polygons
//        trianglesToDraw.sortBy { it.depth }
    }

    private fun renderTriangles(canvas: Canvas) {
        for (triangleInd in trianglesToDraw.size - 1 downTo 0) {

            val newTriangle = trianglesToDraw[triangleInd].getScaledLightCopy(2)
            newTriangle.center(width, height)

            drawTriangle(newTriangle,
                Color.valueOf(
                    newTriangle.lightAmount,
                    newTriangle.lightAmount,
                    newTriangle.lightAmount),
                canvas)
        }
    }

    private fun drawImages(canvas: Canvas, chosenImages: List<Int>, squared: Boolean = false) {

        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (triangle in trianglesToDraw) {

            val bitmap: Bitmap

            triangle.center(width, height)

            if (squared) {
                if (triangle.imageId % 2 == 0) {
                    bitmap = BitmapFactory.decodeResource(resources, chosenImages[triangle.imageId / 2])
                    triangleToDemonstrate.setToProjectImageTop(bitmap)
                }
                else {
                    bitmap = BitmapFactory.decodeResource(resources, chosenImages[(triangle.imageId - 1) / 2])
                    triangleToDemonstrate.setToProjectImageBottom(bitmap)
                    val ar = intArrayOf()
                }
            }
            else {
                bitmap = BitmapFactory.decodeResource(resources, chosenImages[triangle.imageId])
                triangleToDemonstrate.setToProjectWholeImage(bitmap)
            }

            val affineTransformMatrix = affineTransformation.getTranformationMatrixByTriangles(triangleToDemonstrate, triangle).getInvertedMatrix()

            fillTriangleIntoBitmap(triangle, affineTransformMatrix, bitmap, newBitmap)
        }

        canvas.drawBitmap(Bitmap.createScaledBitmap(newBitmap, width * 2, height * 2, false), -width.toFloat() / 2, -height.toFloat() / 2, paint)
    }

    // For touch handling
    private var lastX = 0F
    private var lastY = 0F
    private var currentScale = 0F
    private var lastScale = 0F
    private var currentZRotationVector = Point2D()
    private var lastZRotationVector = Point2D()
    var lastTouchCount = 0

    fun cancelTouchEvents(event: MotionEvent) {
        lastX = event.x
        lastY = event.y
        lastTouchCount = event.pointerCount

        if (event.pointerCount == 2) {
            lastScale = hypotenuse(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
            lastZRotationVector.setCoordinates(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
        }
    }

    fun actionMove(event: MotionEvent) {

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
                shapeScale = affineTransformation.clamp(shapeScale + (currentScale - lastScale), 100F, 800F)

                lastScale = currentScale

                currentZRotationVector.setCoordinates(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))

                shapeRotation.z -= tanBetweenTwoLines(currentZRotationVector, lastZRotationVector)
                lastZRotationVector.copyPointsFrom(currentZRotationVector)
            }
            5 -> {
                secretActivated = true
            }
            10 -> {
                secretActivated = false
            }
        }
    }

    private fun clearTheCanvas(canvas: Canvas, givenColor: Int = Color.TRANSPARENT) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
    }
}