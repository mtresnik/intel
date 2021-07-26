package com.resnik.intel.neural.conv

import com.resnik.math.*
import com.resnik.math.linear.array.ArrayTensor
import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensorRegionValueIterator
import com.resnik.math.linear.array.ArrayTensorValueIterator
import java.lang.IllegalStateException
import java.util.*
import java.util.stream.IntStream
import kotlin.math.sqrt

class ConvolutionalLayer(val numKernels: Int, kernalDims : IntArray, val inputDims : IntArray) : TensorLayer {

    var kernels : ArrayTensor
    val kernelDims : IntArray
    val crossCorrelationMapSizes : IntArray
    val outputDims : IntArray
    lateinit var lastInput : ArrayTensor

    init {
        this.kernelDims = IntArray(inputDims.size){1}
        kernalDims.indices.forEach { this.kernelDims[it] = kernalDims[it] }
        crossCorrelationMapSizes = IntArray(inputDims.size){inputDims[it] - this.kernelDims[it] + 1}
        val filterDims = this.kernelDims.toMutableList()
        filterDims.add(numKernels)
        val randLimit = sqrt(2.0) / inputDims.product()
        val random = Random()
        this.kernels = ArrayTensor(ArrayDimension(*filterDims.toIntArray())) { random.nextGaussian() * randLimit }
        val crossCorrelationMapList = crossCorrelationMapSizes.toMutableList()
        crossCorrelationMapList.add(numKernels)
        outputDims = crossCorrelationMapList.toIntArray()
    }

    override fun forward(input: ArrayTensor) : ArrayTensor {
        lastInput = input
        var output : ArrayTensor? = null
        val kernelIterator = kernels.regionIterator(kernelDims, IntArray(0))
        while(kernelIterator.hasNext()){
            val newMap = convolve(input, kernelIterator.next(), crossCorrelationMapSizes)
            if(output == null){
                output = newMap
            }else{
                output.append(newMap, kernels.dim.size())
            }
        }
        if(output == null){
            throw IllegalStateException("Output cannot be null")
        }
        return output
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor {
        var kernelGrads : ArrayTensor? = null
        var inputGrads = lastInput.clone { 0.0 }

        val outputGradSize = outputGrads.dim.stripLast().values
        val outputIterator = outputGrads.regionIterator(outputGradSize)
        val kernelIterator = kernels.regionIterator(kernelDims)

        while(outputIterator.hasNext() && kernelIterator.hasNext()){
            val outputLayer = outputIterator.next()
            val kernelLayer = kernelIterator.next()
            val newMap = convolve(lastInput, outputLayer, kernelDims)
            if(kernelGrads == null){
                kernelGrads = newMap
            }else{
                kernelGrads = kernelGrads.append(newMap, outputGrads.dim.size())
            }
            val padding = IntArray(kernelLayer.dim.size()){kernelLayer.dim[it] - 1}
            val flippedKernel = kernelLayer.flip()
            val inputGrad = convolve(outputLayer, flippedKernel, inputGrads.dim.values, padding)
            inputGrads += inputGrad
        }
        if(kernelGrads == null){
            throw IllegalStateException("Kernel gradients cannot be null")
        }
        kernels = kernels.minus(kernelGrads, learningRate)
        return inputGrads
    }

    override fun inputDimensions(): ArrayDimension =
        ArrayDimension(*inputDims)

    override fun outputDimensions(): ArrayDimension =
        ArrayDimension(*outputDims)

    companion object{

        @JvmStatic
        fun convolve(base : ArrayTensor, kernel : ArrayTensor, ccMapSize : IntArray, padding : IntArray = IntArray(0)) : ArrayTensor {
            val ccMapValues = DoubleArray(ccMapSize.product()){0.0}
            val regions = mutableListOf<ArrayTensorValueIterator>()
            ArrayTensorRegionValueIterator(
                base,
                kernel.dim.values,
                padding
            ).forEachRemaining {
                regions.add(it)
            }
            IntStream.range(0, ccMapValues.size).parallel().forEach {
                ccMapValues[it] = regions[it].innerProduct(kernel)
            }
            return ArrayTensor(ArrayDimension(*ccMapSize)) { ccMapValues[it] }
        }

    }


}