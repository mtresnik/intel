package com.resnik.intel.neural.conv

import com.resnik.math.linear.array.ArrayDimension
import com.resnik.math.linear.array.ArrayTensor

class TensorNetwork : TensorLayer {

    val layers : MutableList<TensorLayer> = mutableListOf()
    lateinit var inputDimensions : ArrayDimension
    var initialized = false

    constructor(inputDimensions : ArrayDimension){
        this.inputDimensions = inputDimensions
        initialized = true
    }

    constructor(){}

    fun add(layer : TensorLayer) : TensorNetwork {
        layers.add(layer)
        if(!initialized){
            this.inputDimensions = layer.inputDimensions()
            initialized = true
        }
        return this
    }

    override fun forward(input: ArrayTensor): ArrayTensor {
        var out : ArrayTensor = input
        layers.forEach {
            println("Forward")
            out = it.forward(out)
        }
        return out
    }

    override fun backward(outputGrads: ArrayTensor, learningRate: Double): ArrayTensor {
        var inputGradient = outputGrads
        layers.indices.reversed().forEach {
            inputGradient = layers[it].backward(inputGradient, learningRate)
        }
        return inputGradient
    }

    override fun inputDimensions(): ArrayDimension = inputDimensions

    override fun outputDimensions(): ArrayDimension = if(layers.isEmpty()) inputDimensions else layers.last().outputDimensions()

}