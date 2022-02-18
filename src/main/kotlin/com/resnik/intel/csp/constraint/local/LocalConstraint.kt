package com.resnik.intel.csp.constraint.local

import com.resnik.intel.csp.constraint.Constraint

abstract class LocalConstraint<VAR, DOMAIN> (val variables : List<VAR>) : Constraint<VAR, DOMAIN> {

    // Space exploration
    abstract fun isPossiblySatisfied(assignment : Map<VAR, DOMAIN>) : Boolean

    // Default to true
    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean = true

    open fun isUnary() : Boolean = variables.size == 1

    open fun isBinary() : Boolean = variables.size == 2

}