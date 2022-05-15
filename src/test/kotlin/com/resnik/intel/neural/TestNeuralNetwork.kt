package com.resnik.intel.neural

import com.resnik.intel.TestRenderDelegate
import com.resnik.intel.neural.TransferFunction.Companion.relu
import com.resnik.intel.neural.TransferFunction.Companion.tanh
import com.resnik.math.linear.array.ArrayVector
import org.junit.Test
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane
import kotlin.math.PI
import kotlin.math.sin

class TestNeuralNetwork : TestRenderDelegate() {

    @Test
    fun testNeural1() {
        val network = FeedForwardNeuralNetwork(2, 2, 1, transferFunction = relu)
        network.learningRate = 0.1
        network.momentum = 0.0
        network.trainRandom(
            arrayOf(ArrayVector(0.0, 0.0), ArrayVector(1.0, 0.0), ArrayVector(0.0, 1.0), ArrayVector(1.0, 1.0)),
            arrayOf(ArrayVector(0.0), ArrayVector(1.0), ArrayVector(1.0), ArrayVector(0.0))
        )
        println(network.predict(ArrayVector(0.0, 0.0)))
        println(network.predict(ArrayVector(1.0, 0.0)))
        println(network.predict(ArrayVector(0.0, 1.0)))
        println(network.predict(ArrayVector(1.0, 1.0)))

        val width = 500
        val height = width
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, width, height)
        repeat(width) { col ->
            val x = col.toDouble() / (width - 1)
            repeat(height) { row ->
                val y = row.toDouble() / (height - 1)
                val z = network.predict(ArrayVector(x, y))[0].toFloat()
                image.setRGB(col, row, Color(z, z, z).rgb)

            }
        }
        graphics.dispose()
        if (RENDER) {
            val icon = ImageIcon(image)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }
    }


    @Test
    fun testSin() {
        val network = FeedForwardNeuralNetwork(1, 5, 5, 1, transferFunction = tanh)
        val inputs = mutableListOf<ArrayVector>()
        val outputs = mutableListOf<ArrayVector>()
        val width = 200
        val max = width
        (0..max).forEach {
            val x = 2 * PI * it / (max - 1.0)
            val y = sin(x)
            inputs.add(ArrayVector(x))
            outputs.add(ArrayVector(y))
        }
        network.learningRate = 8.0 / width
        network.momentum = network.learningRate / 2
        network.trainBatch(inputs.toTypedArray(), outputs.toTypedArray(), epochs = 1000, batchSize = 4)
        val height = width
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, width, height)
        repeat(width) { col ->
            val x = 2 * PI * col.toDouble() / (width - 1)
            val y = ((network.predict(ArrayVector(x))[0].toFloat() + 1) / 2).coerceIn(-1.0f, 1.0f)
            val expected = ((sin(x).toFloat() + 1) / 2).coerceIn(-1.0f, 1.0f)
            val rowY = (y * (height - 1)).toInt()
            val expectedRow = (expected * (height - 1)).toInt()
            image.setRGB(col, rowY, Color.BLUE.rgb)
            image.setRGB(col, expectedRow, Color.RED.rgb)

        }
        graphics.dispose()
        if (RENDER) {
            val icon = ImageIcon(image)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }
    }


}