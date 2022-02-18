package com.resnik.intel.csp

import com.resnik.intel.csp.constraint.global.GlobalAllDiff
import com.resnik.intel.csp.constraint.local.LocalAllDiff
import com.resnik.intel.csp.tree.CSPTree
import mappedTo
import org.junit.Test

class TestAllDiff {

    @Test
    fun allDiff() {
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

        val domainMap = mapOf(A to domainA, B to domainB, C to domainC, D to domainD, E to domainE)

        val csp = CSPTree(domainMap)
        csp.addConstraint(GlobalAllDiff())
        val solution = csp.findAllSolutions()
        println("Solution: $solution")
        println("Time: ${csp.finalTime()}")
    }


}