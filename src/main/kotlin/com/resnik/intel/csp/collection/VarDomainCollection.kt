package com.resnik.intel.csp.collection

interface VarDomainCollection<VAR, DOMAIN> {

    fun getVariableAt(variableIndex: Int): VAR

    fun getDomainsAt(variableIndex: Int): List<DOMAIN>

    fun getDomains(variable: VAR): List<DOMAIN>

    fun getVariables(): List<VAR>

    operator fun contains(variable: VAR): Boolean

}