package com.resnik.intel.csp

import com.resnik.intel.csp.preprocessors.CSPPreprocessor

@Deprecated("Less efficient than using a CSPAgent")
class IterativeCSP<VAR, DOMAIN>(domainMap : Map<VAR, List<DOMAIN>>,
                                sortVariables : Boolean = SORT_VARIABLES_DEFAULT,
                                preprocessors : List<CSPPreprocessor<VAR, DOMAIN>> = mutableListOf())
    : CSPBase<VAR, DOMAIN>(domainMap, sortVariables, preprocessors){

    fun findAllSolutions() : List<Map<VAR, DOMAIN>> {
        val retSet = mutableListOf<Map<VAR, DOMAIN>>()
        val parents = mutableListOf<Pair<VAR, Map<VAR, DOMAIN>>>()
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return retSet
        val firstDomain = domainMap[first] ?: return retSet
        firstDomain.forEach { domain -> parents.add(first to mutableMapOf(first to domain)) }
        while(parents.isNotEmpty()) {
            parents.removeAll { (v, assignment) ->
                if(assignment.size == variables.size && isLocallyConsistent(v, assignment)) retSet.add(assignment)
                assignment.size >= variables.size || !isLocallyConsistent(v, assignment)
            }
            if(parents.isEmpty()) break
            // Get next set of children
            val (_, assignment) = parents.first()
            parents.removeAt(0)
            val nextVar = variables.firstOrNull { it !in assignment } ?: continue
            val nextDomain = domainMap[nextVar] ?: continue
            nextDomain.forEach { domain ->
                val mapClone = LinkedHashMap(assignment)
                mapClone[nextVar] = domain
                val p = nextVar to mapClone
                if(isLocallyConsistent(nextVar, mapClone) && p !in parents) parents.add(p)
            }
        }
        return retSet
    }

    override fun getFirstSolution(): Map<VAR, DOMAIN>? {
        var ret : Map<VAR, DOMAIN>? = null
        val parents = mutableListOf<Pair<VAR, Map<VAR, DOMAIN>>>()
        val variables = getVariables()
        val first = variables.firstOrNull() ?: return null
        val firstDomain = domainMap[first] ?: return null
        firstDomain.forEach { domain -> parents.add(first to mutableMapOf(first to domain)) }
        while(parents.isNotEmpty()) {
            parents.removeAll { (v, assignment) ->
                if(assignment.size == variables.size && isLocallyConsistent(v, assignment)) {
                    ret = assignment
                }
                assignment.size >= variables.size || !isLocallyConsistent(v, assignment)
            }
            if(ret != null) return ret
            if(parents.isEmpty()) break
            // Get next set of children
            val (_, assignment) = parents.first()
            parents.removeAt(0)
            val nextVar = variables.firstOrNull { it !in assignment } ?: continue
            val nextDomain = domainMap[nextVar] ?: continue
            nextDomain.forEach { domain ->
                val mapClone = LinkedHashMap(assignment)
                mapClone[nextVar] = domain
                val p = nextVar to mapClone
                if(isLocallyConsistent(nextVar, mapClone) && p !in parents) parents.add(p)
            }
        }
        return null
    }


}