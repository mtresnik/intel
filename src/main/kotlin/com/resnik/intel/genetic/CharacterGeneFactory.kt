package com.resnik.intel.genetic

class CharacterGeneFactory(min: Char = ' ', max: Char = '~') :
    GeneFactory<Char>(Array(max.code - min.code) { (it + min.code).toChar() })
