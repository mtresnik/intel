package com.resnik.intel.cluster

import com.resnik.math.linear.array.ArrayPoint
import org.junit.Test
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane

class TestCluster {

    @Test
    fun testRandom() {
        val width = 800
        val height = width
        val clusterSize = 20
        val numPoints = clusterSize * 2
        val pixelsPerCluster = width / 2
        val points = List(numPoints){
            ArrayPoint(
                Math.random() * (width - 1),
                Math.random() * (height - 1)
            )
        }
        val kmeans = KMeans.getBestKMeans(clusterSize, points)

        val palette = Array(clusterSize){Color(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat()).rgb}

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0,0, width, height)
        repeat(width){ col ->
            repeat(height){ row ->
                image.setRGB(col, row, palette[kmeans.getClusterIndex(
                    ArrayPoint(
                        col.toDouble(),
                        row.toDouble()
                    )
                )])
            }
        }
        graphics.paint = Color.BLACK
        kmeans.allClusters.forEach { cluster ->
            val mean = cluster.getMean()
            graphics.fillOval(mean[0].toInt(), mean[1].toInt(), 5, 5)
        }
        graphics.dispose()
        val icon = ImageIcon(image)
        val label = JLabel(icon)
        JOptionPane.showMessageDialog(null, label)


    }

}