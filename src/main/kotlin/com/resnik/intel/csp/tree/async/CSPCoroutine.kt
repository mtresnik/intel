package com.resnik.intel.csp.tree.async

import com.resnik.intel.csp.preprocessors.CSPPreprocessor
import com.resnik.intel.csp.tree.CSPAgent
import com.resnik.intel.csp.tree.CSPNode
import com.resnik.intel.csp.tree.CSPSingleRootAgent
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors

class CSPCoroutine<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                maxTime : Long = Long.MAX_VALUE,
                                maxThreads : Int = MAX_THREAD_COUNT,
                                sortVariables : Boolean = SORT_VARIABLES_DEFAULT,
                                preprocessors : List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf())
    : CSPAsyncBase<VAR, DOMAIN>(domainMap, maxTime, maxThreads, sortVariables, preprocessors) {

    override fun findAllSolutions() : List<Map<VAR, DOMAIN>> {
        preprocess()
        onStart()
        val agents = constructAgents()
        val dispatcher = getDispatcher()
        val retList = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())
        val scope = CoroutineScope(dispatcher)
        val jobs = agents.map { agent ->
            scope.launch(dispatcher) {
                val solutions = agent.findAllSolutions()
                synchronized(retList){
                    retList.addAll(solutions)
                }
            }
        }
        runBlocking(dispatcher) { jobs.joinAll() }
        onFinish()
        return retList.filter { isReusablyConsistent(it) }
    }

    override fun getFirstSolution(): Map<VAR, DOMAIN>? {
        preprocess()
        onStart()
        val agents = constructAgents()
        val dispatcher = getDispatcher()
        val retList = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())
        val scope = CoroutineScope(dispatcher)
        val jobs = agents.map { agent ->
            scope.launch(dispatcher) {
                val solution = agent.getFirstSolution()
                synchronized(retList){
                    if(retList.isEmpty()) retList.add(solution)
                    dispatcher.cancel()
                }
            }
        }
        runBlocking(dispatcher) { jobs.joinAll() }
        onFinish()
        return retList.firstOrNull()
    }

    override fun constructAgents() : List<CSPAgent<VAR, DOMAIN>> {
        // Construct for first variable in list
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return listOf()
        val firstDomain = domainMap[first] ?: return listOf()
        // Roots don't have parents
        val start = System.currentTimeMillis()
        return with(firstDomain.map { domain ->
            // Convert each root domain to a job
            val current = CSPNode<VAR, DOMAIN>(first, domain, parent = null)
            val agent = CSPSingleRootAgent(domainMap, start, maxTime, current, variables)
            agent
        }) {
            this@CSPCoroutine.addAllConstraintsTo(this)
            this
        }
    }


    companion object {

        private var setThreads : Int = -1
        private var dispatcher : CoroutineDispatcher? = null

        private fun getDispatcher(threadCount : Int = MAX_THREAD_COUNT) : CoroutineDispatcher {
            if(threadCount == setThreads && dispatcher != null) return dispatcher!!
            setThreads = threadCount
            dispatcher = Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()
            return dispatcher!!
        }

    }
}