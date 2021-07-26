package com.resnik.intel.genetic

class NormalizedDoubleChromosomeFactory(vararg ranges: NormalizedDoubleGeneFactory) : RangeChromosomeFactory(*ranges) {
}