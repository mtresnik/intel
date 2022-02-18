package com.resnik.intel.csp

import com.resnik.intel.csp.constraint.local.LocalConstraint
import com.resnik.intel.csp.tree.CSPTree
import com.resnik.intel.csp.tree.async.CSPDomainAsync
import org.junit.Test


class TestMapColoring {

    class MCC(private val from : String, private val to : String) : LocalConstraint<String, String>(listOf(from, to)){

        override fun isPossiblySatisfied(assignment: Map<String, String>): Boolean {
            if(from !in assignment || to !in assignment)
                return true
            return assignment[from]!! != assignment[to]!!
        }

    }

    val WA = "Western Australia"
    val NT = "Northern Territory"
    val SA = "South Australia"
    val Q  = "Queensland"
    val NSW= "New South Wales"
    val V  = "Victoria"
    val T  = "Tasmania"

    val red     = "red"
    val green   = "green"
    val blue    = "blue"

    val colors = listOf(red, green, blue)

    val variables = listOf(WA, NT, SA, Q, NSW, V, T)
    val domains = mutableMapOf<String, List<String>>()
    val constraints = mutableListOf<MCC>()

    init {
        variables.forEach { variable -> domains[variable] = colors }
        constraints.add(MCC(WA, NT))
        constraints.add(MCC(WA, SA))

        constraints.add(MCC(SA, NT))

        constraints.add(MCC(Q, NT))
        constraints.add(MCC(Q, SA))
        constraints.add(MCC(Q, NSW))

        constraints.add(MCC(NSW, SA))

        constraints.add(MCC(V, SA))
        constraints.add(MCC(V, NSW))
        constraints.add(MCC(V, T))
    }

    @Test
    fun testCSPAustraliaLinear() {
        val start = System.currentTimeMillis()
        val csp = CSPTree(domains)
        csp.addConstraints(constraints)
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
    }

    @Test
    fun testCSPAustraliaAsync() {
        val start = System.currentTimeMillis()
        val csp = CSPDomainAsync(domains)
        csp.addConstraints(constraints)
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
    }

    @Test
    fun testCSPAustraliaProbabilities() {
        val start = System.currentTimeMillis()
        val csp = CSPDomainAsync(domains)
        csp.addConstraints(constraints)
        val probabilities = csp.getProbabilities()
        println(probabilities)
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
    }




}