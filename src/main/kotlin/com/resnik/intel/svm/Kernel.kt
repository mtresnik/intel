package com.resnik.intel.svm

import com.resnik.math.linear.array.ArrayVector

@FunctionalInterface
interface Kernel {

    fun apply(x1: ArrayVector, x2: ArrayVector): Double

}