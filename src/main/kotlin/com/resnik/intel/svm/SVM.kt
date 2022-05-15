package com.resnik.intel.svm

import com.resnik.intel.Model
import com.resnik.math.linear.array.ArrayVector
import kotlin.math.pow

class SVM(dim: Int, val kernel: Kernel = RBF, val learningRate: Double = 0.001, val lambda: Double = 0.01) : Model {

    var w: ArrayVector = ArrayVector(dim, 0.0)
    var b: Double = 0.0

    override fun train(input: ArrayVector, expected: ArrayVector): Double {
        val y = expected[0]
        update(input, y)
        return cost(input, y)
    }

    override fun predict(input: ArrayVector): ArrayVector = ArrayVector(hyperplane(input))

    fun hyperplane(x: ArrayVector): Double {
        val ret = w * x - b
        if (ret <= -1) {
            return -1.0
        }
        if (ret >= 1) {
            return 1.0
        }
        return ret
    }

    fun hingeloss(x: ArrayVector, y: Double): Double = (0.0).coerceAtLeast(1 - y * (w * x - b))

    fun cost(x: Array<ArrayVector>, y: DoubleArray): Double {
        return lambda * (w.magnitude().pow(2)) + (1.0 / x.size) * x.indices.sumByDouble { hingeloss(x[it], y[it]) }
    }

    fun cost(x: ArrayVector, y: Double): Double {
        val eval = y * hyperplane(x)
        if (eval >= 1) {
            return lambda * (w.magnitude().pow(2))
        }
        return lambda * (w.magnitude().pow(2)) + 1 - y * (w * x - b)
    }

    fun gradient(x: ArrayVector, y: Double): Pair<ArrayVector, Double> {
        val eval = y * hyperplane(x)
        var dJdW: ArrayVector = ArrayVector(w.size())
        var dJdB: Double = 0.0
        if (eval >= 1) {
            dJdW = w * (2 * lambda)
            dJdB = 0.0
        } else {
            dJdW = w * (2 * lambda) - x * y
            dJdB = y
        }
        return Pair(dJdW, dJdB)
    }

    fun update(x: ArrayVector, y: Double) {
        val gradient = gradient(x, y)
        w -= gradient.first * learningRate
        b -= gradient.second * learningRate
    }

    companion object {

        val linear: Kernel = object : Kernel {
            override fun apply(x1: ArrayVector, x2: ArrayVector): Double = x1 * x2
        }

        val quadratic: Kernel = object : Kernel {
            override fun apply(x1: ArrayVector, x2: ArrayVector): Double = (x1 * x2).pow(2)
        }

        val RBF: Kernel = RadialBasisFunction()

    }

}