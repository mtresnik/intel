package com.resnik.intel.neural.conv

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

class TestConv {

    companion object {
        @JvmStatic
        fun main(args : Array<String>){
            testNetwork()
        }

        fun testNetwork(){
            val monaLisaURL = URL("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg/1200px-Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg")
            val monaLisa: BufferedImage = ImageIO.read(monaLisaURL)
            val monaLisaScaled : BufferedImage = BufferedImage(256, 256, monaLisa.type)
            val graphics = monaLisaScaled.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
            graphics.drawImage(monaLisa, 0, 0, 256, 256, null)
            graphics.dispose()
            val input = monaLisaScaled.toTensor()
            val network = TensorNetwork()
            network.add(ConvolutionalLayer(8, intArrayOf(3, 3), input.dim.values))
            network.add(ReLULayer(network.outputDimensions()))
            network.add(MaxPoolLayer(intArrayOf(2,2), intArrayOf(2,2), network.outputDimensions().values))

            network.add(ConnectedLayer(2, network.outputDimensions().values))
            network.add(SoftmaxLayer(network.outputDimensions()))

            network.forward(input)
        }
    }

}