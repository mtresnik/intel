package com.resnik.intel.neural.conv

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor
import com.resnik.math.linear.array.ArrayTensorCoordIterator
import com.resnik.math.product
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.sqrt

class ConnectedLayer(val outputLength : Int, val inputDimSizes : IntArray) : TensorLayer {

    var lastInput : ArrayTensor = ArrayTensor(ArrayDimension(*inputDimSizes))
    var recentOutput : ArrayTensor = ArrayTensor(ArrayDimension(outputLength))
    var outputDims : ArrayDimension =
        ArrayDimension(outputLength)
    val weightDims : ArrayDimension =
        ArrayDimension(
            *IntArray(inputDimSizes.size + 1) { if (it < inputDimSizes.size) inputDimSizes[it] else outputLength }
        )
    var weights : ArrayTensor = ArrayTensor(weightDims) { RANDOM.nextGaussian() * sqrt(2.0) / sqrt(inputDimSizes.product().toDouble()) }
    var biases : ArrayTensor = ArrayTensor(ArrayDimension(outputLength))

    override fun forward(input: ArrayTensor): ArrayTensor {
        lastInput = input
        val regionIterator = weights.regionIterator(inputDimSizes)
        while(regionIterator.hasNext()){
            val region = regionIterator.next()
            println("Region Dim: ${region.dim} \t Weight Dim: ${weights.dim} \t Input Dim: ${input.dim}")
            val valueIndex = regionIterator.numTraversed()
            try {
                recentOutput.values[valueIndex] = region.innerProduct(input)
            } catch (ex : IllegalArgumentException) {
                if(region.dim.stripLast() == input.dim) {
                    println("Same up to last dim")
                    val tempTensor = ArrayTensor(region.dim.stripLast()){ index -> region.values[index]}
                    recentOutput.values[valueIndex] = tempTensor.innerProduct(input)
                }
            }
        }
        recentOutput += biases
        return recentOutput.clone()
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor {
        var weightGradient : ArrayTensor? = null
        outputGrads.values.indices.forEach {
            val newGrads = lastInput * outputGrads.values[it]
            weightGradient = if(weightGradient == null) newGrads else weightGradient!!.append(newGrads, weights.dim.size())
        }
        var inputGradient = ArrayTensor(ArrayDimension(*inputDimSizes))
        val regionIterator = weights.regionIterator(inputDimSizes)
        while(regionIterator.hasNext()){
            inputGradient += regionIterator.next() * outputGrads.values[regionIterator.numTraversed() - 1]
        }
        weights -= weightGradient!! * learningRate
        biases -= outputGrads * learningRate
        return inputGradient
    }

    override fun inputDimensions(): ArrayDimension =
        ArrayDimension(*inputDimSizes)

    override fun outputDimensions(): ArrayDimension = outputDims

    companion object {
        val RANDOM = Random()
    }

}