package com.resnik.intel

import com.resnik.intel.neural.FeedForwardNeuralNetwork
import com.resnik.math.linear.array.ArrayVector
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.random.Random

interface Model {

    fun train(inputs: Array<ArrayVector>, expected: Array<ArrayVector>) : Double {
        if(inputs.size != expected.size){
            throw IllegalArgumentException("Inputs must equal outputs")
        }
        var ret = Double.MAX_VALUE
        inputs.indices.forEach {
            ret = train(inputs[it], expected[it])
        }
        return ret
    }

    fun trainRandom(inputs: Array<ArrayVector>, expected: Array<ArrayVector>, epochs: Int = 10000, printEvery: Int = 100){
        repeat(epochs){epoch ->
            // Poll random index to train
            val randomIndex = Random.nextInt(0, inputs.size)
            val error = train(inputs[randomIndex], expected[randomIndex])
            if(epoch % printEvery == 0){
                println("Iteration: $epoch - - - - - Error: $error")
            }
        }
    }

    fun trainRandomTotalError(inputs: Array<ArrayVector>, expected: Array<ArrayVector>, epochs: Int = 10000, printEvery: Int = 1000){
        var previousError = 0.0
        repeat(epochs){epoch ->
            // Poll random index to train
            val randomIndex = Random.nextInt(0, inputs.size)
            val error = train(inputs[randomIndex], expected[randomIndex])
            if(epoch % printEvery == 0){
                val totalError = inputs.indices.sumByDouble { (predict(inputs[it]) - expected[it]).magnitude() }
                println("Iteration: ($epoch , $totalError) dError:${previousError - totalError}")
                previousError = totalError
            }
        }
    }

    fun trainRandomAdjusted(inputs: Array<ArrayVector>, expected: Array<ArrayVector>, epochs: Int = 10000, printEvery: Int = 1000){
        val originalLearningRate = if(this is FeedForwardNeuralNetwork) this.learningRate else 1.0
        var previousError = 0.0
        repeat(epochs){epoch ->
            // Poll random index to train
            val randomIndex = Random.nextInt(0, inputs.size)
            val error = train(inputs[randomIndex], expected[randomIndex])
            if(epoch % printEvery == 0){
                val totalError = inputs.indices.sumByDouble { (predict(inputs[it]) - expected[it]).magnitude() }
                println("Iteration: ($epoch , $totalError)")
                if(this is FeedForwardNeuralNetwork && previousError != 0.0){
                    this.learningRate = abs(1.0 - totalError / previousError) + epoch.toDouble() / epochs
                    if(totalError == previousError){
                        return
                    } else if(abs(totalError / previousError) < 0.1){
                        this.learningRate = 0.1
                    }
                }
                previousError = totalError
            }
        }
    }

    fun trainBatch(inputs: Array<ArrayVector>, expected: Array<ArrayVector>, epochs: Int = 10000, batchSize: Int = 8){
        repeat(epochs){epoch ->
            val randomIndex = Random.nextInt(0, inputs.size)
            for(i in randomIndex until randomIndex + batchSize){
                val index = i % inputs.size
                train(inputs[index], expected[index])
            }

        }
    }

    fun train(input: ArrayVector, expected: ArrayVector) : Double

    fun predict(input: ArrayVector) : ArrayVector

}