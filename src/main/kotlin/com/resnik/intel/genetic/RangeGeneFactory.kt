package com.resnik.intel.genetic

import java.lang.Double.max
import java.lang.Double.min

open class RangeGeneFactory(min: Double, max: Double) : GeneFactory<Double>(arrayOf()) {

    val min: Double = min(min, max)
    val max: Double = max(min, max)

    override fun next(): Gene<Double> = Gene(Math.random() * (max - min) + min)

}