package com.resnik.intel.csp

import com.resnik.intel.csp.collection.VarDomainCollection
import com.resnik.intel.csp.collection.VarDomainMapCollection
import com.resnik.intel.csp.constraint.Constraint
import com.resnik.intel.csp.constraint.ReusableConstraint
import com.resnik.intel.csp.constraint.global.GlobalConstraint
import com.resnik.intel.csp.constraint.local.LocalConstraint
import com.resnik.intel.csp.preprocessors.CSPPreprocessor

abstract class CSPBase<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                    private val sortVariables : Boolean = SORT_VARIABLES_DEFAULT,
                                    private val preprocessors : List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf())
    : TimedCSPAlgorithm(), VarDomainCollection<VAR, DOMAIN> {

    internal var domainMap : Map<VAR, List<DOMAIN>> = domainMap.toMutableMap()
    private val variables : List<VAR> = domainMap.keys.toList()
    private val globalConstraints = mutableListOf<GlobalConstraint<VAR, DOMAIN>>()
    private val localConstraints = mutableMapOf<VAR, MutableList<LocalConstraint<VAR, DOMAIN>>>()
    private var mapStorage : VarDomainCollection<VAR, DOMAIN>? = null

    @Throws(CSPException::class)
    fun preprocess() { preprocessors.forEach { it.preprocess(this) }; println("Preprocessing complete in: ${preprocessors.sumOf { it.finalTime() }}") }

    override fun finalTime(): Long = super.finalTime() + preprocessors.sumOf { it.finalTime() }

    override fun getVariables() : List<VAR> {
        return mapStorage?.getVariables() ?: if(!sortVariables) variables else getSortedVariables(variables, localConstraints)
    }

    override fun getVariableAt(variableIndex: Int): VAR {
        return getVariables()[variableIndex]
    }

    override fun getDomainsAt(variableIndex: Int): List<DOMAIN> {
        return mapStorage?.getDomainsAt(variableIndex) ?: getDomains(getVariableAt(variableIndex))
    }

    override fun getDomains(variable: VAR): List<DOMAIN> {
        return mapStorage?.getDomains(variable) ?: domainMap[variable]!!
    }

    override fun contains(variable: VAR): Boolean {
        return mapStorage?.contains(variable) ?: (variable in domainMap)
    }

    fun isCollected() : Boolean = mapStorage != null

    fun collect() {
        val vars = getVariables()
        mapStorage = VarDomainMapCollection(getVariables(), getDomainMap(vars))
    }

    protected fun getDomainMap(vars : List<VAR> = getSortedVariables(variables, localConstraints)) : Map<VAR, List<DOMAIN>> {
        return if(sortVariables) {
            // Sort map inserts
            vars
                .associateWith { variable -> domainMap[variable]!! }
                .toMutableMap()
        } else {
            domainMap.toMutableMap()
        }
    }

    fun addConstraint(constraint : Constraint<VAR, DOMAIN>) {
        when(constraint) {
            is LocalConstraint -> { addLocalConstraint(constraint) }
            is GlobalConstraint -> { addGlobalConstraint(constraint) }
            else -> {}
        }
    }

    fun addConstraints(constraints : Collection<Constraint<VAR, DOMAIN>>) {
        addAllLocalConstraints(constraints.filterIsInstance<LocalConstraint<VAR, DOMAIN>>())
        addAllGlobalConstraints(constraints.filterIsInstance<GlobalConstraint<VAR, DOMAIN>>())
    }

    protected fun addLocalConstraint(constraint: LocalConstraint<VAR, DOMAIN>) {
        addLocalConstraintInternal(constraint)
        collect()
    }

    protected fun addLocalConstraintInternal(constraint: LocalConstraint<VAR, DOMAIN>) {
        for(variable in constraint.variables) {
            if(variable in this.variables) {
                val constraintLookup : MutableList<LocalConstraint<VAR, DOMAIN>> = this.localConstraints[variable] ?: mutableListOf()
                constraintLookup.add(constraint)
                localConstraints[variable] = constraintLookup
            }
        }
    }

    protected fun addAllLocalConstraints(constraints : Collection<LocalConstraint<VAR, DOMAIN>>) {
        constraints.forEach { constraint -> this.addLocalConstraintInternal(constraint) }
        collect()
    }

    protected fun addAllLocalConstraints(other : CSPBase<VAR, DOMAIN>) {
        this.addAllLocalConstraints(other.localConstraints.values.flatten())
    }

    internal fun getLocalConstraints() = this.localConstraints

    protected fun addGlobalConstraint(constraint: GlobalConstraint<VAR, DOMAIN>) {
        this.globalConstraints.add(constraint)
    }

    protected fun addAllGlobalConstraints(constraints : Collection<GlobalConstraint<VAR, DOMAIN>>) {
        this.globalConstraints.addAll(constraints)
    }

    protected fun addAllGlobalConstraints(other : CSPBase<VAR, DOMAIN>) {
        this.addAllGlobalConstraints(other.globalConstraints)
    }

    internal fun addAllConstraintsFrom(other : CSPBase<VAR, DOMAIN>) {
        this.addAllLocalConstraints(other)
        this.addAllGlobalConstraints(other)
    }

    protected fun addAllConstraintsTo(other : CSPBase<VAR, DOMAIN>) {
        other.addAllConstraintsFrom(this)
    }

    protected fun addAllConstraintsTo(others : Collection<CSPBase<VAR, DOMAIN>>) {
        val localConstraintsFlattened = this.localConstraints.values.flatten()
        others.forEach { it.addAllLocalConstraints(localConstraintsFlattened); it.addAllGlobalConstraints(this.globalConstraints) }
    }

    // Promotes Exploration
    fun isLocallyConsistent(variable : VAR, assignment : Map<VAR, DOMAIN>) : Boolean {
        if(localConstraints.isEmpty()) return globalConstraints.isNotEmpty()
        val localConstraints = localConstraints[variable] ?: return false
        return localConstraints.all { constraint -> constraint.isPossiblySatisfied(assignment) }
    }

    // Saves Memory
    fun isConsistent(variable : VAR, assignment : Map<VAR, DOMAIN>) : Boolean {
        return isGloballyConsistent(assignment) && isAbsolutelyConsistent(variable, assignment)
    }

    private fun isGloballyConsistent(assignment : Map<VAR, DOMAIN>) : Boolean {
        return globalConstraints.all { it.isSatisfied(assignment) }
    }

    private fun isAbsolutelyConsistent(variable : VAR, assignment : Map<VAR, DOMAIN>) : Boolean {
        if(localConstraints.isEmpty()) return globalConstraints.isNotEmpty()
        val localConstraints = localConstraints[variable] ?: return false
        return localConstraints.all { constraint -> constraint.isSatisfied(assignment) }
    }

    protected fun isReusablyConsistent(assignment : Map<VAR, DOMAIN>) : Boolean {
        val reusableConstraints = mutableListOf<ReusableConstraint<VAR, DOMAIN>>()
        reusableConstraints.addAll(localConstraints.values.flatten().filterIsInstance<ReusableConstraint<VAR, DOMAIN>>())
        reusableConstraints.addAll(globalConstraints.filterIsInstance<ReusableConstraint<VAR, DOMAIN>>())
        return reusableConstraints.all { constraint -> constraint.isSatisfied(assignment) }
    }

    open fun isAsync() : Boolean = false

    abstract fun getFirstSolution() : Map<VAR, DOMAIN>?

    companion object {

        const val SORT_VARIABLES_DEFAULT : Boolean = true

        // Sort based on the highest amount of information expected to gain from exploring... so highest constraint first
        private fun <VAR, DOMAIN> getSortedVariables(variables : List<VAR>,
                                             constraints : Map<VAR, MutableList<LocalConstraint<VAR, DOMAIN>>>) : List<VAR> =
            if(constraints.isEmpty()) variables else variables.sortedBy { constraints[it]?.size ?: 0 }.reversed()

        internal fun <VAR, DOMAIN> convertToDomainMap(variables : List<VAR>, domains : List<DOMAIN>) : Map<VAR, List<DOMAIN>> {
            return variables.associateWith { domains }
        }
    }

}