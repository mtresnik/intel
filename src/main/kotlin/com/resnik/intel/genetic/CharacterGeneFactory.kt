package com.resnik.intel.genetic

class CharacterGeneFactory(min: Char = ' ', max: Char = '~') :
    GeneFactory<Char>(Array(max.toInt() - min.toInt()) { (it + min.toInt()).toChar() })
