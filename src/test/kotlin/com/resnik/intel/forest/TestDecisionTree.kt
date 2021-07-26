package com.resnik.intel.forest

import org.junit.Test
import java.lang.Boolean

class TestDecisionTree {

    fun tennisDataset() : Dataset {
        val outlook         = Attribute("Outlook",      String::class.java)
        val sunny           = "Sunny"
        val overcast        = "Overcast"
        val rain            = "Rain"
        val temperature     = Attribute("Temperature",  String::class.java)
        val hot             = "Hot"
        val mild            = "Mild"
        val cool            = "Cool"
        val humidity        = Attribute("Humidity",     String::class.java)
        val high            = "High"
        val normal          = "Normal"
        val wind            = Attribute("Wind",         String::class.java)
        val weak            = "Weak"
        val strong          = "Strong"
        val tennis          = Attribute("Tennis",       String::class.java)
        val no              = "No"
        val yes             = "Yes"
        val schema = Schema(outlook, temperature, humidity, wind, tennis)
        val dataset = Dataset(schema, tennis)
        dataset.add(Entry(schema, sunny,      hot,    high,   weak,       no))
        dataset.add(Entry(schema, sunny,      hot,    high,   strong,     no))
        dataset.add(Entry(schema, overcast,   hot,    high,   weak,       yes))
        dataset.add(Entry(schema, rain,       mild,   high,   weak,       yes))
        dataset.add(Entry(schema, rain,       cool,   normal, weak,       yes))
        dataset.add(Entry(schema, rain,       cool,   normal, strong,     no))
        dataset.add(Entry(schema, overcast,   cool,   normal, strong,     yes))
        dataset.add(Entry(schema, sunny,      mild,   high,   weak,       no))
        dataset.add(Entry(schema, sunny,      cool,   normal, weak,       yes))
        dataset.add(Entry(schema, rain,       mild,   normal, weak,       yes))
        dataset.add(Entry(schema, sunny,      mild,   normal, strong,     yes))
        dataset.add(Entry(schema, overcast,   mild,   high,   strong,     yes))
        dataset.add(Entry(schema, overcast,   hot,    normal, weak,       yes))
        dataset.add(Entry(schema, rain,       mild,   high,   strong,     no))
        return dataset
    }

    @Test
    fun testTennisDataset(){
        val dataset = tennisDataset()
        val decisionTree = dataset.buildTree()
        val result = decisionTree.traverse("Sunny", "Mild", "High", "Strong")
        println(result)
    }

    @Test
    fun testDecisionTree(){
        val input = Attribute("name", String::class.java)
        val middle = Attribute("age", String::class.java)
        val output = Attribute("isCool", Boolean::class.java)
        val schema = Schema(input, middle, output)
        val dataset = Dataset(schema, output)
        val entry1 = Entry(schema)
        entry1[input] = "Boy"
        entry1[middle] = "Young"
        entry1[output] = Boolean.TRUE
        val entry2 = Entry(schema)
        entry2[input] = "Boy"
        entry2[middle] = "Old"
        entry2[output] = Boolean.FALSE
        val entry3 = Entry(schema)
        entry3[input] = "Girl"
        entry3[middle] = "Young"
        entry3[output] = Boolean.TRUE
        val entry4 = Entry(schema)
        entry4[input] = "Girl"
        entry4[middle] = "Old"
        entry4[output] = Boolean.FALSE
        dataset.add(entry1)
        dataset.add(entry2)
        dataset.add(entry3)
        println(dataset)
        println(dataset.getAllInformationGain())
        val tree = dataset.buildTree()
        println(tree.traverse("Girl", "Old"))
    }

}