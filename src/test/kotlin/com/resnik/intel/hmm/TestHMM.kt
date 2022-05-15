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
        val initialProbability = mutableMapOf(Pair(healthy, 0.6), Pair(fever, 0.4))
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
        val observations = listOf(normal, cold, dizzy)
        val hmm = HMM(initialProbability, transitionProbability, emissionProbability, observations)
        val likelihood = hmm.viterbi(observations)
        println(likelihood)
    }

    @Test
    fun testHMM2() {
        // States
        val healthy = "Healthy"
        val fever = "Fever"

        // Observations
        val normal = "normal"
        val cold = "cold"
        val dizzy = "dizzy"

        val initialProbability = mutableMapOf(Pair(healthy, 0.6), Pair(fever, 0.4))
        val transitionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(healthy, mutableMapOf(Pair(healthy, 0.7), Pair(fever, 0.3))),
            Pair(fever, mutableMapOf(Pair(healthy, 0.4), Pair(fever, 0.6)))
        )
        val emissionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(healthy, mutableMapOf(Pair(normal, 0.5), Pair(cold, 0.4), Pair(dizzy, 0.1))),
            Pair(fever, mutableMapOf(Pair(normal, 0.1), Pair(cold, 0.3), Pair(dizzy, 0.6)))
        )
        val observations = listOf(normal, cold, dizzy)
        val hmm = HMM(initialProbability, transitionProbabilities, emissionProbabilities)
        val likelihood = hmm.viterbi(observations)
        println(likelihood)
    }

    @Test
    fun testForward() {
        val rain = "rain"
        val sunny = "sunny"

        val sad = "sad"
        val happy = "happy"

        val initialProbability = mutableMapOf(Pair(rain, 0.375), Pair(sunny, 0.625))

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
        val hmm = HMM(initialProbability, transitionProbability, emissionProbability, allObservations)
        val observationSequence = listOf(sad, sad, happy)
        val forward = hmm.forward(observationSequence)
        println(forward)
    }

    @Test
    fun testForward2() {
        // GC content example
        val stateH = "H"
        val stateL = "L"

        // Observations
        val observationA = "A"
        val observationC = "C"
        val observationG = "G"
        val observationT = "T"

        val initialProbability = mutableMapOf(Pair(stateH, 0.5), Pair(stateL, 0.5))
        val transitionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(stateH, mutableMapOf(Pair(stateH, 0.5), Pair(stateL, 0.5))),
            Pair(stateL, mutableMapOf(Pair(stateH, 0.4), Pair(stateL, 0.6)))
        )
        val emissionProbabilities = mutableMapOf<String, Map<String, Double>>(
            Pair(stateH, mutableMapOf(Pair(observationA, 0.2), Pair(observationC, 0.3), Pair(observationG, 0.3), Pair(observationT, 0.2))),
            Pair(stateL, mutableMapOf(Pair(observationA, 0.3), Pair(observationC, 0.2), Pair(observationG, 0.2), Pair(observationT, 0.3)))
        )
        val observationSequence = listOf(observationG, observationG, observationC, observationA)

        val hmm = HMM(initialProbability, transitionProbabilities, emissionProbabilities)
        val forward = hmm.forward(observationSequence)
        println(forward)
    }

    @Test
    fun testBaumWelch() {
        val observationA = "A"
        val observationB = "B"

        val observations = mutableListOf<List<Pair<String, Int>>>(
            mutableListOf(Pair(observationB, 3), Pair(observationB, 2), Pair(observationA, 2)),
            mutableListOf(Pair(observationA, 1), Pair(observationA, 1), Pair(observationB, 3)),
            mutableListOf(Pair(observationB, 1), Pair(observationA, 2), Pair(observationB, 3)),
            mutableListOf(Pair(observationB, 2), Pair(observationA, 1), Pair(observationA, 1))
        )
        val params = HMM.baumWelch(observations)
        println(params)
    }

}