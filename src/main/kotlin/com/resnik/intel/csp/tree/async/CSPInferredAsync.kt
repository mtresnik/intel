package com.resnik.intel.csp.tree.async

import com.resnik.intel.csp.preprocessors.CSPPreprocessor
import com.resnik.intel.csp.tree.CSPAgent
import com.resnik.intel.csp.tree.CSPNode
import com.resnik.intel.csp.tree.CSPSingleRootAgent
import java.util.*
import java.util.concurrent.Executors


/** Higher complexity over the long-term than CSPCoroutine, because of overhead of creating thread pool each time. */
class CSPInferredAsync<VAR, DOMAIN>(
    domainMap: Map<VAR, List<DOMAIN>>,
    maxTime: Long = Long.MAX_VALUE,
    maxThreads: Int = MAX_THREAD_COUNT,
    sortVariables: Boolean = SORT_VARIABLES_DEFAULT,
    preprocessors: List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf()
) : CSPAsyncBase<VAR, DOMAIN>(domainMap, maxTime, maxThreads, sortVariables, preprocessors) {

    private val bfsSolutions = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())

    override fun findAllSolutions(): List<Map<VAR, DOMAIN>> {
        preprocess()
        onStart()
        // Construct for first variable in list
        val agents = constructAgents()
        val retList = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())
        retList.addAll(bfsSolutions)
        if (agents.isEmpty()) {
            // Make domain async
            val domainAsync = CSPDomainAsync(domainMap)
            domainAsync.addAllConstraintsFrom(this)
            return domainAsync.findAllSolutions()
        }
        val threadPoolSize = agents.size
        val executor = Executors.newFixedThreadPool(threadPoolSize)
        agents.forEach { agent ->
            executor.execute {
                val solutions = agent.findAllSolutions()
                synchronized(retList) {
                    retList.addAll(solutions)
                }
            }
        }
        executor.shutdown()
        while (!executor.isTerminated) {
        }
        onFinish()
        return retList.filter { isReusablyConsistent(it) }
    }

    override fun getFirstSolution(): Map<VAR, DOMAIN>? {
        preprocess()
        onStart()
        // Construct for first variable in list
        val agents = constructAgents()
        val retList = Collections.synchronizedList(mutableListOf<Map<VAR, DOMAIN>>())
        retList.addAll(bfsSolutions)
        // First solution was found by doing BFS somehow
        if (retList.isNotEmpty()) return retList.firstOrNull()
        if (agents.isEmpty()) {
            // Make domain async
            val domainAsync = CSPDomainAsync(domainMap)
            domainAsync.addAllConstraintsFrom(this)
            return domainAsync.getFirstSolution()
        }
        val threadPoolSize = agents.size
        val executor = Executors.newFixedThreadPool(threadPoolSize)
        agents.forEach { agent ->
            executor.execute {
                val solution = agent.getFirstSolution()
                synchronized(retList) {
                    if (retList.isEmpty()) retList.add(solution)
                }
            }
        }
        executor.shutdown()
        while (!executor.isTerminated) {
        }
        onFinish()
        return retList.firstOrNull()
    }

    override fun constructAgents(): List<CSPAgent<VAR, DOMAIN>> {
        bfsSolutions.clear()
        val start = System.currentTimeMillis()
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return listOf()
        val firstDomain = domainMap[first] ?: return listOf()

        // Infer the threadpool size based on num variables instead of by roots
        // Do a BFS, get the stack up to the variable number or less than some max thread count
        // Then run thread pool on each stack member
        // This works because BFS ~= DFS for low numbers of elements.

        // |BFSQueue| <= MAX_THREAD_COUNT / c -> DFSStack[] -> Answer
        val bfsQueue = ArrayDeque<CSPNode<VAR, DOMAIN>>()
        val rootNodes = firstDomain.map { domain -> CSPNode<VAR, DOMAIN>(first, domain, parent = null) }.toMutableList()
        rootNodes.forEach { bfsQueue.add(it) }
        // c = maxDomainSize for now because of max growth rate per iteration
        var currentBFS: CSPNode<VAR, DOMAIN>
        val maxDomainSize = domainMap.values.map { it.size }.maxOrNull()!!
        val maxQueueSize = (maxThreads - maxDomainSize).coerceAtLeast(firstDomain.size)
        while (bfsQueue.size <= maxQueueSize && bfsQueue.isNotEmpty()) {
            currentBFS = bfsQueue.poll()
            // Visit Current
            val currentMap = currentBFS.map
            if (isLocallyConsistent(currentBFS.variable, currentMap)) {
                if (currentMap.size == variables.size) {
                    // Don't add to solution set unless absolutely sure.
                    if (isConsistent(currentBFS.variable, currentMap)) {
                        bfsSolutions.add(currentMap)
                    }
                    // Else stop exploring branch.
                    continue
                } else {
                    // Add all children
                    val nextVariable = variables.firstOrNull { variable -> variable !in currentMap } ?: continue
                    val nextDomain = domainMap[nextVariable] ?: continue
                    nextDomain.forEach { domain ->
                        // Note: there won't be any parents in the queue (repeat calls) bc the parent is polled earlier
                        val child = CSPNode(nextVariable, domain, parent = currentBFS)
                        bfsQueue.add(child)
                    }
                }
            }
        }
        return with(bfsQueue.map { root ->
            val agent = CSPSingleRootAgent(domainMap, start, maxTime, root, variables)
            agent
        }) {
            this@CSPInferredAsync.addAllConstraintsTo(this)
            this
        }
    }

}