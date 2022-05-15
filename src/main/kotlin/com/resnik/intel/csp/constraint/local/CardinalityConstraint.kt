package com.resnik.intel.csp.constraint.local

open class CardinalityConstraint<VAR, DOMAIN>(variables: List<VAR>, val maxCount: Int, val domain: DOMAIN) :
    LocalConstraint<VAR, DOMAIN>(variables) {

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return isPossiblySatisfied(assignment)
    }

    override fun isPossiblySatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return assignment.values.count { it == domain } <= maxCount
    }

}