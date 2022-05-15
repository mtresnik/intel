package com.resnik.intel.csp

import com.resnik.intel.csp.constraint.local.LocalAllDiff
import com.resnik.intel.csp.constraint.local.MinimumConstraint
import com.resnik.intel.csp.tree.async.CSPCoroutine
import org.junit.Test

class TestMinMax {

    class SumMinConstraint<VAR>(variables: List<VAR>) : MinimumConstraint<VAR, Int>(variables) {

        override fun evaluate(assignment: Map<VAR, Int>): Double {
            return assignment.values.sum().toDouble()
        }

    }

    @Test
    fun testMin() {
        val domainA = (1..10).toList()
        val domainB = (1..10).toList()
        val domainC = (1..10).toList()
        val domainD = (1..10).toList()
        val domainE = (1..10).toList()

        val A = "A"
        val B = "B"
        val C = "C"
        val D = "D"
        val E = "E"
        val variables = listOf(A, B, C, D, E)
        val domains = listOf(domainA, domainB, domainC, domainD, domainE)

        val domainMap = mapOf(A to domainA, B to domainB, C to domainC, D to domainD, E to domainE)
        val csp = CSPCoroutine(domainMap)
        csp.addConstraint(LocalAllDiff(listOf(A, B, C)))
        csp.addConstraint(SumMinConstraint(variables))

        val solutions = csp.findAllSolutions()
        println("Num Solutions: ${solutions.size}")
        println("Time: ${csp.finalTime()}")
        solutions.forEach { println(it.values) }

    }

}