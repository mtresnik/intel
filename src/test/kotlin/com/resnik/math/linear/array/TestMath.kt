package com.resnik.math.linear.array

import com.resnik.math.linear.array.ArrayMatrix
import com.resnik.math.linear.array.ArrayVector
import org.junit.Test

class TestMath {

    @Test
    fun testVector1(){
        val vec1 = ArrayVector(0.0, 1.0, 2.0)
        val vec2 = ArrayVector(3.0, 1.0, 0.0)
        println(vec1)
        println(vec2)
        println(vec1 + vec2)
        println(vec1 * vec2)
    }

    @Test
    fun testMatrix(){
        val mat1 = ArrayMatrix(5, 5) { it.toDouble() }
        val mat2 = ArrayMatrix(5, 5) { it.toDouble() }
        println(mat1)
        println(mat2)
        println(mat1 + mat2)

        val mat3 = ArrayMatrix(arrayOf(doubleArrayOf(2.0, 2.0), doubleArrayOf(2.0, 3.0), doubleArrayOf(5.0, 1.0)))
        println(mat3.dimString())
        val mat4 = ArrayMatrix(arrayOf(doubleArrayOf(-1.0, 3.0, 4.0), doubleArrayOf(5.0, 5.0, 6.0)))
        println(mat4.dimString())
        println(mat3 * mat4)
    }

    @Test
    fun testVecMatrix(){
        val mat1 = ArrayMatrix(3, 3) { 3.0 }
        val vec1 = ArrayVector(0.0, 1.0, 2.0)
        println(mat1)
        println(vec1.toColMatrix())
        println((mat1 * vec1.toColMatrix()))
        println((mat1 * vec1.toColMatrix()).dimString())
    }

    @Test
    fun testApplyMatrix(){
        var mat1 = ArrayMatrix(3, 3) { 3.0 }
        mat1 *= 3.0
        println(mat1)
    }

}