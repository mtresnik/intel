package com.resnik.intel.forest

class Attribute<T>(val name: String, val type: Class<T>){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attribute<*>

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "($name: $type)"
    }

    companion object {

        fun <T> fromClass(clazz: Class<T>) : Array<Attribute<*>> = clazz.declaredFields.map { field -> Attribute(field.name, field.type) }.toTypedArray()

    }

}