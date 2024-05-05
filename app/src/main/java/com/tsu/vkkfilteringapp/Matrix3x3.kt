package com.tsu.vkkfilteringapp

import android.util.Log
import java.lang.Math.pow
import kotlin.math.pow

class Matrix3x3(var rows: MutableList<MutableList<Float>> =
    mutableListOf(
        mutableListOf(0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F),
        mutableListOf(0F, 0F, 0F)
    )) {

    constructor(triangle: Triangle2D) : this() {
        rows = mutableListOf(
            mutableListOf(triangle.vertices[0].x, triangle.vertices[1].x, triangle.vertices[2].x),
            mutableListOf(triangle.vertices[0].y, triangle.vertices[1].y, triangle.vertices[2].y),
            mutableListOf(1F, 1F, 1F)
        )
    }

    fun getDet(det: Matrix3x3 = this) : Float {

        val sum = det.rows[0][0] * (det.rows[1][1] * det.rows[2][2] - det.rows[1][2] * det.rows[2][1]) -
                det.rows[0][1] * (det.rows[1][0] * det.rows[2][2] - det.rows[1][2] * det.rows[2][0]) +
                det.rows[0][2] * (det.rows[1][0] * det.rows[2][1] - det.rows[1][1] * det.rows[2][0])

        return sum
    }

    fun getAlgComp(y: Int, x: Int) : Float {

        var det = mutableListOf(0F, 0F, 0F, 0F)
        var ind = 0
        var oneCoefficient: Float = (-1F).pow(y + x)

        for (yi in 0..2) {
            for (xi in 0..2) {
                if (xi != x && yi != y) {
                    det[ind++] = rows[yi][xi]
                }
            }
        }

        return (det[0] * det[3] - det[1] * det[2]) * oneCoefficient
    }

    fun transpose() {

        for (yi in 0..1) {
            for (xi in yi + 1 ..2) {
                rows[yi][xi] = rows[xi][yi].also { rows[xi][yi] = rows[yi][xi] }
            }
        }

    }

    fun divide(divisor: Float) {

        for (yi in 0..2) {
            for (xi in 0..2) {
                rows[yi][xi] /= divisor
            }
        }
    }

    fun getInvertedMatrix() : Matrix3x3 {

        var invertedMatrix = Matrix3x3()

        var det = getDet()

        if (det != 0F) {

            for (yi in 0..2) {
                for (xi in 0..2) {
                    invertedMatrix.rows[yi][xi] = getAlgComp(yi, xi)
                }
            }

            invertedMatrix.transpose()
            invertedMatrix.divide(det)
        }

        return invertedMatrix
    }

}