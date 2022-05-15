package com.resnik.intel.genetic

class DefaultChromosomeFactory<T>(val size: Int, val geneFactory: GeneFactory<T>) : ChromosomeFactory<T> {

    override fun next(): Chromosome<T> {
        return Chromosome(MutableList(size) { geneFactory.next() })
    }

}