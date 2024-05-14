package com.tsu.vkkfilteringapp.graphics3d

class Line3D() {

    private var distance = Point3D()
    private var pStart = Point3D()
    private var pEnd = Point3D()

    constructor(start: Point3D, end: Point3D) : this() {
        setNewPoints(start, end)
    }

    fun setNewPoints(start: Point3D, end: Point3D) {

        pStart = start
        pEnd = end

        distance.x = pEnd.x - pStart.x
        distance.y = pEnd.y - pStart.y
        distance.z = pEnd.z - pStart.z
    }

    fun getDistance() : Point3D {
        return distance
    }

    fun translate(givenX: Float, givenY: Float, givenZ: Float) {
        pStart.translate(givenX, givenY, givenZ)
        pEnd.translate(givenX, givenY, givenZ)
    }

    fun getNormalToOtherLine(secondLine: Line3D) : Point3D {
        return Point3D(distance.y * secondLine.distance.z - distance.z * secondLine.distance.y,
                        distance.z * secondLine.distance.x - distance.x * secondLine.distance.z,
                        distance.x * secondLine.distance.y - distance.y * secondLine.distance.x)
    }



}