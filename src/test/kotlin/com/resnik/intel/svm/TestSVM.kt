package com.resnik.intel.svm

import com.resnik.intel.TestRenderDelegate
import com.resnik.math.linear.array.ArrayPoint
import com.resnik.math.linear.array.ArrayVector
import org.junit.Test
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane
import kotlin.math.floor

class TestSVM : TestRenderDelegate() {

    private val minX = -5.0
    private val minY = -5.0
    private val maxX = 5.0
    private val maxY = 5.0
    private val width = 800
    private val height = width

    private fun pointToCoord(pt: ArrayPoint): Pair<Int, Int> {
        val x = pt.values[0]
        val y = pt.values[1]
        val relX = (x - minX) / (maxX - minX)
        val relY = 1.0 - (y - minY) / (maxY - minY)
        return Pair(floor(relX * width).toInt(), floor(relY * height).toInt())
    }

    private fun drawLine(
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

    private fun drawPoint(pt: ArrayPoint, graphics2D: Graphics2D, paint: Color = Color.BLACK) {
        graphics2D.paint = paint
        val coords = pointToCoord(pt)
        graphics2D.fillOval(coords.first, coords.second, width / 50, width / 50)
    }

    @Test
    fun testSVM() {
        val firstDataColor = Color.BLUE
        val spread = 2
        val firstDataMean = ArrayPoint(-2.0, -2.0)
        val firstData = mutableListOf<ArrayPoint>()
        val dataSizeFirst = 20
        repeat(dataSizeFirst) {
            firstData.add(
                ArrayPoint(
                    firstDataMean.x() + spread * (Math.random() * 2 - 1),
                    firstDataMean.y() + spread * (Math.random() * 2 - 1)
                )
            )
        }

        val secondDataColor = Color.RED
        val secondSpread = 1
        val secondDataMean = ArrayPoint(2.0, 2.0)
        val secondData = mutableListOf<ArrayPoint>()
        val dataSizesecond = 20
        repeat(dataSizesecond) {
            secondData.add(
                ArrayPoint(
                    secondDataMean.x() + secondSpread * (Math.random() * 2 - 1),
                    secondDataMean.y() + secondSpread * (Math.random() * 2 - 1)
                )
            )
        }
        val trainingInputs = mutableListOf<ArrayVector>()
        val trainingOutputs = mutableListOf<ArrayVector>()
        firstData.forEach {
            trainingInputs.add(it.toVector())
            trainingOutputs.add(ArrayVector(+1.0))
        }
        secondData.forEach {
            trainingInputs.add(it.toVector())
            trainingOutputs.add(ArrayVector(-1.0))
        }
        val epochs = 10000
        val svm = SVM(2, learningRate = 0.001, lambda = 1.0 / epochs)
        svm.trainBatch(trainingInputs.toTypedArray(), trainingOutputs.toTypedArray(), epochs = epochs)

        // Main Boundary
        val x1: Double = (svm.b - svm.w[1] * maxY) / (svm.w[0])
        val topPoint = ArrayPoint(x1, maxY)
        val x2: Double = (svm.b - svm.w[1] * minY) / (svm.w[0])
        val bottomPoint = ArrayPoint(x2, minY)

        val xL1: Double = (1 + svm.b - svm.w[1] * maxY) / (svm.w[0])
        val Ltop = ArrayPoint(xL1, maxY)
        val xL2: Double = (1 + svm.b - svm.w[1] * minY) / (svm.w[0])
        val Lbottom = ArrayPoint(xL2, minY)

        val xR1: Double = (-1 + svm.b - svm.w[1] * maxY) / (svm.w[0])
        val Rtop = ArrayPoint(xR1, maxY)
        val xR2: Double = (-1 + svm.b - svm.w[1] * minY) / (svm.w[0])
        val Rbottom = ArrayPoint(xR2, minY)

        if (RENDER) {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val graphics: Graphics2D = image.createGraphics()
            graphics.background = Color.WHITE
            graphics.clearRect(0, 0, width, height)
            // draw axes
            drawLine(
                ArrayPoint(minX, 0.0),
                ArrayPoint(maxX, 0.0), graphics
            )
            drawLine(
                ArrayPoint(0.0, minY),
                ArrayPoint(0.0, maxY), graphics
            )

            firstData.forEach { drawPoint(it, graphics, firstDataColor) }
            secondData.forEach { drawPoint(it, graphics, secondDataColor) }

            drawLine(topPoint, bottomPoint, graphics, Color.ORANGE, 5.0f)
            drawLine(Ltop, Lbottom, graphics, Color.BLUE, 3.0f)
            drawLine(Rtop, Rbottom, graphics, Color.RED, 3.0f)

            graphics.dispose()
            val icon = ImageIcon(image)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }
    }

}