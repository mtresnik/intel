package com.resnik.intel.csp.constraint.local

import com.resnik.intel.csp.constraint.ReusableConstraint

abstract class MinimumConstraint<VAR, DOMAIN>(variables: List<VAR>) : LocalHeuristicConstraint<VAR, DOMAIN>(variables),
    ReusableConstraint<VAR, DOMAIN> {

    private var minValue = Double.MAX_VALUE

    override fun isPossiblySatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return evaluate(assignment) <= minValue
    }

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        val tempValue = evaluate(assignment)
        if (tempValue <= minValue) {
            minValue = tempValue
            return true
        }
        return false
    }

}