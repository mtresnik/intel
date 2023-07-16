package com.resnik.intel.quadtree.point

import com.resnik.intel.quadtree.image.ImageQuadTree
import com.resnik.math.linear.array.ArrayPoint2d
import com.resnik.math.linear.array.geometry.ShapeCollection
import org.junit.Ignore
import org.junit.Test
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane

class TestQuadtree {

    @Test
    @Ignore
    fun testQuadTree1() {
        // gen random points, show boxes
        val size = 500
        val root = PointQuadTreeRect(0.0, 0.0, size.toDouble(), size.toDouble())
        val random = Random(System.currentTimeMillis())
        val quadTree = PointQuadTree(root)
        val numPoints = 1000
        repeat(numPoints) {
            val x = random.nextGaussian() * size
            val y = random.nextGaussian() * size
            quadTree.insertIfContained(ArrayPoint2d(x, y))
        }
        val data = quadTree.collapseData()
        val shapeCollection = ShapeCollection(width = size + 1, height = size + 1, pointRadius = 4)
        data.points.forEach { point ->
            shapeCollection.addPoint(point, Color.BLUE)
        }
        shapeCollection.render()
    }

    @Ignore
    @Test
    fun testQuadTree2() {
        val url = URL("https://ssti.us/wp-content/uploads/sites/1303/2020/08/blog8.31speedlimit.jpg")
        val input: BufferedImage = ImageIO.read(url)
        val imageQuadTree = ImageQuadTree(input, threshold = ImageQuadTree.Thresholds.VERY_HIGH)
        val output = BufferedImage(input.width, input.height, input.type)
        repeat(input.height) { row ->
            repeat(input.width) { col ->
                val color = imageQuadTree.getColor(row, col)
                val rgb = color?.rgb ?: input.getRGB(col, row)
                output.setRGB(col, row, rgb)
            }
        }
        val icon = ImageIcon(output)
        val label = JLabel(icon)
        JOptionPane.showMessageDialog(null, label)
    }

}