package com.resnik.intel.csp.constraint.local

import com.resnik.intel.csp.constraint.global.GlobalConstraint

class LocalizedGlobalConstraint<VAR, DOMAIN>(
    variables: List<VAR>,
    private val globalConstraint: GlobalConstraint<VAR, DOMAIN>
) :
    LocalConstraint<VAR, DOMAIN>(variables) {

    override fun isPossiblySatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return globalConstraint.isSatisfied(assignment)
    }

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return globalConstraint.isSatisfied(assignment)
    }
}