package com.resnik.intel.nlp

object WordTokenizer {

    fun tokenize(sentence : String) : List<String> {
        val tempList = mutableListOf<String>()
        val retList = mutableListOf<String>()
        // Pull words out from sentence based on spaces / punctuation
        tempList.addAll(sentence.split(" ").filter { word -> word.isNotEmpty() && word.isNotBlank() })
        tempList.forEach { word ->
            var accumulate = ""
            word.forEach { letter ->
                if(letter in ALL_PUNCTUATION){
                    if(accumulate.isNotEmpty() && accumulate.isNotBlank()){
                        retList.add(accumulate)
                    }
                    accumulate = ""
                    retList.add(letter.toString())
                } else {
                    accumulate += letter
                }
            }
            if(accumulate.isNotEmpty() && accumulate.isNotBlank()){
                retList.add(accumulate)
            }
        }
        return retList
    }

}