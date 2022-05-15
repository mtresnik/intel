package com.resnik.intel.forest

class DecisionTree(val schema: Schema, val attribute: Attribute<*>) {

    val childMap: MutableMap<Any, Dataset> = mutableMapOf()
    val childTrees: MutableMap<Any, DecisionTree> = mutableMapOf()

    operator fun get(any: Any): Dataset? = childMap[any]

    operator fun set(any: Any, dataset: Dataset) {
        childMap[any] = dataset
    }

    fun isTraversalValid(vararg values: Any): Boolean = traverse(*values) !is DecisionTree

    fun traverse(vararg values: Any): Any {
        if (values.isEmpty()) {
            return this
        }
        var currTree = this
        values.forEachIndexed { index, any ->
            val currDataset = currTree[any]
            if (currDataset != null) {
                if (currDataset.converges()) {
                    return currDataset.convergesTo()
                }
                if (currTree.childTrees[any] == null) {
                    currTree.childTrees[any] = currTree.childMap[any]!!.buildTree()
                }
                currTree = currTree.childTrees[any]!!
            }
        }
        return currTree
    }

}