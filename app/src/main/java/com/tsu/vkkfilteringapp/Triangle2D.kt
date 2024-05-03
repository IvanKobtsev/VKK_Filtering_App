package com.tsu.vkkfilteringapp

class Triangle2D(var vertices: MutableList<Point2D>) {

    fun changeProjectionFrom3DByMatrix(origTriangle: Triangle3D, matrix: Matrix4x4) {

        for (vertexInd in 0..2) {

            vertices[vertexInd].x = origTriangle.vertices[vertexInd].x * matrix.rows[0][0] + origTriangle.vertices[vertexInd].y * matrix.rows[1][0] + origTriangle.vertices[vertexInd].z * matrix.rows[2][0] + matrix.rows[3][0]
            vertices[vertexInd].y = origTriangle.vertices[vertexInd].x * matrix.rows[0][1] + origTriangle.vertices[vertexInd].y * matrix.rows[1][1] + origTriangle.vertices[vertexInd].z * matrix.rows[2][1] + matrix.rows[3][1]
            //z = origVertex.x * matrix.rows[0][2] + oldY * matrix.rows[1][2] + oldZ * matrix.rows[2][2] + matrix.rows[3][2]
            val w = origTriangle.vertices[vertexInd].x * matrix.rows[0][3] + origTriangle.vertices[vertexInd].y * matrix.rows[1][3] + origTriangle.vertices[vertexInd].z * matrix.rows[2][3] + matrix.rows[3][3]

            if (w != 0F) {
                vertices[vertexInd].x /= w
                vertices[vertexInd].y /= w
                //z /= w
            }

        }

    }

    fun scale(size: Float, width: Int, height: Int) {

        for (vertexInd in 0..2) {

            vertices[vertexInd].x += 1F
            vertices[vertexInd].y += 1F

            vertices[vertexInd].x *= size * width
            vertices[vertexInd].y *= size * height

        }

    }

}