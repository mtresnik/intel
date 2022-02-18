package com.resnik.intel.csp.preprocessors

import com.resnik.intel.csp.CSPBase
import com.resnik.intel.csp.CSPException
import com.resnik.intel.csp.constraint.local.LocalConstraint
import java.util.ArrayDeque

/**
 * Arc Consistency Algorithm #3
 * Refs https://en.wikipedia.org/wiki/AC-3_algorithm
 * */
class AC3Preprocessor<VAR, DOMAIN> : CSPPreprocessor<VAR, DOMAIN>() {

    private val currentDomain = mutableMapOf<VAR, MutableList<DOMAIN>>()

    override fun preprocess(csp : CSPBase<VAR, DOMAIN>) {
        onStart()
        val originalDomain = csp.domainMap
        val originalConstraints = csp.getLocalConstraints()
        val variables = csp.getVariables()

        val unaryConstraints = mutableMapOf<VAR, MutableList<LocalConstraint<VAR, DOMAIN>>>()
        val binaryConstraints = mutableMapOf<VAR, MutableList<LocalConstraint<VAR, DOMAIN>>>()
        // TODO : What to do with ternary / etc Constraints?
        variables.forEach { variable -> unaryConstraints[variable] = mutableListOf(); binaryConstraints[variable] = mutableListOf() }

        originalConstraints.forEach { (variable, constraints) ->
            constraints.forEach { constraint ->
                if(constraint.isUnary()) {
                    unaryConstraints[variable]!!.add(constraint)
                } else {
                    binaryConstraints[variable]!!.add(constraint)
                }
            }
        }
        // Find all domains that work for the unary constraints
        for(variable in variables) {
            currentDomain[variable] = originalDomain[variable]!!.filter { domain ->
                unaryConstraints[variable]!!.all { constraint ->
                    constraint.isPossiblySatisfied(mapOf(variable to domain))
                }
            }.toMutableList()
        }
        // Find all domains that work for binary constraints
        val workList = ArrayDeque(binaryConstraints.flatMap { (_, constraints) -> constraints })
        println("Worklist size: ${workList.size}")
        do {
            val constraint = workList.poll()
            if(constraint != null) {
                val (x, y) = (constraint.variables[0] to constraint.variables[1])
                val shared = getSharedConstraints(x, y, binaryConstraints)
                if(arcReduce(x, y, shared) && currentDomain[x].isNullOrEmpty())
                    onException(CSPException.EmptyDomainException("Variable: $x has an empty domain."))
            }
        } while (workList.isNotEmpty())

        // Set values for CSP
        val newDomain = mutableMapOf<VAR, List<DOMAIN>>()
        currentDomain.forEach { (v, domain) -> newDomain[v] = domain.toList() }
        csp.domainMap = newDomain
        onFinish()
    }

    private fun arcReduce(x : VAR, y : VAR, binaryConstraints : List<LocalConstraint<VAR, DOMAIN>>) : Boolean {
        var change = false
        currentDomain[x]!!.indices.reversed().forEach { index ->
            val vx = currentDomain[x]!![index]
            val vy = currentDomain[y]!!.firstOrNull { yDomain ->
                binaryConstraints.all { constraint ->
                    constraint.isPossiblySatisfied(mapOf(x to vx, y to yDomain))
                }
            }
            if(vy == null) {
                currentDomain[x]!!.removeAt(index)
                change = true
            }
        }
        return change
    }

    companion object {

        private fun <VAR, DOMAIN> getSharedConstraints(x : VAR, y : VAR, allConstraints : Map<VAR, MutableList<LocalConstraint<VAR, DOMAIN>>>) : List<LocalConstraint<VAR, DOMAIN>> {
            val retList = mutableListOf<LocalConstraint<VAR, DOMAIN>>()
            val listX = allConstraints[x]!!
            val listY = allConstraints[y]!!
            retList.addAll(listX.filter { constraint -> constraint.variables.contains(y) })
            retList.addAll(listY.filter { constraint -> constraint.variables.contains(x) })
            return retList
        }

    }


}