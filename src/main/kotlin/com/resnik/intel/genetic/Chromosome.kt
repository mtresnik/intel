package com.resnik.intel.genetic

import java.lang.IllegalArgumentException

class Chromosome<T>(val values: MutableList<Gene<T>>) : Comparable<Chromosome<T>>{

    var fitness : Double = 0.0
    var normalizedFitness : Double = 0.0 // Between 0.0 and 1.0
    var accumulatedNormalizedFitness : Double = 0.0 // Between 0.0 and 1.0

    operator fun times(other: Chromosome<T>) : Pair<Chromosome<T>, Chromosome<T>> {
        // Cross over (swap)
        if(this.size() != other.size())
            throw IllegalArgumentException("Sizes are different! ${this.size()} != ${other.size()}")
        // Random point for crossover
        val crossIndex: Int = this.values.indices.random()
        val c1 = Chromosome(MutableList(size()){
            if(it <= crossIndex){
                this[it]
            }else{
                other[it]
            }
        })
        val c2 = Chromosome(MutableList(size()){
            if(it <= crossIndex){
                other[it]
            }else{
                this[it]
            }
        })
        return Pair(c1, c2)
    }

    fun lastIndex() : Int = values.lastIndex

    fun size() : Int = values.size

    operator fun get(index: Int) : Gene<T> = values[index]

    operator fun set(index: Int, value: Gene<T>) {
        values[index] = value
    }

    operator fun set(index: Int, value: T) {
        values[index] = Gene(value)
    }

    override fun compareTo(other: Chromosome<T>): Int {
        return fitness.compareTo(other.fitness)
    }

}