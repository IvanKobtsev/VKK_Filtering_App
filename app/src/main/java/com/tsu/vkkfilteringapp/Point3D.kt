package com.tsu.vkkfilteringapp

class Point3D(var x: Float, var y: Float, var z: Float) {

    fun translate(givenX: Float, givenY: Float, givenZ: Float) {
        x += givenX
        y += givenY
        z += givenZ
    }

    fun projectWithMatrix(matrix: Matrix4x4) {
        val oldX = x
        val oldY = y
        val oldZ = z

        x = oldX * matrix.rows[0][0] + oldY * matrix.rows[1][0] + oldZ * matrix.rows[2][0] + matrix.rows[3][0]
        y = oldX * matrix.rows[0][1] + oldY * matrix.rows[1][1] + oldZ * matrix.rows[2][1] + matrix.rows[3][1]
        z = oldX * matrix.rows[0][2] + oldY * matrix.rows[1][2] + oldZ * matrix.rows[2][2] + matrix.rows[3][2]
        val w = oldX * matrix.rows[0][3] + oldY * matrix.rows[1][3] + oldZ * matrix.rows[2][3] + matrix.rows[3][3]

        if (w != 0F) {
            x /= w
            y /= w
            z /= w
        }
    }

}