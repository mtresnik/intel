package com.resnik.intel.neural.conv

import com.resnik.intel.neural.TensorLossFunction
import com.resnik.math.linear.array.ArrayTensor

class TensorNetworkTrainer(
        val tensorNetwork: TensorNetwork,
        val lossFunction: TensorLossFunction,
        val inputs : Array<ArrayTensor>,
        val labels : Array<ArrayTensor>,
        var learningRate : Double,
        val printFrequency : Int = 10) {

    var numEpochs : Int = 0

    fun trainEpoch(){
        val trainOrder = inputs.indices.shuffled()
        trainOrder.indices.forEach {
            val index = trainOrder[it]
            val output = train(inputs[it], labels[it])
            if(numEpochs % printFrequency == 0){
                val loss = lossFunction.loss(labels[it].values, output)
                println("Epoch: $numEpochs \t Loss: $loss")
            }
        }
        numEpochs++
    }

    fun train(input : ArrayTensor, label : ArrayTensor) : DoubleArray {
        val output = tensorNetwork.forward(input)
        tensorNetwork.backward(lossFunction.lossDerivative(label, output), learningRate)
        return output.values
    }
}