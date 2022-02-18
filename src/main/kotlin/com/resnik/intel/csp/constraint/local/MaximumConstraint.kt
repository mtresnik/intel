package com.resnik.intel.csp.constraint.local

import com.resnik.intel.csp.constraint.ReusableConstraint

abstract class MaximumConstraint<VAR, DOMAIN>(variables : List<VAR>)
    : LocalHeuristicConstraint<VAR, DOMAIN>(variables), ReusableConstraint<VAR, DOMAIN> {

    private var maxValue = -Double.MAX_VALUE

    override fun isPossiblySatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return evaluate(assignment) >= maxValue
    }

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        val tempValue = evaluate(assignment)
        if(tempValue >= maxValue) {
            maxValue = tempValue
            return true
        }
        return false
    }

}