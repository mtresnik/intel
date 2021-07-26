package com.resnik.intel.neural.conv

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor

class InverseLayer(val other: TensorLayer) : TensorLayer {

    var lastLearningRate = 0.01

    override fun forward(input: ArrayTensor): ArrayTensor {
        return other.backward(input, lastLearningRate)
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor {
        lastLearningRate = learningRate
        return other.forward(outputGrads)
    }

    override fun inputDimensions(): ArrayDimension = other.outputDimensions()

    override fun outputDimensions(): ArrayDimension = other.inputDimensions()
}