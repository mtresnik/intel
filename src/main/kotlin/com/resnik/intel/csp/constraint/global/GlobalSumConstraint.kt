package com.resnik.intel.csp.constraint.global

import com.resnik.intel.csp.constraint.SumConstraint

abstract class GlobalSumConstraint<VAR, DOMAIN>(private val maxValue : DOMAIN) : GlobalConstraint<VAR, DOMAIN>, SumConstraint<VAR, DOMAIN> {

    override fun isSatisfied(assignment: Map<VAR, DOMAIN>): Boolean {
        return this.compare(sum(assignment.values), maxValue) == 0
    }

}