package com.resnik.intel.csp

import com.resnik.intel.csp.constraint.local.LocalConstraint
import com.resnik.intel.csp.preprocessors.AC3Preprocessor
import com.resnik.intel.csp.tree.CSPTree
import com.resnik.intel.csp.tree.async.CSPDomainAsync
import com.resnik.intel.csp.tree.async.CSPCoroutine
import org.junit.Test
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane

class TestNQueens {

    class QC(columns : List<Int>) : LocalConstraint<Int, Int>(columns) {

        override fun isPossiblySatisfied(assignment: Map<Int, Int>): Boolean {
            // Map is essentially a 2d array for all queens currently placed...
            // Since we know they can't be on the same row / col the uniqueness holds
            assignment.forEach { (col1, row1) ->
                // Iterate through other queen tiles in assignment
                assignment.forEach { (col2, row2) ->
                    if(col1 != col2) {
                        if(row1 == row2)
                            return false
                        if(kotlin.math.abs(row1 - row2) == kotlin.math.abs(col1 - col2))
                            return false
                    }
                }
            }
            return true
        }

    }

    @Test
    fun testNQueensAll2() {
        val n = 9
        val start = System.currentTimeMillis()
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val csp = IterativeCSP(rows)
        csp.addConstraint(QC(cols))
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        // time (ms) ~= e^(1.6*(n - 4)) + 13
        println("Time Taken: $time")
    }

    @Test
    fun testNQueensTree() {
        val n = 13
        val start = System.currentTimeMillis()
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val csp = CSPTree(rows)
        csp.addConstraint(QC(cols))
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
        drawBoard(n, solutions)
    }

    private fun drawBoard(n : Int, solutions : Collection<Map<Int, Int>>) {
        val squareSize = 20
        val imageSize = n * squareSize
        val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB)
        val frequency = Array(n){ DoubleArray(n) }
        solutions.forEach { solution ->
            solution.keys.forEach { key ->
                val row = key - 1
                val col = solution[key]!! -1
                frequency[row][col]++
            }
        }
        // Scale frequency
        val min = frequency.minOf { it.minOrNull()!! }
        val max = frequency.maxOf { it.maxOrNull()!! }
        val delta = max - min
        val scaled = Array(n) { row -> DoubleArray(n) { col -> (frequency[row][col] - min) / delta } }

        repeat(imageSize) { imageRow ->
            val row = imageRow / squareSize
            repeat(imageSize) { imageCol ->
                val col = imageCol / squareSize
                val scaledValue = scaled[row][col].toFloat().coerceAtMost(0.99f)
                val color = Color(scaledValue, scaledValue, scaledValue).rgb
                image.setRGB(imageCol, imageRow, color)
            }
        }
        val icon = ImageIcon(image)
        val label = JLabel(icon)
        JOptionPane.showMessageDialog(null, label)
    }

    @Test
    fun testNQueensDomainAsync() {
        val n = 13
        val start = System.currentTimeMillis()
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val csp = CSPDomainAsync(rows, sortVariables = true)
        csp.addConstraint(QC(cols))
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
        drawBoard(n, solutions)
    }

    @Test
    fun testNQueensDomainAsyncFirst() {
        val n = 22
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val start = System.currentTimeMillis()
        val csp = CSPDomainAsync(rows, sortVariables = true)
        csp.addConstraint(QC(cols))
        csp.getFirstSolution()
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")

        val start2 = System.currentTimeMillis()
        val csp2 = CSPDomainAsync(rows, sortVariables = true)
        csp2.addConstraint(QC(cols))
        csp2.getFirstSolution()
        val time2 = System.currentTimeMillis() - start2
        println("Time Taken: $time2")
    }

    @Test
    fun testNQueensDomainCoroutineFirst() {
        val n = 22
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val start = System.currentTimeMillis()
        val csp = CSPCoroutine(rows, sortVariables = true)
        csp.addConstraint(QC(cols))
        csp.getFirstSolution()
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")

        // See if faster on next iteration
        val start2 = System.currentTimeMillis()
        val csp2 = CSPCoroutine(rows, sortVariables = true)
        csp2.addConstraint(QC(cols))
        csp2.getFirstSolution()
        val time2 = System.currentTimeMillis() - start2
        println("Time Taken: $time2")
    }

    @Test
    fun testNQueensDomainCoroutineAll() {
        val n = 9
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val start = System.currentTimeMillis()
        val csp = CSPCoroutine(rows, sortVariables = true)
        csp.addConstraint(QC(cols))
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
        drawBoard(n, solutions)
    }

    @Test
    fun testNQueensInferredAsync() {
        val n = 13
        val start = System.currentTimeMillis()
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val csp = CSPFactory.createCSP(rows, sortVariables = true)
        println("Csp Type: ${csp::class}")
        csp.addConstraint(QC(cols))
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
        drawBoard(n, solutions)
    }

    @Test
    fun testNQueensDomainCoroutinePreprocess() {
        val n = 13
        val cols = (1..n).toList()
        val rows = mutableMapOf<Int, List<Int>>()
        (1 .. n).forEach{rows[it] = (1..n).toList()}
        val csp = CSPCoroutine(rows, sortVariables = true, preprocessors = listOf(AC3Preprocessor()))
        csp.addConstraint(QC(cols))
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        println("Time Taken: ${csp.finalTime()}")
        drawBoard(n, solutions)
    }

}