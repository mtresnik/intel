package com.resnik.intel.svm

import com.resnik.math.linear.array.ArrayVector
import kotlin.math.exp
import kotlin.math.pow

class RadialBasisFunction(private val gamma: Double = 0.1) : Kernel {

    override fun apply(x1: ArrayVector, x2: ArrayVector): Double =
        exp(-gamma * (x1.values.indices.sumOf { x1[it] - x2[it].pow(2) }))

}