package com.resnik.intel.csp.constraint.local

class IntSumConstraint<VAR>(variables : List<VAR>, maxValue : Int)
    : LocalSumConstraint<VAR, Int>(variables, maxValue) {

    override fun add(one: Int, two: Int): Int = one + two

    override fun compare(o1: Int?, o2: Int?): Int = o1!!.compareTo(o2!!)

}