package com.resnik.intel.neural.conv

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor
import com.resnik.math.linear.array.ArrayTensorIndex
import kotlin.math.ceil

class MaxPoolLayer(strides: IntArray, sizes: IntArray, val expectedInputDims: IntArray) : TensorLayer {

    val strides: IntArray = IntArray(expectedInputDims.size) { if (it < strides.size) strides[it] else 1 }
    val sizes: IntArray = IntArray(expectedInputDims.size) { if (it < sizes.size) sizes[it] else 1 }
    val inputTensor: ArrayTensor = ArrayTensor(ArrayDimension(*expectedInputDims)) { 0.0 }
    val outputDims: IntArray =
        IntArray(expectedInputDims.size) { (ceil((expectedInputDims[it] - this.sizes[it] + 1).toDouble() / this.strides[it])).toInt() }
    val outputTensor: ArrayTensor = ArrayTensor(ArrayDimension(*outputDims)) { 0.0 }
    val maxIndices: IntArray = IntArray(outputTensor.values.size) { 0 }

    override fun forward(input: ArrayTensor): ArrayTensor {
        val regionIterator = input.regionIterator(sizes, IntArray(0), this.strides)
        while (regionIterator.hasNext()) {
            val region = regionIterator.next()
            val maxIndex = region.maxIndex()
            maxIndices[regionIterator.numTraversed() - 1] = maxIndex
            outputTensor.values[regionIterator.numTraversed() - 1] = region.values[maxIndex]

        }
        return outputTensor
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor {
        val inputGrads = inputTensor.clone { 0.0 }
        val regionIterator = inputGrads.regionIterator(sizes, IntArray(0), this.strides)
        while (regionIterator.hasNext()) {
            regionIterator.next()
            val maxIndex = maxIndices[regionIterator.numTraversed() - 1]
            val maxCoords = ArrayTensorIndex.fromInt(
                maxIndex,
                ArrayDimension(*sizes)
            )
            inputGrads[ArrayTensorIndex(*(regionIterator.coords() + maxCoords.values))] =
                outputGrads.values[regionIterator.numTraversed() - 1]
        }
        return inputGrads
    }

    override fun inputDimensions(): ArrayDimension = inputTensor.dim

    override fun outputDimensions(): ArrayDimension = outputTensor.dim


}