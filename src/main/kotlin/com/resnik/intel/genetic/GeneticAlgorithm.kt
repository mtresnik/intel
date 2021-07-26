package com.resnik.intel.genetic

import kotlin.math.abs

class GeneticAlgorithm<T>(val numberOfGenes : Int,
                          // Rough 1.5 - 2.0 * number of genes
                          val populationSize : Int = numberOfGenes * 2,
                          // How much crossover will happen
                          val crossoverRate : Double = 0.85,
                          // High mutation gives global search, low gives diversity
                          val mutationRate : Double = 0.05,
                          // Keep some fraction of elites for next generation
                          val elitismFraction : Double = 0.01,
                          val printFitness : Boolean = true,
                          val fitnessFunction: GeneticFitnessFunction<T>,
                          val geneFactory: GeneFactory<T>,
                          val chromosomeFactory: ChromosomeFactory<T> = DefaultChromosomeFactory(numberOfGenes, geneFactory)) {

    var population : MutableList<Chromosome<T>> = mutableListOf()
    var matingPool : MutableList<Chromosome<T>> = mutableListOf()
    var generation : Int = 0
    var totalFitness : Double = 0.0
    lateinit var fittest : Chromosome<T>
    var historicalFittest : MutableList<Chromosome<T>> = mutableListOf()

    private fun init() {
        // Reset stale variables for reuse
        population.clear()
        matingPool.clear()
        historicalFittest.clear()
        generation = 0
        population = MutableList(populationSize){chromosomeFactory.next()}

    }

    fun trainEpoch(numEpochs : Int = 100) : Chromosome<T> {
        init()
        while (generation < numEpochs){
            train()
        }
        if(printFitness)
            printFitness()
        return fittest
    }

    fun trainUntil(threshold : Double) : Chromosome<T> {
        init()
        train()
        var previousFitness = -1.0
        var nextFitness = fittest.fitness
        while(abs(nextFitness - previousFitness) > threshold){
            train()
            previousFitness = nextFitness
            nextFitness = fittest.fitness
        }
        if(printFitness)
            printFitness()
        return fittest
    }

    private fun train() {
        evaluateFitness()
        normalizeFitness()
        selectParents()
        breedNextGeneration()
        if(printFitness)
            printFitness()
        generation++
    }

    fun evaluateFitness() {
        totalFitness = 0.0
        population.forEach {
            it.fitness = fitnessFunction.evaluateFitness(it)
            totalFitness += it.fitness
        }
        population.sort()
        fittest = population.last()
        historicalFittest.add(fittest)
    }

    fun normalizeFitness(){
        var totalAccumulatedNormalFitness = 0.0
        population.forEach {
            it.normalizedFitness = if(totalFitness == 0.0) 0.0 else it.fitness / totalFitness
            it.accumulatedNormalizedFitness = it.normalizedFitness + totalAccumulatedNormalFitness
            totalAccumulatedNormalFitness = it.accumulatedNormalizedFitness
        }
        population.last().accumulatedNormalizedFitness = 1.0
    }

    fun nextParent() : Chromosome<T> {
        // Roulette wheel method
        var ret : Chromosome<T>? = null
        var attempts = 0
        while(ret == null){
            val rand = Math.random()
            ret = population.firstOrNull { it.accumulatedNormalizedFitness >= rand }
            attempts++
            if(attempts > 5)
                return population.random()
        }
        return ret
    }

    fun selectParents() {
        population.sortWith(Comparator { o1, o2 -> o1.accumulatedNormalizedFitness.compareTo(o2.accumulatedNormalizedFitness) })
        matingPool = MutableList(populationSize){nextParent()}
    }

    fun breedNextGeneration(){
        matingPool.chunked(2).forEach { it ->
            var child1 = it.first()
            var child2 = it.last()
            if(Math.random() < crossoverRate){
                val crossed = it.first() * it.last()
                child1 = crossed.first
                child2 = crossed.second
            }
            population.add(mutate(child1))
            population.add(mutate(child2))
        }
    }

    fun mutate(chromosome : Chromosome<T>) : Chromosome<T> {
        val rand = Math.random()
        if(rand <= mutationRate){
            val mutateIndex = chromosome.values.indices.random()
            chromosome[mutateIndex] = geneFactory.mutate(chromosome[mutateIndex])
        }
        return chromosome
    }

    fun printFitness(){
        println("Generation: $generation \t -- Best Fitness: ${fittest.fitness} \t -- Best Chromosome: ${fittest.values}")
    }

}