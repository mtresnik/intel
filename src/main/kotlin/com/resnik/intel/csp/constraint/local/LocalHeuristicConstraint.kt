package com.resnik.intel.csp.constraint.local

import com.resnik.intel.csp.constraint.HeuristicConstraint

abstract class LocalHeuristicConstraint<VAR, DOMAIN>(variables: List<VAR>) : LocalConstraint<VAR, DOMAIN>(variables),
    HeuristicConstraint<VAR, DOMAIN>