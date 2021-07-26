package com.resnik.intel.neural.conv

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor
import kotlin.math.exp

class SoftmaxLayer(val inputDims : ArrayDimension) : TensorLayer {

    lateinit var lastInput : ArrayTensor

    override fun forward(input: ArrayTensor): ArrayTensor {
        lastInput = input
        val output = input.clone{ exp(input.values[it])}
        val sum = output.values.sum()
        output.values.indices.forEach { output.values[it] = output.values[it] / sum }
        return output
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor {
        val derivative = lastInput.clone{ exp(lastInput.values[it])}
        val sum = derivative.values.sum()
        derivative.values.indices.forEach { derivative.values[it] *= (sum - derivative.values[it]) }
        return outputGrads * derivative
    }

    override fun inputDimensions(): ArrayDimension = inputDims

    override fun outputDimensions(): ArrayDimension = inputDims

}