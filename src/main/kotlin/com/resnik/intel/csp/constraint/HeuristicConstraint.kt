package com.resnik.intel.csp.constraint

interface HeuristicConstraint<VAR, DOMAIN> : Constraint<VAR, DOMAIN> {

    fun evaluate(assignment: Map<VAR, DOMAIN>): Double

}