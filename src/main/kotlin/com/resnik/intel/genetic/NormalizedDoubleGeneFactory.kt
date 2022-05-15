package com.resnik.intel.genetic

import java.util.*

class NormalizedDoubleGeneFactory(min: Double, max: Double, val spread: Double) : RangeGeneFactory(min, max) {

    val random: Random = Random()

    override fun mutate(previous: Gene<Double>?): Gene<Double> {
        if (previous == null)
            return next()
        return Gene(random.nextGaussian() * spread + previous.value)
    }

}