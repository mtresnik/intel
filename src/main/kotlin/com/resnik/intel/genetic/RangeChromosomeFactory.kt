package com.resnik.intel.genetic

open class RangeChromosomeFactory(vararg val ranges: RangeGeneFactory) : ChromosomeFactory<Double> {

    override fun next(): Chromosome<Double> {
        return Chromosome(MutableList(ranges.size){ranges[it].next()})
    }

}