package com.resnik.intel.forest

class DecisionTree(val schema: Schema, val attribute: Attribute<*>) {

    private val childMap: MutableMap<Any, Dataset> = mutableMapOf()
    private val childTrees: MutableMap<Any, DecisionTree> = mutableMapOf()

    operator fun get(any: Any): Dataset? = childMap[any]

    operator fun set(any: Any, dataset: Dataset) {
        childMap[any] = dataset
    }

    fun traverse(vararg values: Any): Any {
        if (values.isEmpty()) {
            return this
        }
        var currTree = this
        values.forEach { any ->
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