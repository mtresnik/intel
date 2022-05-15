package com.resnik.intel.genetic

import com.resnik.intel.TestRenderDelegate
import com.resnik.math.linear.array.ArrayPoint
import org.junit.Ignore
import org.junit.Test
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane
import kotlin.math.*

@Ignore
class TestGenetic : TestRenderDelegate() {

    val minX = -3.0
    val minY = -3.0
    val maxX = 3.0
    val maxY = 3.0
    val width = 800
    val height = width

    fun pointToCoord(pt: ArrayPoint): Pair<Int, Int> {
        val x = pt.values[0]
        val y = pt.values[1]
        val relX = (x - minX) / (maxX - minX)
        val relY = 1.0 - (y - minY) / (maxY - minY)
        return Pair(floor(relX * width).toInt(), floor(relY * height).toInt())
    }

    fun drawLine(
        pt1: ArrayPoint,
        pt2: ArrayPoint,
        graphics2D: Graphics2D,
        paint: Color = Color.BLACK,
        stroke: Float = 1.0f
    ) {
        graphics2D.paint = paint
        graphics2D.stroke = BasicStroke(stroke)
        val firstCoords = pointToCoord(pt1)
        val secondCoords = pointToCoord(pt2)
        println("Drawline from: $firstCoords to $secondCoords")
        graphics2D.drawLine(firstCoords.first, firstCoords.second, secondCoords.first, secondCoords.second)
    }

    fun drawPoint(pt: ArrayPoint, graphics2D: Graphics2D, paint: Color = Color.BLACK) {
        graphics2D.paint = paint
        val coords = pointToCoord(pt)
        graphics2D.fillOval(coords.first, coords.second, width / 50, width / 50)
    }

    @Test
    fun testGeneticString() {
        testGeneticString("Hello!")
    }

    @Test
    fun testGeneticString2() {
        testGeneticString("Goodbye!", 5000)
    }

    fun testGeneticString(testString: String, numEpochs: Int = 500) {
        val fitnessFunction = object : GeneticFitnessFunction<Char> {
            override fun evaluateFitness(individual: Chromosome<Char>): Double {
                val chars = individual.values.map { gene -> gene.value }
                return testString.indices.sumBy { if (testString[it] == chars[it]) 1 else 0 }.toDouble()
            }
        }
        val geneFactory = CharacterGeneFactory()
        val geneticAlgorithm = GeneticAlgorithm(
            numberOfGenes = testString.length,
            populationSize = 100,
            fitnessFunction = fitnessFunction,
            geneFactory = geneFactory
        )
        geneticAlgorithm.trainEpoch(numEpochs)
    }

    @Test
    fun testGeneticPoints() {


        val equation1 = object : Function2<Double, Double, Double> {
            override fun invoke(x: Double, y: Double): Double {
                return 3 * (1 - x).pow(2) * exp(-(x.pow(2))) - (y + 1).pow(2) - 10 * (x / 5 - x.pow(3) - y.pow(5)) * exp(
                    -x.pow(2) - y.pow(2)
                ) - (1.0 / 3) * exp(-(x + 1).pow(2) - y.pow(2))
            }
        }

        val equation2 = object : Function2<Double, Double, Double> {
            override fun invoke(x: Double, y: Double): Double {
                return (x * x + y * y) * ((x - 2) * (x - 2) + (y - 2) * (y - 2) + 0.1)
            }
        }

        var equation = equation2

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, width, height)

        var minValue = Double.MAX_VALUE
        var maxValue = -Double.MAX_VALUE
        var minPoint = ArrayPoint(0.0, 0.0)
        val tripleList = mutableListOf<Triple<Int, Int, Double>>()
        repeat(width) { col ->
            val t = col.toDouble() / width
            val x = t * (maxX - minX) + minX
            repeat(height) { row ->
                val s = row.toDouble() / height
                val y = s * (maxY - minY) + minY
                val value = equation(x, y)
                maxValue = max(value, maxValue)
                if (value < minValue) {
                    minPoint = ArrayPoint(x, y)
                }
                minValue = min(value, minValue)
                tripleList.add(Triple(col, row, value))
            }
        }


        val fitnessFunction = object : GeneticFitnessFunction<Double> {

            override fun evaluateFitness(individual: Chromosome<Double>): Double {
                val firstDouble = individual.values[0].value
                val secondDouble = individual.values[1].value
                val output = equation(firstDouble, secondDouble)
                return (maxValue - output) / (maxValue - minValue)
            }

        }

        val geneFactory = NormalizedDoubleGeneFactory(minX, maxX, 0.2)
        val chromosomeFactory = NormalizedDoubleChromosomeFactory(geneFactory, geneFactory)

        val geneticAlgorithm = GeneticAlgorithm(
            numberOfGenes = 2,
            populationSize = 50,
            fitnessFunction = fitnessFunction,
            geneFactory = geneFactory,
            chromosomeFactory = chromosomeFactory
        )
        geneticAlgorithm.trainEpoch(200)


        tripleList.forEach { triple ->
            val gradient = (255.0 * (triple.third - minValue) / (maxValue - minValue)).toInt()
            image.setRGB(triple.first, triple.second, Color(gradient, gradient, gradient).rgb)
        }

        val bestPoints = mutableListOf<Pair<Double, Double>>()

        geneticAlgorithm.historicalFittest.forEach { individual ->
            val firstDouble = individual.values[0].value
            val secondDouble = individual.values[1].value
            val pair = Pair(firstDouble, secondDouble)
            bestPoints.add(pair)
        }

        bestPoints.zipWithNext { first, second ->
            val firstPoint = ArrayPoint(first.first, first.second)
            val secondPoint = ArrayPoint(second.first, second.second)
            drawLine(firstPoint, secondPoint, graphics, Color.RED)
        }

        if (RENDER) {
            drawPoint(minPoint, graphics, Color.BLUE)

            graphics.dispose()
            val icon = ImageIcon(image)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }
    }

}