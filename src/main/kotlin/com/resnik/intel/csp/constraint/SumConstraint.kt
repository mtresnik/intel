package com.resnik.intel.csp.constraint

interface SumConstraint<VAR, DOMAIN> : Constraint<VAR, DOMAIN>, Comparator<DOMAIN> {

    fun add(one : DOMAIN, two : DOMAIN) : DOMAIN

    fun sum(collection : Collection<DOMAIN>) : DOMAIN {
        var sum : DOMAIN
        val values = collection.toList()
        when (values.size) {
            1 -> sum = values.first()
            2 -> sum = values[1]
            else -> {
                sum = values.first()
                (1 .. values.lastIndex).forEach { index ->
                    sum = add(sum, values[index])
                }
            }
        }
        return sum
    }

}