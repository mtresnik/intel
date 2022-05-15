package com.resnik.intel.csp

import com.resnik.intel.TestRenderDelegate
import com.resnik.intel.csp.constraint.local.LocalConstraint
import com.resnik.intel.csp.tree.CSPTree
import com.resnik.intel.csp.tree.async.CSPCoroutine
import com.resnik.intel.csp.tree.async.CSPDomainAsync
import com.resnik.math.linear.array.geometry.Rect
import org.junit.Test
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane


class TestMapColoring : TestRenderDelegate() {

    class MCC(private val from : String, private val to : String) : LocalConstraint<String, String>(listOf(from, to)){

        override fun isPossiblySatisfied(assignment: Map<String, String>): Boolean {
            if(from !in assignment || to !in assignment)
                return true
            return assignment[from]!! != assignment[to]!!
        }

    }

    private val WA = "Western Australia"
    private val NT = "Northern Territory"
    private val SA = "South Australia"
    private val Q  = "Queensland"
    private val NSW= "New South Wales"
    private val V  = "Victoria"
    private val T  = "Tasmania"

    private val red     = "red"
    private val green   = "green"
    private val blue    = "blue"

    private val colors = listOf(red, green, blue)

    private val variables = listOf(WA, NT, SA, Q, NSW, V, T)
    private val domains = mutableMapOf<String, List<String>>()
    private val constraints = mutableListOf<MCC>()

    init {
        variables.forEach { variable -> domains[variable] = colors }
        constraints.add(MCC(WA, NT))
        constraints.add(MCC(WA, SA))

        constraints.add(MCC(SA, NT))

        constraints.add(MCC(Q, NT))
        constraints.add(MCC(Q, SA))
        constraints.add(MCC(Q, NSW))

        constraints.add(MCC(NSW, SA))

        constraints.add(MCC(V, SA))
        constraints.add(MCC(V, NSW))
        constraints.add(MCC(V, T))
    }

    @Test
    fun testCSPAustraliaLinear() {
        val start = System.currentTimeMillis()
        val csp = CSPTree(domains)
        csp.addConstraints(constraints)
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
    }

    @Test
    fun testCSPAustraliaAsync() {
        val start = System.currentTimeMillis()
        val csp = CSPDomainAsync(domains)
        csp.addConstraints(constraints)
        val solutions = csp.findAllSolutions()
        val numSolutions = solutions.size
        println("Number of solutions: $numSolutions")
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
    }

    @Test
    fun testCSPAustraliaProbabilities() {
        val start = System.currentTimeMillis()
        val csp = CSPDomainAsync(domains)
        csp.addConstraints(constraints)
        val probabilities = csp.getProbabilities()
        println(probabilities)
        val time = System.currentTimeMillis() - start
        println("Time Taken: $time")
    }

    class MapColoringConstraint<DOMAIN>(val from : Int, val to : Int) : LocalConstraint<Int, DOMAIN>(listOf(from, to)) {

        override fun isPossiblySatisfied(assignment: Map<Int, DOMAIN>): Boolean {
            if(from !in assignment || to !in assignment)
                return true
            return assignment[from]!! != assignment[to]!!
        }

    }

    @Test
    fun testMapColoringRects() {
        val allTiles = mutableListOf<Rect>()
        val numRectWidth = 8
        val numRectHeight = 8
        val width = 1.0
        val height = 1.0

        repeat(numRectWidth) { col ->
            val x = col * width
            repeat(numRectHeight) { row ->
                val y = row * height
                allTiles.add(Rect(x, y, width, height))
            }
        }

        val neighbors = Array<MutableList<Int>>(allTiles.size){ mutableListOf() }
        allTiles.forEachIndexed { index, rect ->
            neighbors[index] = rect.getNeighborIndices(allTiles).toMutableList()
        }
        val domains = listOf(Color.WHITE.rgb, Color.BLACK.rgb)
        val domainMap = allTiles.indices.associateWith { domains }
        val csp = CSPCoroutine(domainMap)
        allTiles.forEachIndexed { index, _ ->
            val removeFrom = neighbors[index]
            removeFrom.forEach { neighborIndex ->
                csp.addConstraint(MapColoringConstraint(index, neighborIndex))
                neighbors[neighborIndex].remove(index)
            }
        }
        val first = csp.getFirstSolution()
        val rectPixels = 32
        val imageWidth = rectPixels * numRectWidth
        val imageHeight = rectPixels * numRectHeight
        val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
        var index = 0
        repeat(numRectWidth) { col ->
            val imageColStart = col * rectPixels
            repeat(numRectHeight) { row ->
                val imageRowStart = row * rectPixels
                val colorRgb = first!![index]!!
                repeat(rectPixels) { col1 ->
                    val imageCol = imageColStart + col1
                    repeat(rectPixels) { row1 ->
                        val imageRow = imageRowStart + row1
                        image.setRGB(imageCol, imageRow, colorRgb)
                    }
                }
                index++
            }
        }
        if (RENDER) {
            val icon = ImageIcon(image)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }
    }

    @Test
    fun testMapColoringDifferentSized() {
        val allTiles = mutableListOf<Rect>(
            Rect(0.0, 0.0, 1.0, 1.0), Rect(1.0, 0.0, 2.0, 1.0), Rect(3.0, 0.0, 1.0, 1.0),
            Rect(0.0, 1.0, 1.0, 1.0), Rect(1.0, 1.0, 2.0, 2.0), Rect(3.0, 1.0, 1.0, 1.0),
            Rect(0.0, 2.0, 1.0, 1.0), Rect(3.0, 2.0, 1.0, 2.0),
            Rect(0.0, 3.0, 3.0, 1.0)
        )
        val neighbors = Array<MutableList<Int>>(allTiles.size){ mutableListOf() }
        allTiles.forEachIndexed { index, rect ->
            neighbors[index] = rect.getNeighborIndices(allTiles).toMutableList()
        }
        val firstColor = Color(0,128,0)
        val secondColor = Color(0, 0,128)
        val thirdColor = Color(128, 0,0)
        val fourthColor = Color(0, 128,128)
        val domains = listOf(firstColor.rgb, secondColor.rgb, thirdColor.rgb, fourthColor.rgb)
        val domainMap = allTiles.indices.associateWith { domains }
        val csp = CSPTree(domainMap)
        allTiles.forEachIndexed { index, _ ->
            val removeFrom = neighbors[index]
            removeFrom.forEach { neighborIndex ->
                csp.addConstraint(MapColoringConstraint(index, neighborIndex))
                neighbors[neighborIndex].remove(index)
            }
        }
        val first = csp.getFirstSolution()

        val rectsWidth = 4.0
        val rectsHeight = 4.0
        val imageWidth = 300
        val imageHeight = 300
        val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)

        fun pixelsToRelative(col : Int, row : Int) : Pair<Double, Double> {
            return (col.toDouble() / (imageWidth - 1) to row.toDouble() / (imageHeight - 1))
        }

        fun relativeToPixels(x : Double, y : Double) : Pair<Int, Int> {
            val col = (x*(imageWidth - 1)).toInt().coerceIn(0, imageWidth - 1)
            val row = (y*(imageHeight - 1)).toInt().coerceIn(0, imageHeight - 1)
            return Pair(col, row)
        }

        fun tileToRelative(tx : Double, ty : Double) : Pair<Double, Double> {
            return Pair((tx/rectsWidth).coerceIn(0.0, 1.0), (ty/rectsHeight).coerceIn(0.0, 1.0))
        }

        repeat(imageHeight) { row ->
            repeat(imageWidth) { col ->
                image.setRGB(col, row, Color.BLACK.rgb)
            }
        }

        allTiles.forEachIndexed { index, rect ->
            val color = first!![index]!!
            val (xRel, yRel) = tileToRelative(rect.x, rect.y)
            val (startCol, startRow) = relativeToPixels(xRel, yRel)
            val (rectWidthRel, rectHeightRel) = tileToRelative(rect.width, rect.height)
            val (rectWidthPx, rectHeightPx) = relativeToPixels(rectWidthRel, rectHeightRel)

            repeat(rectHeightPx) { row ->
                val imageRow = row + startRow
                repeat(rectWidthPx) { col ->
                    val imageCol = col + startCol
                    image.setRGB(imageCol, imageRow, color)
                }
            }

        }
        if (RENDER) {
            val icon = ImageIcon(image)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }

    }




}