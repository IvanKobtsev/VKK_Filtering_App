package com.tsu.vkkfilteringapp.spline

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class Matrix {

    fun findDeterminant(matrix: ArrayList<ArrayList<Float>>) : Double
    {
        var det = 0.0;
        var sign = 1.0;

        val size : Int = matrix.size


        // Base Case
        if (size == 1) {
            det = matrix[0][0].toDouble();
        } else if (size == 2) {
            det = ((matrix[0][0] * matrix[1][1]) -
                    (matrix[0][1] * matrix[1][0])).toDouble();
        }

        // Perform the Laplace Expansion
        else {
            for (i in 0 until size) {

                // Stores the cofactor matrix
                val cofactor:ArrayList<ArrayList<Float>> =  ArrayList()
                for (j in 0 until  size - 1) {
                    cofactor.add(ArrayList<Float>())
                    for (n in 0 until size - 1)
                        cofactor[j].add(0.0F)
                }

                var subI = 0
                var subJ = 0;
                for ( j in  1 until size) {
                    for (k in 0 until size) {
                        if (k == i) {
                            continue;
                        }
                        cofactor[subI][subJ] = matrix[j][k];
                        subJ++;
                    }
                    subI++;
                    subJ = 0;
                }

                // Update the determinant value
                det += sign * matrix[0][i] * findDeterminant(cofactor);
                sign = -sign;

            }
        }



        // Return the final determinant value
        return det;
    }

    fun transposedMatrix(matrix: ArrayList<ArrayList<Float>>): ArrayList<ArrayList<Float>> {

        val width: Int = matrix.size
        val height: Int = matrix[0].size

        val newMatrix: ArrayList<ArrayList<Float>> = ArrayList();
        for (j in 0 until height) {
            newMatrix.add(ArrayList<Float>())
            for (n in 0 until width)
                newMatrix[j].add(0.0F)
        }

        for (j in 0 until height)
            for (n in 0 until width)
                newMatrix[j][n] = matrix[n][j]

        return newMatrix

    }

    fun algebraicAdditionMatrix(matrix: ArrayList<ArrayList<Float>>): ArrayList<ArrayList<Float>>{

        val width: Int = matrix.size
        val height: Int = matrix[0].size

        val newMatrix: ArrayList<ArrayList<Float>> = ArrayList();
        for (j in 0 until height) {
            newMatrix.add(ArrayList<Float>())
            for (n in 0 until width)
                newMatrix[j].add(0.0F)
        }

        for(j in 0 until height) {
            for (n in 0 until width) {

                val tempMatrix: ArrayList<ArrayList<Float>> = ArrayList();
                var subI = 0
                var subJ = 0;
                for (h in 0 until height-1) {
                    tempMatrix.add(ArrayList<Float>())
                    for (w in 0 until width-1)
                        tempMatrix[h].add(0.0F)
                }

                for (h in 0 until height) {
                    if(h == j)continue
                    for (w in 0 until width) {
                        if(w == n)continue

                        tempMatrix[subI][subJ] = matrix[h][w]

                        subJ++
                    }
                    subJ = 0
                    subI++


                }

                newMatrix[j][n]= (findDeterminant(tempMatrix) * (-1.0).pow((n + j))).toFloat()
            }
        }

        return  newMatrix
    }

    fun multiplyByNumber(matrix: ArrayList<ArrayList<Float>>, number: Double): ArrayList<ArrayList<Float>> {
        var newMatrix = matrix;
        for (i in 0 until matrix.size)
            for (j in 0 until matrix[i].size)
                newMatrix[i][j] *= number.toFloat()

        return newMatrix;
    }

    fun createMatrix(height :Int ,width: Int): ArrayList<ArrayList<Float>> {
        val matrix: ArrayList<ArrayList<Float>> = ArrayList();
        for (j in 0 until height) {
            matrix.add(ArrayList<Float>())
            for (n in 0 until width)
                matrix[j].add(0.0F)
        }
        return matrix
    }

    fun createMatrix():ArrayList<ArrayList<Float>>{
        val `in` = Scanner(System.`in`)
        print("Input a height: ")
        val height: Int = `in`.nextInt()
        print("Input a width: ")
        val width: Int = `in`.nextInt()

        val matrix: ArrayList<ArrayList<Float>> = ArrayList()
        for (i in 0 until height) {
            matrix.add(ArrayList<Float>(height))

            for (j in 0 until width) {
                readlnOrNull()?.let { matrix[i].add(it.toFloat()) }
            }
        }

        return matrix
    }

    fun multiplyByMatrix(matrixA: ArrayList<ArrayList<Float>>, matrixB: ArrayList<ArrayList<Float>>): ArrayList<ArrayList<Float>>{
        var newMatrix = createMatrix(matrixA.size,matrixB[0].size)
        for(i in 0 until matrixA.size) {

            for (j in 0 until matrixB[0].size) {
                for (k in 0 until matrixB.size)
                    newMatrix[i][j] += matrixA[i][k] * matrixB[k][j]
            }
        }


        return newMatrix;
    }

    fun inverseMatrix(matrix: ArrayList<ArrayList<Float>>): ArrayList<ArrayList<Float>>{

        return multiplyByNumber(
            transposedMatrix(algebraicAdditionMatrix(matrix)),
            (1/findDeterminant(matrix))
        )
    }

    fun printMatrix2D(matrix: ArrayList<ArrayList<Float>>){
        for (iPrint in 0 until matrix.size) {
            for (jPrint in 0 until matrix[iPrint].size) {
                print("" + matrix[iPrint][jPrint] + "  ")
            }
            println()
        }
        println()
    }

    fun printArray(array: ArrayList<Float>){
        for(i in 0 until array.size)
            print("" + array[i] + "  ")
        println()
        println()
    }

    fun findSLAE(matrixLAE: ArrayList<ArrayList<Float>>, matrixAnswer:ArrayList<ArrayList<Float>>): ArrayList<ArrayList<Float>>{
        return multiplyByMatrix(inverseMatrix(matrixLAE),matrixAnswer)
    }

    fun coverArrayToMatrix(array: ArrayList<Float>): ArrayList<ArrayList<Float>>{
        var matrix = createMatrix(array.size,1)
        for(i in 0 until array.size)
            matrix[i][0] = array[i]
        return matrix
    }
}