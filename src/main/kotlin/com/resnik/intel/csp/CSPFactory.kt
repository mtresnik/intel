package com.resnik.intel.csp

import com.resnik.intel.csp.CSPBase.Companion.SORT_VARIABLES_DEFAULT
import com.resnik.intel.csp.preprocessors.CSPPreprocessor
import com.resnik.intel.csp.tree.CSPTree
import com.resnik.intel.csp.tree.async.CSPAsyncBase
import com.resnik.intel.csp.tree.async.CSPCoroutine
import com.resnik.intel.csp.tree.async.CSPDomainAsync
import com.resnik.intel.csp.tree.async.CSPInferredAsync
import java.math.BigDecimal

object CSPFactory {

    private val REPEAT_THRESHOLD = BigDecimal(13).pow(13)

    fun <VAR, DOMAIN> createCSP(
        domainMap: Map<VAR, List<DOMAIN>>,
        maxTime: Long = Long.MAX_VALUE,
        sortVariables: Boolean = SORT_VARIABLES_DEFAULT,
        isRepeatable: Boolean = false,
        preprocessors: List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf()
    ): MultiCSP<VAR, DOMAIN> {
        // Based on number of variables and max domain size, estimate full tree
        val numVariables = domainMap.keys.size
        val maxDomainInt = domainMap.values.maxOfOrNull { it.size } ?: return CSPTree(domainMap, maxTime)
        val maxDomain = BigDecimal(maxDomainInt)
        val maxChildren = maxDomain.pow(numVariables)
        val maxThreadCount = BigDecimal(CSPAsyncBase.MAX_THREAD_COUNT)
        // Default to async bc it's faster for large numbers of items
        if (maxChildren > maxThreadCount) {
            return if (isRepeatable || maxChildren > REPEAT_THRESHOLD) {
                // More efficient for large ~ 13^13 datasets / repeating
                CSPCoroutine(domainMap, maxTime, sortVariables = sortVariables, preprocessors = preprocessors)
            } else {
                CSPInferredAsync(domainMap, maxTime, sortVariables = sortVariables, preprocessors = preprocessors)
            }
        }
        // Find out case where CSPDomain is needed... maybe when maxDomain > numVariables?
        if (maxDomain > BigDecimal(numVariables)) {
            return CSPDomainAsync(domainMap, maxTime, sortVariables = sortVariables, preprocessors = preprocessors)
        }
        return CSPTree(domainMap, maxTime, sortVariables = sortVariables)
    }

}