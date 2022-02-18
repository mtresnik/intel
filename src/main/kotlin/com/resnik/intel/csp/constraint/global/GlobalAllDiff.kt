package com.resnik.intel.csp.constraint.global

class GlobalAllDiff<VAR, DOMAIN> : GlobalConstraint<VAR, DOMAIN> {

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return assignment.keys.size == assignment.values.toSet().size
    }

}