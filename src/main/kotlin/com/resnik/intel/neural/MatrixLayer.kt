package com.resnik.intel.neural

import com.resnik.math.linear.array.ArrayMatrix

interface MatrixLayer {

    fun forward(inputs: List<ArrayMatrix>): List<ArrayMatrix>

    fun backward(errors: List<ArrayMatrix>, learningRate: Double): List<ArrayMatrix>

}