package com.resnik.intel.csp.constraint

/** Used to find locally optimal assignment, reapplied to global set of possible solutions. */
interface ReusableConstraint<VAR, DOMAIN> : Constraint<VAR, DOMAIN>