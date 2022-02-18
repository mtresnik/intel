package com.resnik.intel.csp.tree

import com.resnik.intel.csp.MultiCSP
import java.util.*

internal open class CSPAgent<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                          var start : Long = System.currentTimeMillis(),
                                          private val maxTime : Long = Long.MAX_VALUE,
                                          private val stack : Stack<CSPNode<VAR, DOMAIN>>,
                                          private val sortedVariables : List<VAR>) : MultiCSP<VAR, DOMAIN>(domainMap, sortVariables = true) {

    override fun genSequence() : Sequence<Map<VAR, DOMAIN>> = sequence {
        onStart()
        var current : CSPNode<VAR, DOMAIN>
        while (stack.isNotEmpty() && timeElapsed() < maxTime) {
            // DFS on tree
            current = stack.pop()

            // Visit Current
            val currentMap = current.map
            if(isLocallyConsistent(current.variable, currentMap)) {
                if(currentMap.size == sortedVariables.size) {
                    // Don't add to solution set unless absolutely sure.
                    if(isConsistent(current.variable, currentMap)) {
                        yield(currentMap)
                    }
                    // Else stop exploring branch.
                    continue
                } else {
                    // Add all children
                    val nextVariable = sortedVariables.firstOrNull { variable -> variable !in currentMap } ?: continue
                    val nextDomain = domainMap[nextVariable] ?: continue
                    nextDomain.forEach { domain ->
                        stack.push(CSPNode(nextVariable, domain, parent = current))
                    }
                }
            }
        }
        onFinish()
    }

}