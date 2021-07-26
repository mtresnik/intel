package com.resnik.intel.neural

import com.resnik.math.linear.array.ArrayVector
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane

class TestRNN {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            testRNN()
        }

        fun testRNN(){
            val sundayUrl = URL("https://www.destinationmansfield.com/wp-content/uploads/2018/06/A_Sunday_on_La_Grande_Jatte_Georges_Seurat_1884-600x403.png")
            val sunday: BufferedImage = ImageIO.read(sundayUrl)
            val scaledSize = 128
            val sundayScaled : BufferedImage = BufferedImage(scaledSize, scaledSize, sunday.type)
            var graphics = sundayScaled.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            graphics.drawImage(sunday, 0, 0, scaledSize, scaledSize, null)
            graphics.dispose()

            // Vectorize image
            // Convert to rgb array
            val pixels = mutableListOf<ArrayVector>()
            val uniquePixels = mutableListOf<ArrayVector>()
            repeat(scaledSize){row ->
                repeat(scaledSize) {col ->
                    val color = Color(sundayScaled.getRGB(col, row))
                    val r = color.red / 255.0
                    val g = color.green / 255.0
                    val b = color.blue / 255.0
                    val vec = ArrayVector(r, g, b)
                    pixels.add(vec)
                    if(vec !in uniquePixels){
                        uniquePixels.add(vec)
                    }
                }
            }
            val expectedData = uniquePixels.toTypedArray()
            val outputSize = pixels.size
            val numCategories: Int = uniquePixels.size
            println("NumCategories: $numCategories \t Output Size: $outputSize")

            val iterations = 100
            val learningRate = 0.001
            val rnn = RecurrentNeuralNetwork(3, 3, numCategories, expectedData, learningRate)
            println("Created the RNN")
            (0..iterations).forEach{
                rnn.forward()
                val error = rnn.backward()
                if(it % 10 == 0){
                    println("Iteration: $it\t Error: $error")
                }
            }

            val monaLisaURL = URL("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg/1200px-Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg")
            val monaLisa: BufferedImage = ImageIO.read(monaLisaURL)
            val monaLisaScaled : BufferedImage = BufferedImage(256, 256, monaLisa.type)
            graphics = sundayScaled.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            graphics.drawImage(sunday, 0, 0, 256, 256, null)
            graphics.dispose()

            val icon = ImageIcon(monaLisaScaled)
            val label = JLabel(icon)
            JOptionPane.showMessageDialog(null, label)
        }
    }
}