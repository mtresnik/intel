package com.resnik.intel.csp.tree.async

import com.resnik.intel.csp.MultiCSP
import com.resnik.intel.csp.preprocessors.CSPPreprocessor
import com.resnik.intel.csp.tree.CSPAgent

abstract class CSPAsyncBase<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                         val maxTime : Long = Long.MAX_VALUE,
                                         val maxThreads : Int = MAX_THREAD_COUNT,
                                         sortVariables : Boolean = SORT_VARIABLES_DEFAULT,
                                         preprocessors : List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf())
    : MultiCSP<VAR, DOMAIN>(domainMap, sortVariables, preprocessors) {

    override fun isAsync(): Boolean = true

    internal abstract fun constructAgents() : List<CSPAgent<VAR, DOMAIN>>

    companion object {
        const val MAX_THREAD_COUNT = 200
    }

}