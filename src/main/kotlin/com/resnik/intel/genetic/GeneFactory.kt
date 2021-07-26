package com.resnik.intel.genetic

open class GeneFactory<T> (private val possibleValues: Array<T>){

    private val cachedValues = possibleValues.map { Gene(it) }.toTypedArray()

    open fun getAllChromosomes() : Array<Gene<T>> = cachedValues

    open fun mutate(previous : Gene<T>?) : Gene<T> = next()

    open fun next() : Gene<T> = cachedValues.random()

}