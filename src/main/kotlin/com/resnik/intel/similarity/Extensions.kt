package com.resnik.intel.similarity

import java.util.*

fun String.lcs(other: String): String {
    val numArray = Array(this.length + 1) { IntArray(other.length + 1) { 0 } }
    for (i in 1 until this.length + 1) {
        val c1 = this[i - 1]
        for (j in 1 until other.length + 1) {
            val c2 = other[j - 1]
            if (c1 == c2) {
                numArray[i][j] = numArray[i - 1][j - 1] + 1;
            } else {
                numArray[i][j] = numArray[i - 1][j].coerceAtLeast(numArray[i][j - 1])
            }
        }
    }
    val retStack = Stack<Char>()
    var i = numArray.size - 1
    var j = numArray[0].size - 1
    var currNum = numArray[i][j]
    while (currNum != 0) {
        when {
            numArray[i - 1][j] == currNum -> {
                i--
            }
            numArray[i][j - 1] == currNum -> {
                j--
            }
            numArray[i - 1][j - 1] != currNum -> {
                i--
                j--
                retStack.push(this[i])
            }
        }
        currNum = numArray[i][j]
    }
    var retString: String = ""
    while (!retStack.isEmpty()) {
        retString += retStack.pop()
    }
    return retString
}

fun String.lcsSimilarity(other: String): Double =
    this.lcs(other).length.toDouble() / this.length.coerceAtLeast(other.length)

fun String.jaro(other: String): Double {
    if (this.isEmpty() && other.isEmpty()) return 1.0
    val maxMatchDistance = this.length.coerceAtLeast(other.length) / 2 - 1
    val thisMatchArray = BooleanArray(this.length)
    val otherMatchArray = BooleanArray(other.length)
    var matches = 0
    for (i in this.indices) {
        val start = 0.coerceAtLeast(i - maxMatchDistance)
        val end = (i + maxMatchDistance + 1).coerceAtMost(other.length)
        (start until end).find { j -> !otherMatchArray[j] && this[i] == other[j] }?.let {
            thisMatchArray[i] = true
            otherMatchArray[it] = true
            matches++
        }
    }
    if (matches == 0) return 0.0
    var t = 0.0
    var k = 0
    (this.indices).filter { thisMatchArray[it] }.forEach { i ->
        while (!otherMatchArray[k]) k++
        if (this[i] != other[k]) t += 0.5
        k++
    }

    val m = matches.toDouble()
    return (m / this.length + m / other.length + (m - t) / m) / 3.0
}

fun String.jaroWinkler(other: String, scalingFactor: Double = 0.1): Double {
    val sJ = this.jaro(other)
    if (sJ <= 0.7) return sJ
    val prefix = (0 until this.length.coerceAtMost(other.length))
        .count { this[it] == other[it] }
        .coerceAtMost(4)
        .toDouble()
    println(prefix)
    return sJ + scalingFactor * prefix * (1.0 - sJ)
}