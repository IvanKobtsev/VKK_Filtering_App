package com.tsu.vkkfilteringapp

class Triangle3D(var vertices: MutableList<Point3D>) {

    fun translate(givenX: Float, givenY: Float, givenZ: Float) {
        for (vertex in vertices) {
            vertex.translate(givenX, givenY, givenZ)
        }
    }

    fun rotateByMatrix(matrix: Matrix4x4) {

        for (vertexInd in 0..2) {

            val oldX = vertices[vertexInd].x
            val oldY = vertices[vertexInd].y
            val oldZ = vertices[vertexInd].z

            vertices[vertexInd].x = oldX * matrix.rows[0][0] + oldY * matrix.rows[1][0] + oldZ * matrix.rows[2][0] + matrix.rows[3][0]
            vertices[vertexInd].y = oldX * matrix.rows[0][1] + oldY * matrix.rows[1][1] + oldZ * matrix.rows[2][1] + matrix.rows[3][1]
            vertices[vertexInd].z = oldX * matrix.rows[0][2] + oldY * matrix.rows[1][2] + oldZ * matrix.rows[2][2] + matrix.rows[3][2]
            val w = oldX * matrix.rows[0][3] + oldY * matrix.rows[1][3] + oldZ * matrix.rows[2][3] + matrix.rows[3][3]

            if (w != 0F) {
                vertices[vertexInd].x /= w
                vertices[vertexInd].y /= w
                vertices[vertexInd].z /= w
            }

        }

    }

}