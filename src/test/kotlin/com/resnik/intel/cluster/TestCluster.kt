package com.resnik.intel.cluster

import com.resnik.math.linear.array.ArrayPoint
import org.junit.Ignore
import org.junit.Test
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane

class TestCluster {

    @Test
    @Ignore
    fun testRandom() {
        val width = 800
        val height = width
        val clusterSize = 20
        val numPoints = clusterSize * 2
        val points = List(numPoints) {
            ArrayPoint(
                Math.random() * (width - 1),
                Math.random() * (height - 1)
            )
        }
        val kmeans = KMeans.getBestKMeans(clusterSize, points)

        val palette = Array(clusterSize) {
            Color(
                Math.random().toFloat(),
                Math.random().toFloat(),
                Math.random().toFloat()
            ).rgb
        }

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, width, height)
        repeat(width) { col ->
            repeat(height) { row ->
                image.setRGB(
                    col, row, palette[kmeans.getClusterIndex(
                        ArrayPoint(
                            col.toDouble(),
                            row.toDouble()
                        )
                    )]
                )
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

    @Throws(IOException::class)
    fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        val resizedImage = BufferedImage(targetWidth, targetHeight, originalImage.type)
        val graphics2D = resizedImage.createGraphics()
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
        graphics2D.dispose()
        return resizedImage
    }

    @Test
    @Ignore
    fun testImageCompression() {
        val url =
            URL("https://www.destinationmansfield.com/wp-content/uploads/2018/06/A_Sunday_on_La_Grande_Jatte_Georges_Seurat_1884-600x403.png")
        val originalImage: BufferedImage = ImageIO.read(url)
        val clusterSize = 16
        val ratio = originalImage.height.toDouble() / originalImage.width.toDouble()
        val newWidth = 300
        val newHeight = (ratio * 300).toInt()
        val input = resizeImage(originalImage, newWidth, newHeight)
        println("Image Resized to: ($newWidth, $newHeight)")
        val width = input.width
        val height = input.height
        val allPoints = mutableListOf<ArrayPoint>()
        repeat(height) { row ->
            repeat(width) { col ->
                val color = Color(input.getRGB(col, row))
                allPoints.add(ArrayPoint(color.red.toDouble(), color.green.toDouble(), color.blue.toDouble()))
            }
        }
        allPoints.shuffle()
        val points = allPoints.take(500)
        val kmeans = KMeans.getBestKMeans(clusterSize, points)
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()
        graphics.background = Color.WHITE
        graphics.clearRect(0, 0, width, height)
        repeat(width) { col ->
            repeat(height) { row ->
                val color = Color(input.getRGB(col, row))
                val closestCluster =
                    kmeans[ArrayPoint(color.red.toDouble(), color.green.toDouble(), color.blue.toDouble())]
                val meanColorPoint = closestCluster.getMean()
                val red = meanColorPoint[0].toInt().coerceIn(0, 255)
                val green = meanColorPoint[1].toInt().coerceIn(0, 255)
                val blue = meanColorPoint[2].toInt().coerceIn(0, 255)
                val meanColor = Color(red, green, blue)
                image.setRGB(col, row, meanColor.rgb)
            }
        }
        graphics.dispose()

        val icon = ImageIcon(image)
        val label = JLabel(icon)
        JOptionPane.showMessageDialog(null, label)

    }

}