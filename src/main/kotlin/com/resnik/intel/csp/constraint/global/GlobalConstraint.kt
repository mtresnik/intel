package com.resnik.intel.csp.constraint.global

import com.resnik.intel.csp.constraint.Constraint
import com.resnik.intel.csp.constraint.local.LocalizedGlobalConstraint

interface GlobalConstraint<VAR, DOMAIN> : Constraint<VAR, DOMAIN> {

    fun toLocal(variables: List<VAR>) = LocalizedGlobalConstraint(variables, this)

}