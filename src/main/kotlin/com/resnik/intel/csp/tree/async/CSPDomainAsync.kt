package com.resnik.intel.csp.tree.async

import com.resnik.intel.csp.preprocessors.CSPPreprocessor
import com.resnik.intel.csp.tree.CSPAgent
import com.resnik.intel.csp.tree.CSPNode
import com.resnik.intel.csp.tree.CSPSingleRootAgent
import java.util.*
import java.util.concurrent.Executors

/**
 * Useful for when domain size is significant. For a binary tree this would speed up calcs by a factor of 2...
 * For an n-ary tree, this would speed up calcs by a factor of n | n < MAX_THREAD_COUNT
 * */
class CSPDomainAsync<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                  maxTime : Long = Long.MAX_VALUE,
                                  maxThreads : Int = MAX_THREAD_COUNT,
                                  sortVariables : Boolean = SORT_VARIABLES_DEFAULT,
                                  preprocessors : List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf())
    : CSPAsyncBase<VAR, DOMAIN>(domainMap, maxTime, maxThreads, sortVariables, preprocessors) {

    override fun findAllSolutions() : List<Map<VAR, DOMAIN>> {
        preprocess()
        onStart()
        val agents = constructAgents()
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return listOf()
        val firstDomain = domainMap[first] ?: return listOf()
        val threadPoolSize = firstDomain.size.coerceIn(1, maxThreads)
        val executor = Executors.newFixedThreadPool(threadPoolSize)
        val retList = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())
        agents.forEach { agent ->
            executor.execute {
                val solutions = agent.findAllSolutions()
                synchronized(retList){
                    retList.addAll(solutions)
                }
            }
        }
        executor.shutdown()
        while(!executor.isTerminated) {}
        onFinish()
        return retList
    }

    override fun getFirstSolution(): Map<VAR, DOMAIN>? {
        preprocess()
        onStart()
        val agents = constructAgents()
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return null
        val firstDomain = domainMap[first] ?: return null
        val threadPoolSize = firstDomain.size.coerceIn(1, maxThreads)
        val executor = Executors.newFixedThreadPool(threadPoolSize)
        val retList = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())
        agents.forEach { agent ->
            executor.execute {
                val solution = agent.getFirstSolution()
                synchronized(retList){
                    if(retList.isEmpty()) retList.add(solution)
                }
            }
        }
        executor.shutdown()
        while(!executor.isTerminated) {}
        onFinish()
        return retList.firstOrNull()
    }

    override fun constructAgents() : List<CSPAgent<VAR, DOMAIN>> {
        val retList = Collections.synchronizedList(mutableListOf<CSPAgent<VAR, DOMAIN>>())
        // Construct for first variable in list
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return retList
        val firstDomain = domainMap[first] ?: return retList
        // Roots don't have parents
        val start = System.currentTimeMillis()
        firstDomain.map { domain ->
            // Convert each root domain to a job
            val current = CSPNode<VAR, DOMAIN>(first, domain, parent = null)
            val agent = CSPSingleRootAgent(domainMap, start, maxTime, current, variables)
            agent
        }
        this.addAllConstraintsTo(retList)
        return retList
    }

}