package com.resnik.intel.nlp

import org.junit.Test

class TestTokenizer {

    @Test
    fun testTokenizer1(){
        val input = "Hello, my name is Mike. I like waffles and cheese! Do you want a waffle?"
        val words = WordTokenizer.tokenize(input)
        println(words)
    }

}