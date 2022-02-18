package com.resnik.intel.csp

import com.resnik.intel.csp.constraint.local.ExactCardinalityConstraint
import com.resnik.intel.csp.tree.async.CSPInferredAsync
import mappedTo
import org.junit.Test

class TestCardinal {

    @Test
    fun testCardinal1() {
        val variables = (0..32).toList()
        val domains = listOf(0, 1)
        val csp = CSPInferredAsync(variables.mappedTo(domains))
        csp.addConstraint(ExactCardinalityConstraint(variables, 5, 1))
        val solutions = csp.findAllSolutions()
        println("All Solutions: ${solutions.size}")
        println("Time Taken: ${csp.finalTime()}")
    }

}