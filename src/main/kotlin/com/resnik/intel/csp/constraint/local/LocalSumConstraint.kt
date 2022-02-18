package com.resnik.intel.csp.constraint.local

import com.resnik.intel.csp.constraint.SumConstraint

abstract class LocalSumConstraint<VAR, DOMAIN>(variables : List<VAR>,
                                           private val maxValue : DOMAIN)
    : LocalConstraint<VAR, DOMAIN>(variables), SumConstraint<VAR, DOMAIN> {

    override fun isPossiblySatisfied(assignment: Map<VAR, DOMAIN>) : Boolean {
        return this.compare(sum(assignment.values), maxValue) <= 0
    }

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>) : Boolean {
        return this.compare(sum(assignment.values), maxValue) == 0
    }

}