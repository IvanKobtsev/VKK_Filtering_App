package com.tsu.vkkfilteringapp.graphics3d

import com.tsu.vkkfilteringapp.matrices.Matrix4x4
import kotlin.math.sqrt

class Point3D(var x: Float = 0F, var y: Float = 0F, var z: Float = 0F) {

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

    fun getCopy(): Point3D {
        var newPoint = Point3D(x, y, z)
        return newPoint
    }

    fun scaleByDivision(devisor: Float) {
        x /= devisor
        y /= devisor
        z /= devisor
    }

    fun getVectorLength() : Float {
        return sqrt(x * x + y * y + z * z)
    }

}