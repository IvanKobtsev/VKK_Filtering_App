package com.tsu.vkkfilteringapp

class Mesh(var triangles: MutableList<Triangle3D> = mutableListOf(), var hasTriangularFaces: Boolean = false) {

    fun translate(givenX: Float, givenY: Float, givenZ: Float) {
        for (mesh in triangles) {
            mesh.translate(givenX, givenY, givenZ)
        }
    }

    fun addTriangle(triangle: Triangle3D) {
        triangles.add(triangle)
    }

}