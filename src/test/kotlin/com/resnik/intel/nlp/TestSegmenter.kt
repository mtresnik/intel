package com.resnik.intel.nlp

import org.junit.Test

class TestSegmenter {

    @Test
    fun testSegment1(){
        val input = "Hello, my name is Mike. I like waffles and cheese! Do you want a waffle?"
        val sentences = SentenceSegmenter.segment(input)
        println(sentences)
    }

}