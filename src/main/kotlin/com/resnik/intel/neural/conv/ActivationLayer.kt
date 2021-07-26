package com.resnik.intel.neural.conv

import com.resnik.intel.neural.TransferFunction
import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor

open class ActivationLayer(val inputDimensions : ArrayDimension, val transferFunction: TransferFunction) : TensorLayer {

    lateinit var recentInput : ArrayTensor

    override fun forward(input: ArrayTensor): ArrayTensor {
        recentInput = input
        return ArrayTensor(input.dim) { transferFunction.activate(input.values[it]) }
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor =
            outputGrads * ArrayTensor(recentInput.dim) { transferFunction.derivative(recentInput.values[it]) }

    override fun inputDimensions(): ArrayDimension = inputDimensions

    override fun outputDimensions(): ArrayDimension = inputDimensions

}