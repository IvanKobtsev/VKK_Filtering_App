package com.tsu.vkkfilteringapp

class SeekbarData(var currentValue: Int, private val minValue: Int, private val maxValue: Int, private val showDivider: Float = 1F) {

    fun getMax() : Int {
        return maxValue - minValue
    }

    fun getFloatValue() : Float {
        return (currentValue + minValue) / showDivider
    }

    fun getTextMax() : String {
        return (maxValue / showDivider).toString()
    }

    fun getTextMin() : String {
        return (minValue / showDivider).toString()
    }

}