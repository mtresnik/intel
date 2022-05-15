package com.resnik.intel.csp.tree

import com.resnik.intel.csp.MultiCSP
import com.resnik.intel.csp.preprocessors.CSPPreprocessor
import java.util.*

/**
 * Standard, iterative Depth-First-Search CSP
 * */
class CSPTree<VAR, DOMAIN>(
    domainMap: Map<VAR, List<DOMAIN>>,
    private val maxTime: Long = Long.MAX_VALUE,
    sortVariables: Boolean = SORT_VARIABLES_DEFAULT,
    preprocessors: List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf()
) : MultiCSP<VAR, DOMAIN>(domainMap, sortVariables, preprocessors) {

    override fun findAllSolutions(): List<Map<VAR, DOMAIN>> {
        preprocess()
        onStart()
        val cspAgent = constructAgent() ?: return listOf()
        val ret = cspAgent.findAllSolutions()
        onFinish()
        return ret
    }

    override fun getFirstSolution(): Map<VAR, DOMAIN>? {
        preprocess()
        onStart()
        val cspAgent = constructAgent() ?: return null
        val ret = cspAgent.getFirstSolution()
        onFinish()
        return ret
    }

    private fun constructAgent(): CSPAgent<VAR, DOMAIN>? {
        val start = System.currentTimeMillis()
        // Construct for first variable in list
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return null
        val firstDomain = domainMap[first] ?: return null
        // Roots don't have parents
        val rootNodes = firstDomain.map { domain -> CSPNode<VAR, DOMAIN>(first, domain, parent = null) }.toMutableList()
        val stack = Stack<CSPNode<VAR, DOMAIN>>()
        rootNodes.forEach { stack.push(it) }
        return with(CSPAgent(domainMap, start, maxTime, stack, variables)) {
            this@CSPTree.addAllConstraintsTo(this)
            this
        }
    }

}