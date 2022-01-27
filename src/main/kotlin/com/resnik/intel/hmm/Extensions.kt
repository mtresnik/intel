package com.resnik.intel.hmm

import kotlin.math.exp
import kotlin.math.ln

/**
 *
 * Mirrors the IT++ implementation of logsumexp(DoubleArray), where traditional would be
 *
 * {LSE(X) = log(X).sum() = log(X.product()) }
 *
 * This implementation uses:
 *
 * {LSE(X) = x* + log(exp(x0 - x*) + exp(x1 - x*) ... exp(xN - x*))}
 *
 * Which is mathematically equivalent but reduces risk of arithmetic over/underflow
 *
 * */
fun DoubleArray.logsumexp() : Double {
    if(this.isEmpty())
        return 0.0
    val max = this.max() ?: this.first()
    /*
    * Needed for possible NaN values, where xi = inf and x* = inf then inf - inf = NaN
    * */
    if(max == Double.NEGATIVE_INFINITY)
        return Double.NEGATIVE_INFINITY
    if(max == Double.POSITIVE_INFINITY)
        return Double.POSITIVE_INFINITY
    var sum = 0.0
    this.forEach { x ->
        sum += exp(x - max)
    }
    return ln(sum) + max
}

fun DoubleArray.product() : Double {
    var product = 1.0
    this.forEach { product *= it }
    return product
}

fun DoubleArray.normalize() : DoubleArray {
    val sum = this.sum()
    return this.map { it / sum }.toDoubleArray()
}