package com.tsu.vkkfilteringapp.graphics2d

import com.tsu.vkkfilteringapp.matrices.Matrix3x3
import kotlin.math.pow
import kotlin.math.sqrt

class Point2D(var x: Float = 0F, var y: Float = 0F) {

    fun translate(givenX: Float, givenY: Float) {
        x += givenX
        y += givenY
    }

    fun transformByMatrix(transformMatrix: Matrix3x3) {
        val oldX = x
        val oldY = y

        x = oldX * transformMatrix.rows[0][0] + oldY * transformMatrix.rows[0][1] + transformMatrix.rows[0][2]
        y = oldX * transformMatrix.rows[1][0] + oldY * transformMatrix.rows[1][1] + transformMatrix.rows[1][2]
    }

    fun getTransformedX(transformMatrix: Matrix3x3) : Float {
        return x * transformMatrix.rows[0][0] + y * transformMatrix.rows[0][1] + transformMatrix.rows[0][2]
    }

    fun getTransformedY(transformMatrix: Matrix3x3) : Float {
        return x * transformMatrix.rows[1][0] + y * transformMatrix.rows[1][1] + transformMatrix.rows[1][2]
    }

    fun magnitude() : Float {
        return sqrt( x.pow(2) + y.pow(2))
    }

    fun setCoordinates(givenX: Float, givenY: Float) {
        x = givenX
        y = givenY
    }

    fun copyPointsFrom(pointToCopy: Point2D) {
        x = pointToCopy.x
        y = pointToCopy.y
    }
}