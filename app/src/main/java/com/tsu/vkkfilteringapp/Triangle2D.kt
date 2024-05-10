package com.tsu.vkkfilteringapp

import android.graphics.Bitmap

class Triangle2D(var vertices: MutableList<Point2D> = mutableListOf(Point2D(), Point2D(), Point2D()), var depth: Float = 0F, var lightAmount: Float = 0F, var imageId: Int = 0) {

    constructor(matrix: Matrix3x3) : this() {
        vertices = mutableListOf(
            Point2D(matrix.rows[0][0], matrix.rows[1][0]),
            Point2D(matrix.rows[0][1], matrix.rows[1][1]),
            Point2D(matrix.rows[0][2], matrix.rows[1][2])
        )
    }

    fun changeProjectionFrom3DByMatrix(origTriangle: Triangle3D, matrix: Matrix4x4) {

        for (vertexInd in 0..2) {

            vertices[vertexInd].x = origTriangle.vertices[vertexInd].x * matrix.rows[0][0] + origTriangle.vertices[vertexInd].y * matrix.rows[1][0] + origTriangle.vertices[vertexInd].z * matrix.rows[2][0] + matrix.rows[3][0]
            vertices[vertexInd].y = origTriangle.vertices[vertexInd].x * matrix.rows[0][1] + origTriangle.vertices[vertexInd].y * matrix.rows[1][1] + origTriangle.vertices[vertexInd].z * matrix.rows[2][1] + matrix.rows[3][1]
            val w = origTriangle.vertices[vertexInd].x * matrix.rows[0][3] + origTriangle.vertices[vertexInd].y * matrix.rows[1][3] + origTriangle.vertices[vertexInd].z * matrix.rows[2][3] + matrix.rows[3][3]

            if (w != 0F) {
                vertices[vertexInd].x /= w
                vertices[vertexInd].y /= w
            }
        }
    }

    fun scale(size: Float) {

        for (vertexInd in 0..2) {

            vertices[vertexInd].x += 0.001F
            vertices[vertexInd].y += 0.001F

            vertices[vertexInd].x *= size
            vertices[vertexInd].y *= size

        }
    }

    fun center(width: Int, height: Int) {

        for (vertexInd in 0..2) {
            vertices[vertexInd].translate(width / 2F, height / 2F)
        }
    }

    fun setToProjectImageBottom(bitmap: Bitmap) {
        vertices[0].x = 0F
        vertices[0].y = 0F
        vertices[1].x = 0F
        vertices[1].y = bitmap.height.toFloat()
        vertices[2].x = bitmap.width.toFloat()
        vertices[2].y = bitmap.height.toFloat()
    }

    fun setToProjectImageTop(bitmap: Bitmap) {
        vertices[0].x = bitmap.width.toFloat()
        vertices[0].y = 0F
        vertices[1].x = 0F
        vertices[1].y = 0F
        vertices[2].x = bitmap.width.toFloat()
        vertices[2].y = bitmap.height.toFloat()
    }

    fun setToProjectWholeImage(bitmap: Bitmap) {
        vertices[2].x = 0F
        vertices[2].y = bitmap.height.toFloat()
        vertices[1].x = bitmap.width.toFloat() / 2F
        vertices[1].y = 0F
        vertices[0].x = bitmap.width.toFloat()
        vertices[0].y = bitmap.height.toFloat()
    }

    fun getCentroid() : Point2D {
        return Point2D(
            (vertices[0].x + vertices[1].x + vertices[2].x) / 3,
            (vertices[0].y + vertices[1].y + vertices[2].y) / 3)
    }

}