package com.resnik.intel.cluster

import com.resnik.math.linear.array.ArrayPoint

class Cluster(val seed: ArrayPoint, vararg optionalPoints: ArrayPoint) : ArrayList<ArrayPoint>() {

    val dim = seed.dim

    init {
        this.add(seed)
        this.addAll(optionalPoints)
    }

    fun getMean() : ArrayPoint =
        ArrayPoint(*DoubleArray(dim) { index ->
            var sum = 0.0; this.forEach { point -> sum += point[index] }; sum / this.size
        })

    fun distanceTo(other: ArrayPoint) : Double = getMean().distanceTo(other)

    fun getVariance() : Double {
        val mean = getMean()
        return sumOf { point -> point.distanceTo(mean) } / size
    }

}