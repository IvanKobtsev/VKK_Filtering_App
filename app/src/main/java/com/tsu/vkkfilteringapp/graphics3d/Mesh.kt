package com.tsu.vkkfilteringapp.graphics3d

import com.tsu.vkkfilteringapp.graphics3d.Triangle3D

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