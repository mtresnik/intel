package com.resnik.intel.neural.conv

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor

interface TensorLayer {

    fun forward(input: ArrayTensor): ArrayTensor

    fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor

    fun inputDimensions(): ArrayDimension

    fun outputDimensions(): ArrayDimension

    fun inverse(): InverseLayer = InverseLayer(this)

}