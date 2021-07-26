package com.resnik.intel.kd

import com.resnik.math.linear.array.ArrayPoint
import org.junit.Test
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane


class TestKDTree {

    @Test
    fun testKD1(){
        val N = 50000
        val pointList = mutableListOf<KDTreeValue<Double>>()
        repeat(N){
            pointList.add(KDTreeValue(
                ArrayPoint(
                    Math.random(),
                    Math.random()
                ), Math.random()))
        }
        println("Starting Insertion Tests for N=$N")
        var insertion: Long = System.currentTimeMillis()
        val kdTree = KDTree<Double>(2)
        pointList.forEach { kdTree + it }
        insertion = System.currentTimeMillis() - insertion
        println("Insertion Time (ms): $insertion")

        val M = 50000
        val lookupPoints = mutableListOf<ArrayPoint>()
        repeat(M){lookupPoints.add(
            ArrayPoint(
                Math.random(),
                Math.random()
            )
        ) }
        println("Starting Lookup Time for M=$M")
        var lookupTime: Long = System.currentTimeMillis()
        lookupPoints.forEach { kdTree[it] }
        lookupTime = System.currentTimeMillis() - lookupTime
        println("Lookup Time (ms): $lookupTime")
    }

    @Test
    fun testKDImage(){
        val url = URL("https://www.destinationmansfield.com/wp-content/uploads/2018/06/A_Sunday_on_La_Grande_Jatte_Georges_Seurat_1884-600x403.png")
        val input: BufferedImage = ImageIO.read(url)
        val kdTree: KDTree<Color> = KDTree(2)
        val seedPoints = 1000
        repeat(seedPoints){
            val colD = input.width * Math.random()
            val col = colD.toInt()
            val rowD = input.height * Math.random()
            val row = rowD.toInt()
            kdTree.plus(KDTreeValue(ArrayPoint(colD, rowD), Color(input.getRGB(col, row))))
        }
        val output: BufferedImage = BufferedImage(input.width, input.height, input.type)
        var maxDist = -1*Double.MAX_VALUE
        repeat(input.height){row ->
            println("Row: $row / ${input.height}")
            repeat(input.width){col ->
                val node: KDTreeNode<Color>? = kdTree[ArrayPoint(
                    col.toDouble(),
                    row.toDouble()
                )]
                val r = node!!.value.point.distanceTo(
                    ArrayPoint(
                        col.toDouble(),
                        row.toDouble()
                    )
                )
                maxDist = maxDist.coerceAtLeast(r)
            }
        }
        repeat(input.height){row ->
            repeat(input.width){col ->
                val node: KDTreeNode<Color>? = kdTree[ArrayPoint(
                    col.toDouble(),
                    row.toDouble()
                )]
                val distance = node!!.value.point.distanceTo(
                    ArrayPoint(
                        col.toDouble(),
                        row.toDouble()
                    )
                )
                val normalized = 1.0 - distance / maxDist
                val color = node.value.data
                output.setRGB(col, row, Color((color.red * normalized).toInt(), (color.green * normalized).toInt(), (color.blue * normalized).toInt()).rgb)
            }
        }
        val icon = ImageIcon(output)
        val label = JLabel(icon)
        JOptionPane.showMessageDialog(null, label)
    }

    @Test
    fun testNext() {
        val url = URL("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg/1200px-Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg")
        val before: BufferedImage = ImageIO.read(url)

        val w = before.width
        val h = before.height
        var input = resizeImage(before, w/2, h/2)

        val seedPoints = 1000
        val seedPointList : MutableList<Pair<ArrayPoint, Color>> = mutableListOf()
        repeat(seedPoints){
            val colD = input.width * Math.random()
            val col = colD.toInt()
            val rowD = input.height * Math.random()
            val row = rowD.toInt()
            seedPointList.add(Pair(ArrayPoint(rowD, colD), Color(input.getRGB(col, row))))
        }
        val output: BufferedImage = BufferedImage(input.width, input.height, input.type)
        repeat(input.height){row ->
            println("Row: $row / ${input.height}")
            repeat(input.width){col ->
                val curr = ArrayPoint(row.toDouble(), col.toDouble())
                val next = seedPointList.minBy { t -> t.first.distanceTo(curr) }!!
                val color = next.second
                output.setRGB(col, row, color.rgb)
            }
        }
        val icon = ImageIcon(output)
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

}