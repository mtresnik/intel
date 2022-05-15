package com.resnik.intel.csp.collection

class VarDomainMapCollection<VAR, DOMAIN>(
    private val variables: List<VAR>,
    val domainMap: Map<VAR, List<DOMAIN>>
) : VarDomainCollection<VAR, DOMAIN> {

    private val domains = domainMap.values.toList()

    override fun getVariableAt(variableIndex: Int): VAR = variables[variableIndex]

    override fun getDomainsAt(variableIndex: Int): List<DOMAIN> = domains[variableIndex]

    override fun getDomains(variable: VAR): List<DOMAIN> = domainMap[variable]!!

    override fun getVariables(): List<VAR> = variables

    override fun contains(variable: VAR): Boolean = variable in domainMap

}