package com.resnik.intel.nlp

object SentenceSegmenter {

    fun segment(input: String): List<String> {
        // Separate string into sentences by recognizing ending punctuations
        val retList = mutableListOf<String>()
        var accumulate = ""
        input.forEach { c ->
            accumulate += c
            if (c in ENDING_PUNCTUATION) {
                retList.add(accumulate)
                accumulate = ""
            }
        }
        return retList
    }

}