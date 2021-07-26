package com.resnik.intel.forest

class Schema(vararg val attributes: Attribute<*>) {

    fun indexOf(attribute: Attribute<*>) = attributes.indexOf(attribute)

    operator fun contains(attribute: Attribute<*>) : Boolean = attribute in attributes

    override fun toString(): String = attributes.contentToString()

    operator fun get(name: String) = attributes.first { it.name == name }

    fun remove(attribute: Attribute<*>) : Schema {
        val newAttributes = attributes.toMutableList()
        newAttributes.remove(attribute)
        return Schema(*newAttributes.toTypedArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Schema

        if (!attributes.contentEquals(other.attributes)) return false

        return true
    }

    override fun hashCode(): Int {
        return attributes.contentHashCode()
    }

    companion object {

        fun <T> fromClass(clazz: Class<T>) : Schema = Schema(*Attribute.fromClass(clazz))

    }

}