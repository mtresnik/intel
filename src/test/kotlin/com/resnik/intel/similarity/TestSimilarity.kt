package com.resnik.intel.similarity

import org.junit.Test

class TestSimilarity {

    @Test
    fun testLCS(){
        val str1 = "Hello my name is Mike"
        val str2 = "Mike my name is"
        assert(str2.lcs(str1) == "e my name is")
    }

    @Test
    fun testJaro(){
        println("TRATE".jaro("TRACE"))
    }

    @Test
    fun testJaroWinkler(){
        println("TRATE".jaroWinkler("TRACE"))
    }

}