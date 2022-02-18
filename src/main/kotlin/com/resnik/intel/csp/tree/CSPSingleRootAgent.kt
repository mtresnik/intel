package com.resnik.intel.csp.tree

import java.util.*

internal class CSPSingleRootAgent<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                               start : Long = System.currentTimeMillis(),
                                               maxTime : Long = Long.MAX_VALUE,
                                               current : CSPNode<VAR, DOMAIN>,
                                               sortedVariables : List<VAR>) :
    CSPAgent<VAR, DOMAIN>(domainMap, start, maxTime, with(Stack<CSPNode<VAR, DOMAIN>>()) { this.push(current); this }, sortedVariables)