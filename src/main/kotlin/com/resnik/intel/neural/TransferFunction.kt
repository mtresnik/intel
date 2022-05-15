package com.resnik.intel.neural

import kotlin.math.pow
import kotlin.math.tanh

interface TransferFunction {


    fun activate(x: Double): Double

    fun derivative(x: Double): Double

    companion object {

        val sigmoid: TransferFunction = object : TransferFunction {

            override fun activate(x: Double): Double = 1 / (1 + kotlin.math.exp(-x))

            override fun derivative(x: Double): Double = activate(x) * (1 - activate(x))

        }

        val relu: TransferFunction = object : TransferFunction {

            override fun activate(x: Double): Double = x.coerceAtLeast(0.0)

            override fun derivative(x: Double): Double = if (x <= 0) 0.0 else 1.0

        }

        val tanh: TransferFunction = object : TransferFunction {

            override fun activate(x: Double): Double = tanh(x)

            override fun derivative(x: Double): Double = 1.0 - tanh(x).pow(2)

        }

    }

}