package com.resnik.intel.cluster

import com.resnik.math.linear.array.ArrayPoint
import java.util.LinkedHashSet

class KMeans(val seedPoints: Collection<ArrayPoint>) : Comparable<KMeans> {

    val allClusters: MutableList<Cluster> = seedPoints.map { Cluster(it) }.toMutableList()

    fun cluster(points: Collection<ArrayPoint>) : List<Cluster> {
        val clusterIndices = mutableSetOf<Int>()
        points.forEach { point -> val index = getClusterIndex(point); allClusters[index].add(point); clusterIndices.add(index) }
        class MoveOp(val point: ArrayPoint, val fromIndex: Int, val toIndex: Int) {}
        val moveOperations = mutableListOf<MoveOp>()
        allClusters.indices.forEach { clusterIndex ->
            val cluster: Cluster = allClusters[clusterIndex]
            cluster.forEach { point ->
                val expectedIndex = getClusterIndex(point)
                if(clusterIndex != expectedIndex){
                    moveOperations.add(MoveOp(point, clusterIndex, expectedIndex))
                }
            }
        }
        moveOperations.forEach {
            allClusters[it.fromIndex].remove(it.point)
            allClusters[it.toIndex].add(it.point)
        }
        return clusterIndices.map { allClusters[it] }
    }

    fun getClusterIndex(point: ArrayPoint) : Int = allClusters.indices.minByOrNull { index -> allClusters[index].distanceTo(point) }!!

    fun getCluster(point: ArrayPoint) : Cluster = allClusters[getClusterIndex(point)]

    operator fun get(point: ArrayPoint) : Cluster = getCluster(point)

    fun getMeanVariance() : Double = allClusters.sumByDouble { it.getVariance() } / allClusters.size

    companion object {

        fun randomSeedIndices(size: Int, length: Int) : MutableSet<Int> {
            val seedIndices: MutableSet<Int> = LinkedHashSet()
            while (seedIndices.size < size) {
                seedIndices.add((Math.random() * length).toInt())
            }
            return seedIndices
        }

        fun getSeedPoints(indices: Set<Int>, data: List<ArrayPoint>) = indices.map { data[it] }

        fun getDataPoints(seedIndices: Set<Int>, data: List<ArrayPoint>) = data.indices.takeWhile { it !in seedIndices }.map { data[it] }

        fun getKMeans(size: Int, data: List<ArrayPoint>) : KMeans {
            val seedIndices = randomSeedIndices(size, data.lastIndex)
            val seedPoints = getSeedPoints(seedIndices, data)
            val dataPoints = getDataPoints(seedIndices, data)
            val ret = KMeans(seedPoints)
            ret.cluster(dataPoints)
            return ret
        }

        fun getBestKMeans(size: Int, data: List<ArrayPoint>, iterations: Int = data.size / 2) : KMeans{
            val seedPool : MutableSet<MutableSet<Int>> = mutableSetOf()
            var bestKMeans = getKMeans(size, data)
            repeat(iterations - 1) {
                var seedIndices = randomSeedIndices(size, data.lastIndex)
                while (seedIndices in seedPool) { seedIndices = randomSeedIndices(size, data.lastIndex) }
                val seedPoints = getSeedPoints(seedIndices, data)
                val dataPoints = getDataPoints(seedIndices, data)
                val ret = KMeans(seedPoints)
                ret.cluster(dataPoints)
                if(ret < bestKMeans){
                    bestKMeans = ret
                }
            }
            return bestKMeans
        }
    }

    override fun compareTo(other: KMeans): Int = getMeanVariance().compareTo(other.getMeanVariance())

}