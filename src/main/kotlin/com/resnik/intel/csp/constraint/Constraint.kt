package com.resnik.intel.csp.constraint

interface Constraint<VAR, DOMAIN> {

    fun isSatisfied(assignment: Map<VAR, DOMAIN>) : Boolean

}