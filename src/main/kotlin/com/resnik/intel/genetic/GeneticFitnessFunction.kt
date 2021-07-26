package com.resnik.intel.genetic

interface GeneticFitnessFunction<T> {

    fun evaluateFitness(individual: Chromosome<T>) : Double

}