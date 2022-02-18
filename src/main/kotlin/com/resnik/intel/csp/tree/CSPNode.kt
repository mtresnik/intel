package com.resnik.intel.csp.tree

internal open class CSPNode<VAR, DOMAIN>(val variable : VAR, val value : DOMAIN, var parent : CSPNode<VAR, DOMAIN>? = null) {

    val map : Map<VAR, DOMAIN>

    init {
        if(parent == null) {
            map = mutableMapOf(variable to value)
        } else {
            val parentMap = LinkedHashMap(parent!!.map)
            parentMap[this.variable] = this.value
            map = parentMap
        }
    }

}