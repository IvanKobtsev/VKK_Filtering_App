package com.tsu.vkkfilteringapp

class Triangle2D(var vertices: MutableList<Point2D> = mutableListOf(Point2D(), Point2D(), Point2D()), var depth: Float = 0F, var lightAmount: Float = 0F) {

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

}