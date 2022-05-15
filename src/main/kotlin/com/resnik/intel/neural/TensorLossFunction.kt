package com.resnik.intel.neural

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor
import com.resnik.math.linear.array.ArrayVector
import kotlin.math.ln

interface TensorLossFunction {

    fun loss(expected: DoubleArray, actual: DoubleArray): ArrayTensor

    fun lossDerivative(expected: DoubleArray, actual: DoubleArray): ArrayTensor

    fun loss(expected: ArrayVector, actual: ArrayVector): ArrayTensor = loss(expected.values, actual.values)

    fun loss(expected: ArrayTensor, actual: ArrayTensor): ArrayTensor = loss(expected.values, actual.values)

    fun lossDerivative(expected: ArrayVector, actual: ArrayVector): ArrayTensor =
        lossDerivative(expected.values, actual.values)

    fun lossDerivative(expected: ArrayTensor, actual: ArrayTensor): ArrayTensor =
        lossDerivative(expected.values, actual.values)

    companion object {

        val crossEntropy = object : TensorLossFunction {
            override fun loss(expected: DoubleArray, actual: DoubleArray): ArrayTensor =
                ArrayTensor(ArrayDimension(expected.size)) {
                    if (expected[it] == 1.0) {
                        -1.0 * ln(actual[it])
                    } else {
                        -1.0 * ln(1 - actual[it])
                    }
                }

            override fun lossDerivative(expected: DoubleArray, actual: DoubleArray): ArrayTensor =
                ArrayTensor(ArrayDimension(expected.size)) {
                    if (expected[it] == 1.0) {
                        -1.0 / actual[it]
                    } else {
                        -1.0 / (1 - actual[it])
                    }
                }
        }

    }
}