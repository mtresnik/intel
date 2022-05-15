package com.resnik.intel.csp.constraint.local

class LocalAllDiff<VAR, DOMAIN>(variables: List<VAR>) : LocalConstraint<VAR, DOMAIN>(variables) {

    override fun isPossiblySatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return assignment.keys.size == assignment.values.toSet().size
    }

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return isPossiblySatisfied(assignment)
    }

}