package com.resnik.intel.neural

import com.resnik.intel.neural.TransferFunction.Companion.sigmoid
import com.resnik.intel.neural.TransferFunction.Companion.tanh
import com.resnik.math.linear.array.ArrayMatrix
import com.resnik.math.linear.array.ArrayVector

class LSTM(inputSize : Int, val outputSize: Int, val learningRate: Double) {

    var inputLayer: ArrayVector = ArrayVector(inputSize + outputSize)
    var outputLayer: ArrayVector = ArrayVector(outputSize)
    var cellState: ArrayVector = ArrayVector(outputSize)
    var forgetState : ArrayMatrix = ArrayMatrix(outputSize, inputSize + outputSize)
    var inputGate : ArrayMatrix = ArrayMatrix(outputSize, inputSize + outputSize)
    var cellStateMat : ArrayMatrix = ArrayMatrix(outputSize, inputSize + outputSize)
    var outputGate : ArrayMatrix = ArrayMatrix(outputSize, inputSize + outputSize)

    var forgetGateGradient = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var inputGateGradient = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var cellStateGradient = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var outputGateGradient = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }

    var forget = ArrayVector(outputSize, 0.0)
    var input = ArrayVector(outputSize, 0.0)
    var cell = ArrayVector(outputSize, 0.0)
    var output = ArrayVector(outputSize, 0.0)

    var updatedError = ArrayVector(outputSize, 0.0)
    var outputUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var cellUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var inputUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var forgetUpdate = ArrayMatrix(outputSize, inputSize + outputSize) { 0.0 }
    var derivativePrimeCellState = ArrayVector(outputSize, 0.0)
    var derivativePrimeHiddenState = ArrayVector(outputSize, 0.0)

    fun forward() {
        forget = (this.forgetState.transpose() * this.inputLayer.toColMatrix()).apply { sigmoid.activate(it) }.toVector()!!
        cellState = cellState.hadamard(forget)
        input = (this.inputGate.transpose() * this.inputLayer.toColMatrix()).apply { sigmoid.activate(it) }.toVector()!!
        cell = (this.cellStateMat.transpose() * this.inputLayer.toColMatrix()).apply { tanh.activate(it) }.toVector()!!
        this.cellState = input.hadamard(cell)
        output = (this.outputGate.transpose() * this.inputLayer.toColMatrix()).apply { sigmoid.activate(it) }.toVector()!!
        this.outputLayer = output.hadamard(cellState.apply { tanh.activate(it) })
    }

    fun backward(error : ArrayVector, allCellStates : ArrayVector, forget: ArrayVector, input: ArrayVector, cell: ArrayVector, output: ArrayVector, cellDerivative: ArrayVector, hiddenStateDerivative: ArrayVector) {
        updatedError = (error + hiddenStateDerivative).apply { it.coerceIn(MIN_WEIGHT, MAX_WEIGHT) } // outputSize x 1
        val outputDerivative = cellState.apply { tanh.activate(it) }.hadamard(updatedError) // outputSize x 1
        // Output Update : outputSize x inputSize + outputSize
        outputUpdate = outputDerivative.hadamard(output.apply { tanh.derivative(it) }).toColMatrix() * this.inputLayer.toColMatrix().transpose()
        val derivativeCellState = (updatedError.hadamard(output).hadamard(cellState.apply { tanh.derivative(it) } + cellDerivative)).apply { it.coerceIn(MIN_WEIGHT, MAX_WEIGHT) } // outputSize x 1
        val derivativeCell = derivativeCellState.hadamard(input) // outputSize x 1
        cellUpdate = derivativeCell.hadamard(cell.apply { tanh.derivative(it) }).toColMatrix() * this.inputLayer.toColMatrix().transpose()
        val inputDerivative = derivativeCellState.hadamard(cell)
        inputUpdate = inputDerivative.hadamard(input.apply { sigmoid.derivative(it) }).toColMatrix() * this.inputLayer.toColMatrix().transpose()
        val derivativeForget = derivativeCellState.hadamard(allCellStates)
        forgetUpdate = derivativeForget.hadamard(forget.apply { sigmoid.derivative(it) }).toColMatrix() * this.inputLayer.toColMatrix().transpose()
        derivativePrimeCellState = derivativeCellState.hadamard(forget)
        derivativePrimeHiddenState =
                (this.cellStateMat.transpose() * derivativeCell.toColMatrix()).toVector()!!.limit(outputSize) +
                (this.outputGate.transpose() * outputDerivative.toColMatrix()).toVector()!!.limit(outputSize) +
                (this.inputGate.transpose() * inputDerivative.toColMatrix()).toVector()!!.limit(outputSize) +
                (this.forgetState.transpose() * derivativeForget.toColMatrix()).toVector()!!.limit(outputSize)
    }

    fun update(forgetUpdate : ArrayMatrix, inputUpdate : ArrayMatrix, cellUpdate : ArrayMatrix, outputUpdate : ArrayMatrix){
        this.forgetGateGradient = this.forgetGateGradient * UPDATE_RATE + forgetUpdate.pow(2) * (1.0 - UPDATE_RATE)
        this.inputGateGradient = this.inputGateGradient * UPDATE_RATE   + inputUpdate.pow(2)  * (1.0 - UPDATE_RATE)
        this.cellStateGradient = this.cellStateGradient * UPDATE_RATE + cellUpdate.pow(2) * (1.0 - UPDATE_RATE)
        this.outputGateGradient = this.outputGateGradient * UPDATE_RATE + outputUpdate.pow(2) * (1.0 - UPDATE_RATE)

        this.forgetState -= this.forgetGateGradient.apply { this.learningRate / kotlin.math.sqrt(it + NON_ZERO) }.hadamard(forgetUpdate)
        this.inputGate -= this.inputGateGradient.apply { this.learningRate / kotlin.math.sqrt(it + NON_ZERO) }.hadamard(inputUpdate)
        this.cellStateMat -= this.cellStateGradient.apply { this.learningRate / kotlin.math.sqrt(it + NON_ZERO) }.hadamard(cellUpdate)
        this.outputGate -= this.outputGateGradient.apply { this.learningRate / kotlin.math.sqrt(it + NON_ZERO) }.hadamard(outputUpdate)
    }

    companion object {
        val MIN_WEIGHT = -6.0
        val MAX_WEIGHT = 6.0

        val UPDATE_RATE = 0.9
        val NON_ZERO = 1e-8
    }

}