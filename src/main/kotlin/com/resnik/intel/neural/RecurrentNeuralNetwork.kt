package com.resnik.intel.neural

import com.resnik.intel.neural.TransferFunction.Companion.sigmoid
import com.resnik.math.linear.array.ArrayMatrix
import com.resnik.math.linear.array.ArrayVector

class RecurrentNeuralNetwork(val inputSize : Int, val outputSize: Int, val recurrences: Int, val expectedOutput : Array<ArrayVector>, val learningRate: Double) {

    var input = ArrayVector(inputSize, 0.0)
    var weights = ArrayMatrix(outputSize, outputSize) { Math.random() }
    var learningRateDecay = ArrayMatrix(outputSize, outputSize) { 0.0 }
    val inputArray =                Array<ArrayVector>(recurrences + 1){ ArrayVector(inputSize, 0.0) }
    val cellStateArray =            Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val outputArray =               Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val hiddenStateArray =          Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val forgetArray =               Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val inputGateArray =            Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val cellStateMatArray =         Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val outputGateArray =           Array<ArrayVector>(recurrences + 1){ ArrayVector(outputSize, 0.0) }
    val lstm =                      LSTM(inputSize, outputSize, learningRate)


    fun forward(){
        (1 until  recurrences + 1).forEach {
            if(it <= expectedOutput.lastIndex){
                lstm.inputLayer = hiddenStateArray[it - 1].append(input)
                lstm.forward()
                this.cellStateArray[it] = lstm.cellState
                this.hiddenStateArray[it] = lstm.outputLayer
                this.forgetArray[it] = lstm.forget
                this.inputGateArray[it] = lstm.input
                this.cellStateMatArray[it] = lstm.cell
                this.outputGateArray[it] = lstm.output
                this.outputArray[it] = (weights * lstm.output.toColMatrix()).toVector()!!.apply { v -> sigmoid.activate(v) }
                this.input = expectedOutput[it - 1]
            }
        }
    }

    fun backward() : Double {
        var totalError = 0.0
        var derivativeCellState = ArrayVector(outputSize, 0.0)
        var derivativeHiddenState = ArrayVector(outputSize, 0.0)
        var weightUpdate = ArrayMatrix(outputSize, outputSize) { 0.0 }
        var forgetGateUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
        var inputGateUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
        var cellUnit = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
        var outputGateUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
        (recurrences downTo 1).forEach {
            if(it <= outputArray.lastIndex && it <= expectedOutput.lastIndex){
                var error = outputArray[it] - expectedOutput[it]
                weightUpdate += error.hadamard(outputArray[it]).toColMatrix() * hiddenStateArray[it].toColMatrix().transpose()
                error = (this.weights * error.toColMatrix()).toVector()!!
                lstm.inputLayer = hiddenStateArray[it - 1].append(inputArray[it])
                lstm.cellState = this.cellStateArray[it]
                lstm.backward(error, this.cellStateArray[it - 1], this.forgetArray[it], this.inputArray[it], this.cellStateMatArray[it], this.outputArray[it], derivativeCellState, derivativeHiddenState)
                derivativeCellState = lstm.derivativePrimeCellState
                derivativeHiddenState = lstm.derivativePrimeHiddenState
                totalError += lstm.updatedError.sum()
                forgetGateUpdate += lstm.forgetUpdate
                inputGateUpdate += lstm.inputUpdate
                cellUnit += lstm.cellUpdate
                outputGateUpdate += lstm.outputUpdate
            }
        }
        lstm.update(forgetGateUpdate / recurrences, inputGateUpdate / recurrences, cellUnit / recurrences, outputGateUpdate / recurrences)
        this.update(weightUpdate / recurrences)
        return totalError
    }

    fun train(){
        forward()
        backward()
    }

    fun update(weightUpdate : ArrayMatrix){
        this.learningRateDecay = this.learningRateDecay * DECAY_FACTOR + weightUpdate.pow(2) * UPDATE_CONTRIBUTION
        this.weights -= this.learningRateDecay.apply { this.learningRate / kotlin.math.sqrt(it + NON_ZERO) }.hadamard(weightUpdate)
    }

    fun sample() : Array<ArrayVector> {
        (1 .. recurrences + 1).forEach {
            lstm.inputLayer = hiddenStateArray[it - 1].append(input)
            lstm.forward()

            this.cellStateArray[it] = lstm.cellState
            this.hiddenStateArray[it] = lstm.outputLayer
            this.forgetArray[it] = lstm.forget
            this.inputGateArray[it] = lstm.input
            this.cellStateMatArray[it] = lstm.cell
            this.outputGateArray[it] = lstm.output
            this.outputArray[it] = (weights * lstm.output.toColMatrix()).toVector()!!.apply { v -> sigmoid.activate(v) }
            this.input = this.outputArray[it]
        }
        return outputArray
    }

    companion object {
        val DECAY_FACTOR = 0.95
        val UPDATE_CONTRIBUTION = 0.1
        val NON_ZERO = 1e-8
    }

}