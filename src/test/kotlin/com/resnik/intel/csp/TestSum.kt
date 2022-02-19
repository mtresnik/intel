package com.resnik.intel.csp

import com.resnik.intel.csp.constraint.local.IntSumConstraint
import com.resnik.intel.csp.tree.async.CSPCoroutine
import org.junit.Test

class TestSum {

    @Test
    fun testSum1() {
        val domainA = listOf(3, 4, 5, 6)
        val domainB = listOf(3, 4)
        val domainC = listOf(2, 3, 4, 5)
        val domainD = listOf(2, 3, 4)
        val domainE = listOf(3, 4)

        val A = "A"
        val B = "B"
        val C = "C"
        val D = "D"
        val E = "E"
        val variables = listOf(A, B, C, D, E)
        val domains = listOf(domainA, domainB, domainC, domainD, domainE)
        val domainMap = mapOf(
            A to domainA,
            B to domainB,
            C to domainC,
            D to domainD,
            E to domainE)

        val csp = CSPCoroutine(domainMap)
        csp.addConstraint(IntSumConstraint(variables, 22))
        val solutions = csp.findAllSolutions()
        println("Num Solutions: ${solutions.size}")
        println("Time: ${csp.finalTime()}")

    }

}