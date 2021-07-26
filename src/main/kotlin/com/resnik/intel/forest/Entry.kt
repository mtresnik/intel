package com.resnik.intel.forest

class Entry(val schema: Schema, vararg toSet : Any) {

    val values: Array<Any> = Array(schema.attributes.size){}

    operator fun get(attribute: Attribute<*>) : Any = values[schema.indexOf(attribute)]

    operator fun set(attribute: Attribute<*>, value: Any) {
        values[schema.indexOf(attribute)] = value
    }

    operator fun set(index: Int, value: Any) {
        values[index] = value
    }

    fun remove(attribute: Attribute<*>, newSchema: Schema) : Entry {
        val newValues = mutableListOf<Any>()
        newSchema.attributes.forEach {
            newValues.add(this[it])
        }
        return Entry(newSchema, *newValues.toTypedArray())
    }

    init {
        toSet.forEachIndexed { index, any ->
            this[index] = toSet[index]
        }
    }

    override fun toString(): String {
        return values.contentDeepToString()
    }

}