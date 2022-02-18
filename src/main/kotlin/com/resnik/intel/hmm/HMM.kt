package com.resnik.intel.hmm

import com.resnik.math.linear.array.ArrayMatrix
import com.resnik.math.linear.array.ArrayVector
import java.lang.IllegalStateException
import kotlin.math.exp
import kotlin.math.ln

/**
 * T = observation sequence.length
 * N = states.length
 * transitions.dim = (NxN)
 * emissions.dim = (NxM)
 * initialProbability.length = N
 * initialProbability : probability first element will be of state i, scales markov chains
 * ex: initialProbability : [0.0, 1.0] for states : [sun, rain]
 * means the starting element will always be rain in an observation sequence, and scales all beginning sun states to 0.0
 *
 * For a given sequence S of length T, there are T - 1 transitions and T emissions, where
 * transition is defined as state -> state
 * emission is defined as state -> observation
 * Example: {H, T, H, H, T, H}.length = 6 would give transitions {HT, TH, HH, HT, TH}.length = 5
 *
 * Emission matrix is a parameter for an HMM because it classifies all observations,
 * a given sequence of observations: ABAAB doesn't account for all observations A,B,C
 *
 * The emission matrix is of length 3 but the sequence is of length 5, which is fine because there will be 5
 * lookups / timesteps in the matrices, this doesn't affect the size of the base matrices.
 *
 * Add log-sum trick to algorithms for arithmetic under / overflow,
 * @see logsumexp
 *
 * @param initialProbability Normalized vector of initial state distribution of size N
 * @param transitions Row normalized transition matrix of size (NxN)
 * @param emissions Row normalized emission matrix of size (NxM)
 * @param observations All possible observations of size M
 * */
class HMM<STATE, OBSERVATION>(
    private val initialProbability : Map<STATE, Double>,
    private val transitions : ArrayMatrix,
    private val emissions: ArrayMatrix,
    observations: Collection<OBSERVATION>
) {

    // Overhead log calcs to reduce complexity
    private val logPi : ArrayVector = ArrayVector(*initialProbability.values.map { ln(it) }.toDoubleArray())
    private val logTransitions : ArrayMatrix = transitions.apply { ln(it) }
    private val logEmissions : ArrayMatrix = emissions.apply { ln(it) }

    /** Length N */
    private val states = initialProbability.keys.toSet().toList()
    /** Length N */
    private val pi = initialProbability.values.toList()
    /** Length M */
    private val observations = observations.toSet().toList()

    constructor(other : HMM<STATE, OBSERVATION>) : this(other.getParameters())

    constructor(hmmParameters: HMMParameters<STATE, OBSERVATION>) : this(hmmParameters.initialProbability, hmmParameters.transitions, hmmParameters.emissions)

    constructor(initialProbability : Map<STATE, Double>, transitions : Array<DoubleArray>, emissions : Array<DoubleArray>, observations: Collection<OBSERVATION>) :
            this(initialProbability, ArrayMatrix(transitions), ArrayMatrix(emissions), observations)

    constructor(initialProbability: Map<STATE, Double>, transitionProbability : Map<STATE, Map<STATE, Double>>, emissions : Map<STATE, Map<OBSERVATION, Double>>)
    : this(initialProbability,
        convertToTransitionArrayMatrix(initialProbability.keys.toList(), transitionProbability),
        convertToEmissionArrayMatrix(initialProbability.keys, emissions.entries.first().value.keys.toList(), emissions),
        emissions.entries.first().value.keys.toList()
    )

    fun getParameters() : HMMParameters<STATE, OBSERVATION> {
        return HMMParameters(initialProbability, convertToTransitionMap(states, transitions), convertToEmissionMap(states, observations, emissions))
    }

    /**
     * Generates an alpha matrix of size (N x T)
     *
     * Complexity: O(T * N^2)
     *
     * Uses log-likelihood where L(a|b) = ln(p(a|b)) | p(a|b) = { 0 -> -inf, else p(a|b) }
     *
     * @see states
     * @param observationSequence Sequence of observations with length T
     * */
    fun forward(observationSequence: List<OBSERVATION>) : ProbabilityValue<ArrayMatrix> {
        // Also known as a "forward" matrix, used for "recursion"
        // "recursion" as mathematical not stack based
        val alphaMatrix = ArrayMatrix(states.size, observationSequence.size)
        // Initialize starting values
        states.indices.forEach { i ->
            // Only possible NaN is when pi[i] is inf and emission[i][0] is 0.0
            alphaMatrix[i][0] = logPi[i] + logEmissions[i][0]
        }
        (1 until observationSequence.size).forEach { T ->
            // Get actual index of observation as it relates to the emission matrix
            val obsIndex = observations.indexOf(observationSequence[T])
            (states.indices).forEach { state ->
                // Need to use log-sum-exp trick here, converting p(a|b) -> L(a|b)
                // And converting a * b * c -> ln(a) + ln(b) + ln(c)
                alphaMatrix[state][T] = DoubleArray(states.size) {
                    alphaMatrix[it][T -1] + logTransitions[it][state] + emissions[state][obsIndex]
                }.logsumexp()
            }
        }
        val t = observationSequence.lastIndex
        val logProbability = DoubleArray(states.size) {i -> alphaMatrix[i][t]}.logsumexp()
        return ProbabilityValue(alphaMatrix, logProbability)
    }

    /**
     * Generates a matrix of size (N x T)
     *
     * */
    fun backward(observationSequence: List<OBSERVATION>) : ProbabilityValue<ArrayMatrix> {
        // Generates a beta matrix
        val betaMatrix = ArrayMatrix(states.size, observationSequence.size)
        // Boundary condition
        states.indices.forEach { betaMatrix[it][observationSequence.lastIndex] = 1.0 }
        ((observationSequence.lastIndex - 1) downTo 1).forEach { T ->
            val nextIndex = observations.indexOf(observationSequence[T + 1])
            states.indices.forEach { k ->
                betaMatrix[k][T] = DoubleArray(states.size) {
                    betaMatrix[it][T + 1] + logTransitions[k][it] + logEmissions[it][nextIndex]
                }.logsumexp()
            }
        }
        val probability = DoubleArray(states.size) { i -> betaMatrix[i][0] }.logsumexp()
        return ProbabilityValue(betaMatrix, probability)
    }

    /**
     * Decoding problem, optimal sequence of hidden states for a given observation sequence
     *
     * @param observationSequence A sequence of observations, not to be confused with all possible observations.
     * */
    fun viterbi(observationSequence: List<OBSERVATION>) : Path<STATE> {
        // Observation sequence size != num observations in total
        val viterbiMatrix = ArrayMatrix(states.size, observationSequence.size)
        // Stores indices of dim states.size x observations.size
        val backpointerMatrix = Array(states.size){ IntArray(observationSequence.size) }
        logPi.values.forEachIndexed { i, logProbability ->
            viterbiMatrix[i][0] = logProbability + logEmissions[i][0]
            backpointerMatrix[i][0] = 0
        }
        (1 until observationSequence.size).forEach { j ->
            // Get actual index of observation as it relates to the emission matrix
            val obsIndex = observations.indexOf(observationSequence[j])
            (states.indices).forEach { i ->
                // Viterbi matrix max for timestep
                viterbiMatrix[i][j] = states.indices
                    .map { k -> viterbiMatrix[k][j-1] + logTransitions[k][i] + logEmissions[i][obsIndex] }
                    .maxOrNull() ?: throw IllegalStateException()
                // Argmax stored in backpointer matrix
                backpointerMatrix[i][j] = states.indices
                    .maxByOrNull { k -> viterbiMatrix[k][j-1] + logTransitions[k][i] + logEmissions[i][obsIndex] }
                    ?: throw IllegalStateException()
            }
        }
        // pointers and path are populated by best state for observation index
        val bestPathPointers = IntArray(observationSequence.size)
        val bestPath = MutableList<STATE?>(observationSequence.size){null}
        bestPathPointers[bestPathPointers.lastIndex] = states.indices.maxByOrNull { k -> viterbiMatrix[k].last() } ?: throw IllegalStateException()
        bestPath[bestPath.lastIndex] = states[bestPathPointers.last()]
        (observationSequence.lastIndex downTo   1).forEach { j ->
            bestPathPointers[j - 1] = backpointerMatrix[bestPathPointers[j]][j]
            bestPath[j - 1] = states[bestPathPointers[j-1]]
        }
        return Path(bestPath.map { it ?: throw IllegalStateException() }, viterbiMatrix[bestPathPointers[bestPathPointers.lastIndex]].last())
    }

    open class ProbabilityValue<VALUE>(val value : VALUE, val probability: Double) {
        override fun toString(): String = "Value: $value \t Probability: $probability"
    }

    data class Path<STATE>(private val path : List<STATE>, private val p : Double) : ProbabilityValue<List<STATE>>(path, p)

    data class HMMParameters<STATE, OBSERVATION>(
        val initialProbability : Map<STATE, Double>,
        val transitions : Map<STATE, Map<STATE, Double>>,
        val emissions : Map<STATE, Map<OBSERVATION, Double>>,
        var probability: Double = 1.0) {

        fun states() : Set<STATE> = initialProbability.keys

        fun observations() : Set<OBSERVATION> = emissions.entries.first().value.keys

    }

    companion object {

        // Helper function for map conversions
        private fun <STATE> convertToTransitionArrayMatrix(states : Collection<STATE>, transitionProbability : Map<STATE, Map<STATE, Double>>) : ArrayMatrix {
            val ret = ArrayMatrix(states.size, states.size)
            states.forEachIndexed { row, rowState ->
                val internalMap = transitionProbability[rowState] ?: throw IllegalStateException()
                internalMap.forEach { entry ->
                    val colState = entry.key
                    val col = states.indexOf(colState)
                    val probability = entry.value
                    ret[row][col] = probability
                }
            }
            return ret
        }

        private fun <STATE> convertToTransitionMap(states : Collection<STATE>, transitionMatrix: ArrayMatrix) : Map<STATE, Map<STATE, Double>> {
            val transitionMap = mutableMapOf<STATE, Map<STATE, Double>>()
            states.forEachIndexed { row, state ->
                val tempMap = mutableMapOf<STATE, Double>()
                states.forEachIndexed { col, child ->
                    tempMap[child] = transitionMatrix[row][col]
                }
                transitionMap[state] = tempMap
            }
            return transitionMap
        }

        // Helper function for map conversions
        private fun <STATE, OBSERVATION>
                convertToEmissionArrayMatrix(states : Collection<STATE>,
                                             observations: Collection<OBSERVATION>,
                                             emissions : Map<STATE, Map<OBSERVATION, Double>>) : ArrayMatrix {
            val ret = ArrayMatrix(states.size, observations.size)
            // Make sure state / obs indices line up
            states.forEachIndexed { row, rowState ->
                val internalMap = emissions[rowState]!!
                internalMap.forEach { entry ->
                    val colObs = entry.key
                    val col = observations.indexOf(colObs)

                    val probability = entry.value
                    ret[row][col] = probability
                }
            }
            return ret
        }

        private fun <STATE, OBSERVATION> convertToEmissionMap(states: Collection<STATE>, observations : Collection<OBSERVATION>, emissionMatrix: ArrayMatrix) :
                Map<STATE, Map<OBSERVATION, Double>> {
            val emissionMap = mutableMapOf<STATE, Map<OBSERVATION, Double>>()
            states.forEachIndexed { row, state ->
                val tempMap = mutableMapOf<OBSERVATION, Double>()
                observations.forEachIndexed { col, observation ->
                    tempMap[observation] = emissionMatrix[row][col]
                }
                emissionMap[state] = tempMap
            }
            return emissionMap
        }

        /**
         * Generates initial probability distribution, transition, and emission matrices based on training data.
         *
         * Note: this data may be incomplete, meaning there may be more hidden states / observations that are never used / seen.
         * Add extraStates and extraObservations as parameters if you expect to use this model on futured expected states / obs.
         * */
        @Suppress("UNCHECKED_CAST")
        fun<STATE, OBSERVATION> estimateInitialParameters(markedObservations : List<List<Pair<STATE, OBSERVATION>>>,
                                                          extraStates : List<STATE> = mutableListOf(),
                                                          extraObservations : List<OBSERVATION> = mutableListOf())
                : HMMParameters<STATE, OBSERVATION> {
            // Approximate parameters using marked observations first for transition / emission / initial
            // Find all defined states / observations from marked pairs
            var states = mutableSetOf<STATE>()
            var observations = mutableSetOf<OBSERVATION>()
            states.addAll(markedObservations.flatten().map { it.first })
            states.addAll(extraStates)
            observations.addAll(markedObservations.flatten().map { it.second })
            observations.addAll(extraObservations)
            // Sort elements for readability of results (if possible)
            if(states.all { it is Comparable<*> }) {
                states = states
                    .map { it as Comparable<STATE> }
                    .sortedWith(Comparator{ s1, s2 -> s1.compareTo(s2 as STATE) })
                    .map { it as STATE }
                    .toMutableSet()
            }
            // Sort observations if possible as well for readability
            if(observations.all { it is Comparable<*> }) {
                observations = observations
                    .map { it as Comparable<OBSERVATION> }
                    .sortedWith(Comparator{ o1, o2 -> o1.compareTo(o2 as OBSERVATION)})
                    .map { it as OBSERVATION }
                    .toMutableSet()
            }

            // Find initial probability
            val firstStates = markedObservations.map { it.first() }
            val tempProbability = mutableMapOf<STATE, Double>()
            // Need to put these in order of the states defined above so it keeps array / matrix order
            tempProbability.putAll(states.map { Pair(it, firstStates.count { pair -> pair.first == it }.toDouble() / markedObservations.size)})
            val initialProbability = mutableMapOf<STATE, Double>()
            states.forEach { initialProbability[it] = tempProbability[it]!! }

            /* Count the previous state given the current state into a 2d IntArray of size NxN
               Default value is 0 for int arrays
               Ordered based on state list defined above */
            val stateCountArray = Array(states.size){ IntArray(states.size) { 0 } }
            markedObservations.forEach { sequence ->
                // Starting at 1 because first element doesn't have a prior element
                (1 until sequence.size).forEach { index ->
                    // Last index vs current index
                    val lastState = sequence[index - 1].first
                    val row = states.indexOf(lastState)
                    val currState = sequence[index].first
                    val col = states.indexOf(currState)
                    // Increment transition count
                    stateCountArray[row][col]++
                }
            }
            // Normalize matrix for row adding to 1.0 property
            val transitionMatrix = ArrayMatrix(stateCountArray.map { row ->
                row.map { col -> col.toDouble() / row.sum() }.toDoubleArray()
            }.toTypedArray())

            // In a similar way make the emission count matrix and normalize
            val emissionCountArray = Array(states.size) { IntArray(observations.size) }
            markedObservations.forEach { sequence ->
                sequence.forEach { (state, observation) ->
                    val row = states.indexOf(state)
                    val col = observations.indexOf(observation)
                    // Increment count where state matches to an observation (emission)
                    emissionCountArray[row][col]++
                }
            }
            // Normalize emission counts by row
            val emissionMatrix = ArrayMatrix(emissionCountArray.map { row ->
                row.map { col -> col.toDouble() / row.sum() }.toDoubleArray()
            }.toTypedArray())
            // Convert matrices into their mapped forms
            val transitionMap = convertToTransitionMap<STATE>(states, transitionMatrix)
            val emissionMap = convertToEmissionMap<STATE, OBSERVATION>(states, observations, emissionMatrix)
            // Initial parameters
            return HMMParameters(initialProbability, transitionMap, emissionMap)
        }

        /**
         * Generates the HMM parameters of a local minima which maximizes the current model state.
         * Retrains internal models using expectation-minimization, model <-> parameters until some epsilon is reached
         * OR when num iterations is reached.
         *
         * */
        fun <STATE, OBSERVATION> baumWelch(markedTrainingData : List<List<Pair<STATE, OBSERVATION>>>,
                                           extraStates : List<STATE> = mutableListOf(),
                                           extraObservations : List<OBSERVATION> = mutableListOf(),
                                           initialHMMParameters: HMMParameters<STATE, OBSERVATION> =
                                               estimateInitialParameters(markedTrainingData, extraStates, extraObservations),
                                           numIterations : Int = 100,
                                           epsilon : Double? = null)
        : HMMParameters<STATE, OBSERVATION> {
            // Setup needed initial parameters if not defined
            val initialHMM = HMM(initialHMMParameters)
            val states = initialHMMParameters.states()
            val observations = initialHMMParameters.observations()

            var trainingSequences = markedTrainingData.map { it.map { pair -> pair.second } }
            var currHMM = initialHMM
            var currIteration = 0
            var currProbability = 0.0
            while(currIteration < numIterations) {
                // Iterate over each training sequence
                // Use forward probability from sequence for epsilon
                if(epsilon != null && currProbability < epsilon){
                    break
                }
                val gammas = mutableListOf<ArrayMatrix>()
                val xis = mutableListOf<Array<Array<DoubleArray>>>()
                trainingSequences.forEach { sequence ->
                    // Reuse forward calc for each sequence to find average probability of the currHMM
                    val forward = currHMM.forward(sequence)
                    // alpha and beta have log-likelihood values
                    val alpha = forward.value
                    val forwardProbability = forward.probability
                    val backward = currHMM.backward(sequence)
                    val beta = backward.value
                    // i in 0 to N - 1
                    // t in 0 to T - 1
                    // gamma[i][t] = alpha[i][t] + beta[i][t] - sum(j in 0 to N - 1 of alpha[j][t])
                    // Normalized by row (for all j in time-step t)
                    val gamma = (alpha + beta).apply { it - forwardProbability }
                    // NxNxT
                    val xi = Array(states.size){Array(states.size){ DoubleArray(sequence.size) }}
                    (0 until sequence.lastIndex).forEach { t ->
                        // Max index of t is T.lastIndex - 1 so beta won't go out of bounds here
                        val nextObservation = sequence[t + 1]
                        val nextEmissionIndex = currHMM.observations.indexOf(nextObservation)

                        states.indices.forEach { i ->
                            states.indices.forEach { j ->
                                xi[i][j][t] = exp(alpha[i][t] + currHMM.logTransitions[i][j] + beta[j][t + 1] + currHMM.logEmissions[j][nextEmissionIndex] - forwardProbability)
                                if(xi[i][j][t].isNaN()) xi[i][j][t] = 0.0
                            }
                        }
                    }

                    gammas.add(gamma)
                    xis.add(xi)
                }
                // Find new values of pi, transitions, emissions
                // Ensure pi is normalized as well
                val newInitialProbabilities = mutableMapOf<STATE, Double>()
                states.forEachIndexed { i, state ->
                    newInitialProbabilities[state] = gammas.map { gamma -> exp(gamma[i][0]) }.average()
                }
                val statesSum = newInitialProbabilities.values.sum()
                states.forEachIndexed { _, state ->
                    newInitialProbabilities[state] = newInitialProbabilities[state]!! / statesSum
                }
                val newTransitions = ArrayMatrix(states.size, states.size)
                states.indices.forEach { i ->
                    var rowSum = 0.0
                    states.indices.forEach { j ->
                        var xiSum = 0.0
                        var gammaSum = 0.0
                        trainingSequences.indices.forEach { r ->
                            // Since T depends on sequence size
                            val lastIndex = trainingSequences[r].lastIndex
                            // For the sum it doesn't really matter if it's T or T - 1 because
                            // arrays fill with 0's anyway... but still for completeness
                            xiSum += (0 until lastIndex).sumByDouble { t ->
                                exp(xis[r][i][j][t])
                            }
                            gammaSum += (0 until lastIndex).sumByDouble { t ->
                                exp(gammas[r][i][t])
                            }
                        }
                        newTransitions[i][j] = xiSum / gammaSum
                        rowSum += newTransitions[i][j]
                    }
                    states.indices.forEach { j ->
                        newTransitions[i][j] /= rowSum
                    }
                }

                fun indicator(obs1 : OBSERVATION, obs2 : OBSERVATION) : Int = if(obs1 == obs2) 1 else 0

                val newEmissions = ArrayMatrix(states.size, observations.size)
                states.indices.forEach { i ->
                    var rowSum = 0.0
                    observations.forEachIndexed { k, vk ->
                        var numeratorSum = 0.0
                        var denominatorSum = 0.0
                        trainingSequences.indices.forEach { r ->
                            val T = trainingSequences[r].size
                            (0 until T).forEach { t ->
                                numeratorSum += indicator(vk, trainingSequences[r][t]) * exp(gammas[r][i][t])
                                denominatorSum += exp(gammas[r][i][t])
                            }
                        }
                        newEmissions[i][k] = numeratorSum / denominatorSum
                        rowSum += newEmissions[i][k]
                    }
                    observations.forEachIndexed { k, vk ->
                        newEmissions[i][k] /= rowSum
                    }
                }
                // Replace currHMM with newHMM
                currHMM = HMM(newInitialProbabilities, newTransitions, newEmissions, observations)
                // Find next probability by using forwards on all sequences
                val sequenceProbabilities = trainingSequences.map { sequence -> exp(currHMM.forward(sequence).probability) }
                val sequenceProbabilitySum = sequenceProbabilities.sum()
                currProbability = sequenceProbabilities.map { probability -> probability / sequenceProbabilitySum }.average()
                println(with(currHMM.getParameters()) {
                    this.probability = currProbability
                    this
                })
                currIteration++
            }
            return with(currHMM.getParameters()) {
                this.probability = currProbability
                this
            }
        }

    }

}