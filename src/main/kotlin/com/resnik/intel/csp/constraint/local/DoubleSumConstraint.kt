package com.resnik.intel.csp.constraint.local

class DoubleSumConstraint<VAR>(variables : List<VAR>, maxValue : Double)
    : LocalSumConstraint<VAR, Double>(variables, maxValue) {

    override fun add(one: Double, two: Double): Double = one + two

    override fun compare(o1: Double?, o2: Double?): Int = o1!!.compareTo(o2!!)

}