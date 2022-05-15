package com.resnik.intel

import com.resnik.math.linear.array.ArrayVector
import kotlin.random.Random

interface Model {

    @Deprecated("Adds bias to first inputs more than later inputs.")
    fun train(inputs: Array<ArrayVector>, expected: Array<ArrayVector>): Double {
        if (inputs.size != expected.size) {
            throw IllegalArgumentException("Inputs must equal outputs")
        }
        var ret = Double.MAX_VALUE
        inputs.indices.forEach {
            ret = train(inputs[it], expected[it])
        }
        return ret
    }

    fun trainRandom(
        inputs: Array<ArrayVector>,
        expected: Array<ArrayVector>,
        epochs: Int = 10000,
        printEvery: Int = 100
    ) {
        repeat(epochs) { epoch ->
            // Poll random index to train
            val randomIndex = Random.nextInt(0, inputs.size)
            val error = train(inputs[randomIndex], expected[randomIndex])
            if (epoch % printEvery == 0) {
                println("Iteration: $epoch - - - - - Error: $error")
            }
        }
    }

    fun trainBatch(inputs: Array<ArrayVector>, expected: Array<ArrayVector>, epochs: Int = 10000, batchSize: Int = 8) {
        repeat(epochs) {
            val randomIndex = Random.nextInt(0, inputs.size)
            for (i in randomIndex until randomIndex + batchSize) {
                val index = i % inputs.size
                train(inputs[index], expected[index])
            }

        }
    }

    fun train(input: ArrayVector, expected: ArrayVector): Double

    fun predict(input: ArrayVector): ArrayVector

}