package com.resnik.intel.neural

import com.resnik.intel.Model
import com.resnik.intel.neural.TransferFunction.Companion.sigmoid
import com.resnik.math.linear.array.ArrayMatrix
import com.resnik.math.linear.array.ArrayVector
import kotlin.math.pow

class FeedForwardNeuralNetwork : Model {

    val inputLayer: ArrayVector
    val layers: Array<ArrayVector>
    val outputLayer: ArrayVector

    val biases: ArrayVector
    val dBiases: ArrayVector

    val weights: Array<ArrayMatrix>
    val dWeights: Array<ArrayMatrix>

    val transferFunction: TransferFunction
    var learningRate: Double = 0.1
    var momentum: Double = 0.9

    constructor(
        vararg layerSizes: Int,
        biases: ArrayVector = ArrayVector(layerSizes.size, 0.0),
        transferFunction: TransferFunction = sigmoid
    ) {
        if (layerSizes.size < 2) {
            throw IllegalArgumentException("Must have at least input and output layer.")
        }
        if (biases.size() != layerSizes.size) {
            throw IllegalArgumentException("Must have equal number of biases: ${biases.size()} to layers: ${layerSizes.size}.")
        }
        this.layers = layerSizes.map { ArrayVector(it) }.toTypedArray()
        this.inputLayer = this.layers.first()
        this.outputLayer = this.layers.last()
        // Initialize to random weights
        this.weights =
            Array(this.layers.size - 1) { ArrayMatrix(layers[it + 1].size(), layers[it].size()) { Math.random() } }
        this.dWeights = Array(this.layers.size - 1) { ArrayMatrix(layers[it + 1].size(), layers[it].size()) { 0.0 } }
        this.transferFunction = transferFunction
        this.biases = biases
        this.dBiases = ArrayVector(biases.size(), 0.0)
    }

    override fun train(input: ArrayVector, expected: ArrayVector): Double {
        predict(input)
        backprop(expected)
        predict(input)
        return error(expected)
    }

    fun backprop(expected: ArrayVector) {
        val sigmas: Array<ArrayVector> = Array(layers.size) { ArrayVector(layers[it].size(), 0.0) }
        // Sigma_L = (x_L - expected) o f'(W_L*x_L-1)
        val sigmaL = (outputLayer - expected).toColMatrix()
            .hadamard((weights.last() * layers[layers.lastIndex - 1].toColMatrix()).apply(transferFunction::derivative))
            .toVector()!!
        sigmas[sigmas.lastIndex] = sigmaL
        // Sigma_i = W_i+1^T * sigma_i+1 o f'(W_i * x_i-1)
        (sigmas.lastIndex - 1 downTo 1).forEach { index ->
            sigmas[index] = (weights[index].transpose() * sigmas[index + 1].toColMatrix()).hadamard(
                (weights[index - 1] * layers[index - 1].toColMatrix() + biases[index]).apply(transferFunction::derivative)
            ).toVector()!!
        }
        (0 until sigmas.lastIndex - 1).forEach { index ->
            val currLayer = layers[index].toColMatrix()
            val currSigma = sigmas[index + 1].toColMatrix()
            val dW = currSigma * currLayer.transpose() * learningRate + dWeights[index] * momentum
            weights[index] -= dW
            dWeights[index] = dW
            val dB = sigmas[index + 1].sum() * learningRate + dBiases[index] * momentum
            biases[index] -= dB
            dBiases[index] = dB
        }

    }

    fun error(expected: ArrayVector): Double = outputLayer.values.zip(expected.values) { a, b -> (a - b).pow(2) }.sum()

    override fun predict(input: ArrayVector): ArrayVector {
        inputLayer.setFrom(input)
        weights.indices.forEach { index ->
            layers[index + 1].setFrom(
                (((weights[index] * layers[index].toColMatrix()) + biases[index + 1])
                    .apply(transferFunction::activate))
                    .columnVectors()[0]
            )
        }
        return outputLayer
    }

}