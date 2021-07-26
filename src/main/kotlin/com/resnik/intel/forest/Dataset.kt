package com.resnik.intel.forest

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.math.log

class Dataset(val schema: Schema, val targetAttribute: Attribute<*>) : ArrayList<Entry>() {

    init {
        if(targetAttribute !in schema){
            throw IllegalArgumentException("Attribute $targetAttribute is not in the Schema!")
        }
    }

    fun converges() : Boolean = this.getUniqueValues(targetAttribute).size == 1

    fun convergesTo() : Any = this.getUniqueValues(targetAttribute).first()!!

    fun buildTree() : DecisionTree {
        if(converges()){
            throw IllegalStateException("Nothing to decide on.")
        }
        val highestAttribute = getHighestGainAttribute() ?: throw IllegalStateException("All attributes have equal entropy.")
        val ret = DecisionTree(schema, highestAttribute.key)
        val subDatasets = split(highestAttribute.key)
        val uniqueValues = getUniqueValues(highestAttribute.key)
        uniqueValues.forEachIndexed{index, any ->
            ret[any!!] = subDatasets[index]
        }

        return ret
    }

    fun split(attribute: Attribute<*>) : List<Dataset> {
        val retList = mutableListOf<Dataset>()
        val newSchema = schema.remove(attribute)
        val uniqueValues = getUniqueValues(attribute)
        uniqueValues.forEach { value ->
            val matchingEntries = this.filter(attribute, value!!)
            val newEntries = matchingEntries.map { entry -> entry.remove(attribute, newSchema) }
            val newDataset = Dataset(newSchema, targetAttribute)
            newDataset.addAll(newEntries)
            retList.add(newDataset)
        }
        return retList
    }

    fun targetEntropy() : Double {
        val uniqueValues = getUniqueValues(targetAttribute)
        val numMatching = count(targetAttribute, uniqueValues.first()!!)
        val pPlus = numMatching.toDouble() / this.size
        val pMinus = 1.0 - pPlus
        return -1 * xLog2x(pPlus) - xLog2x(pMinus)
    }

    fun informationGain(attribute: Attribute<*>) : Double {
        val uniqueTargetValues = getUniqueValues(targetAttribute)
        val expectedValue = uniqueTargetValues.first()!!
        val targetEntropy = targetEntropy()
        val uniqueValues = getUniqueValues(attribute)
        var sum : Double = 0.0
        uniqueValues.forEach { value ->
            val entriesOfValue = this.filter { entry -> entry[attribute] == value }
            val scale = entriesOfValue.size.toDouble() / this.size
            val passingEntries = entriesOfValue.filter { entry -> entry[targetAttribute] == expectedValue }
            val pPlus = passingEntries.size.toDouble() / entriesOfValue.size
            val pMinus = 1.0 - pPlus
            val entropy = -xLog2x(pPlus) - xLog2x(pMinus)
            sum += scale * entropy
        }
        return targetEntropy - sum
    }

    fun getAllInformationGain() : Map<Attribute<*>, Double> {
        val ret = mutableMapOf<Attribute<*>, Double>()
        schema.attributes.filter { it != targetAttribute }.map { ret[it] = informationGain(it) }
        return ret
    }

    fun filter(attribute: Attribute<*>, value: Any) : List<Entry> = this.filter { entry -> entry[attribute] == value }

    fun getHighestGainAttribute() : Map.Entry<Attribute<*>, Double>? = getAllInformationGain().entries.maxBy { entry -> entry.value }

    fun count(attribute: Attribute<*>, value : Any) : Int = this.count { entry -> entry[attribute] == value }

    fun getUniqueValues(attribute: Attribute<*>) : List<*> = this.map { entry -> entry[attribute] }.toSet().toList()

    override fun add(element: Entry): Boolean {
        if(element.schema != this.schema){
            println("Improper use of schema")
            return false
        }
        return super.add(element)
    }

    override fun add(index: Int, element: Entry) {
        if(element.schema != this.schema){
            return
        }
        super.add(index, element)
    }

    override fun addAll(elements: Collection<Entry>): Boolean = elements.all { this.add(it) }

    override fun addAll(index: Int, elements: Collection<Entry>): Boolean {
        val toAdd = elements.filter { it.schema == this.schema }
        var ret = super.addAll(index, toAdd)
        if(toAdd.size != elements.size){
            return false
        }
        return ret
    }

    override fun toString(): String = schema.toString() + "\n" + super.toString()

    companion object {
        fun xLog2x(x: Double) : Double {
            if(x == 0.0){
                return 0.0;
            }
            return x * log(x, 2.0)
        }
    }
}