package com.tsu.vkkfilteringapp

class Point2D(var x: Float = 0F, var y: Float = 0F) {

    fun translate(givenX: Float, givenY: Float) {
        x += givenX
        y += givenY
    }

}