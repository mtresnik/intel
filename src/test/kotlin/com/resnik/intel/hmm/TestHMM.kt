package com.resnik.intel.hmm

import com.resnik.math.linear.array.ArrayMatrix
import org.junit.Test

class TestHMM {

    @Test
    fun testHMM1() {
        val healthy = "Healthy"
        val fever = "Fever"
        val normal = "normal"
        val cold = "cold"
        val dizzy = "dizzy"

        // If you want to map everything yourself (also works with 2d Double Arrays)
        val initialProbability = mutableMapOf<String, Double>(Pair(healthy, 0.6), Pair(fever, 0.4))
        val transitionProbability = ArrayMatrix(2, 2)
        transitionProbability[0][0] = 0.7
        transitionProbability[0][1] = 0.3
        transitionProbability[1][0] = 0.4
        transitionProbability[1][1] = 0.6
        val emissionProbability = ArrayMatrix(2, 3)
        emissionProbability[0][0] = 0.5
        emissionProbability[0][1] = 0.4
        emissionProbability[0][2] = 0.1
        emissionProbability[1][0] = 0.1
        emissionProbability[1][1] = 0.3
        emissionProbability[1][2] = 0.6
        val observations = listOf<String>(normal, cold, dizzy)
        val hmm = HMM<String, String>(initialProbability, transitionProbability, emissionProbability, observations)
        val likelihood = hmm.viterbi(observations)
        println(likelihood)
    }

    @Test
    fun testHMM2(){
        // States
        val healthy = "Healthy"
        val fever = "Fever"

        // Observations
        val normal = "normal"
        val cold = "cold"
        val dizzy = "dizzy"

        val initialProbability = mutableMapOf<String, Double>(Pair(healthy, 0.6), Pair(fever, 0.4))
        val transitionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(healthy, mutableMapOf(Pair(healthy, 0.7), Pair(fever, 0.3))),
            Pair(fever, mutableMapOf(Pair(healthy, 0.4), Pair(fever, 0.6)))
        )
        val emissionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(healthy, mutableMapOf(Pair(normal, 0.5), Pair(cold, 0.4), Pair(dizzy, 0.1))),
            Pair(fever, mutableMapOf(Pair(normal, 0.1), Pair(cold, 0.3), Pair(dizzy, 0.6)))
        )
        val observations = listOf<String>(normal, cold, dizzy)
        val hmm = HMM<String, String>(initialProbability, transitionProbabilities, emissionProbabilities)
        val likelihood = hmm.viterbi(observations)
        println(likelihood)
    }

    @Test
    fun testForward() {
        val rain = "rain"
        val sunny = "sunny"

        val sad = "sad"
        val happy = "happy"

        val initialProbability = mutableMapOf<String, Double>(Pair(rain, 0.375), Pair(sunny, 0.625))

        val transitionProbability = ArrayMatrix(2, 2)
        transitionProbability[0][0] = 0.5
        transitionProbability[0][1] = 0.5
        transitionProbability[1][0] = 0.3
        transitionProbability[1][1] = 0.7
        val emissionProbability = ArrayMatrix(2, 2)
        emissionProbability[0][0] = 0.8
        emissionProbability[0][1] = 0.2
        emissionProbability[1][0] = 0.4
        emissionProbability[1][1] = 0.6
        val allObservations = mutableListOf(sad, happy)
        val hmm = HMM<String, String>(initialProbability, transitionProbability, emissionProbability, allObservations)
        val observationSequence = listOf<String>(sad, sad, happy)
        val forward = hmm.forward(observationSequence)
        println(forward)
    }

    @Test
    fun testForward2(){
        // GC content example
        val H = "H"
        val L = "L"

        // Observations
        val A = "A"
        val C = "C"
        val G = "G"
        val T = "T"

        val initialProbability = mutableMapOf<String, Double>(Pair(H, 0.5), Pair(L, 0.5))
        val transitionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(H, mutableMapOf(Pair(H, 0.5), Pair(L, 0.5))),
            Pair(L, mutableMapOf(Pair(H, 0.4), Pair(L, 0.6)))
        )
        val emissionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(H, mutableMapOf(Pair(A, 0.2), Pair(C, 0.3), Pair(G, 0.3), Pair(T, 0.2))),
            Pair(L, mutableMapOf(Pair(A, 0.3), Pair(C, 0.2), Pair(G, 0.2), Pair(T, 0.3)))
        )
        val observationSequence = listOf<String>(G, G, C, A)

        val hmm = HMM<String, String>(initialProbability, transitionProbabilities, emissionProbabilities)
        val forward = hmm.forward(observationSequence)
        println(forward)
    }

    @Test
    fun testBaumWelch(){
        val A = "A"
        val B = "B"

        val observations = mutableListOf<List<Pair<String, Int>>>(
            mutableListOf(Pair(B, 3), Pair(B, 2), Pair(A, 2)),
            mutableListOf(Pair(A, 1), Pair(A, 1), Pair(B, 3)),
            mutableListOf(Pair(B, 1), Pair(A, 2), Pair(B, 3)),
            mutableListOf(Pair(B, 2), Pair(A, 1), Pair(A, 1))
        )
        val params = HMM.baumWelch(observations)
        println(params)
    }

    @Test
    fun testBaumWelch2(){
        // Egg example from wikipedia
        // States
        val STATE1 = "STATE1"
        val STATE2 = "STATE2"
        // Observations
        val N = "N"
        val E = "E"

        val initialProbability = mutableMapOf<String, Double>(Pair(STATE1, 0.2), Pair(STATE2, 0.8))
        val transitionMatrix = ArrayMatrix(arrayOf(doubleArrayOf(0.5, 0.5), doubleArrayOf(0.3, 0.7)))
        val emissionMatrix = ArrayMatrix(arrayOf(doubleArrayOf(0.3, 0.7), doubleArrayOf(0.8, 0.2)))
    }

}