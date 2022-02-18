package com.resnik.intel.csp

import com.resnik.intel.csp.preprocessors.CSPPreprocessor

abstract class MultiCSP<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                     sortVariables : Boolean = SORT_VARIABLES_DEFAULT,
                                     preprocessors : List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf())
    : CSPBase<VAR, DOMAIN>(domainMap, sortVariables, preprocessors){

    open fun findAllSolutions(): List<Map<VAR, DOMAIN>> = genSequence().toList().filter { isReusablyConsistent(it) }

    override fun getFirstSolution(): Map<VAR, DOMAIN>? = genSequence().firstOrNull()

    open fun genSequence() : Sequence<Map<VAR, DOMAIN>> = sequence {  }

    fun getFrequencies() : Map<VAR, Map<DOMAIN, Int>> {
        // Get Solutions
        val solutions = findAllSolutions()
        // Setup retMap
        val variables = this.domainMap.keys
        val retMap = mutableMapOf<VAR, MutableMap<DOMAIN, Int>>()
        variables.forEach { variable ->
            val variableMap = mutableMapOf<DOMAIN, Int>()
            val domains = domainMap[variable]!!
            domains.forEach { domain -> variableMap[domain] = 0 }
            retMap[variable] = variableMap
        }
        // For each solution increment proper count
        solutions.forEach { solution ->
            solution.forEach { (variable, domain) ->
                retMap[variable]!![domain] = retMap[variable]!![domain]!! + 1
            }
        }
        return retMap
    }

    fun getProbabilities() : Map<VAR, Map<DOMAIN, Double>> {
        val frequencies = getFrequencies()
        // Normalize frequencies st probabilities[var].sum() = 1.0
        val retMap = mutableMapOf<VAR, MutableMap<DOMAIN, Double>>()
        val variables = this.domainMap.keys
        variables.forEach { variable ->
            val sum = frequencies[variable]!!.values.sum().toDouble()
            val currMap = mutableMapOf<DOMAIN, Double>()
            val domains = domainMap[variable]!!
            if(sum <= 0.0) {
                // Make each domain value 0.0
                domains.forEach { domain -> currMap[domain] = 0.0 }
            } else {
                val domainFrequencies = frequencies[variable]!!
                // Make each domain value scaled
                domains.forEach { domain -> currMap[domain] = domainFrequencies[domain]!! / sum }
            }
            retMap[variable] = currMap
        }
        return retMap
    }

}