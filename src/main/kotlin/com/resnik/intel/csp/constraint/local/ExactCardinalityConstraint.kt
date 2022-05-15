package com.resnik.intel.csp.constraint.local

class ExactCardinalityConstraint<VAR, DOMAIN>(variables: List<VAR>, maxCount: Int, domain: DOMAIN) :
    CardinalityConstraint<VAR, DOMAIN>(variables, maxCount, domain) {

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return assignment.values.count { it == domain } == maxCount
    }

}