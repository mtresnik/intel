package com.resnik.intel.genetic

interface ChromosomeFactory<T> {

    fun next() : Chromosome<T>

}