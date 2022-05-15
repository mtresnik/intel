package com.resnik.intel.quadtree.image

import com.resnik.intel.quadtree.AbstractQuadTree
import com.resnik.math.linear.array.ArrayPoint2d
import com.resnik.math.linear.array.geometry.Rect
import java.awt.Color
import java.awt.image.BufferedImage

class ImageQuadTree(val image: BufferedImage, val threshold: Double, rect: ImageQuadTreeRect = rectFromImage(image)) :
    AbstractQuadTree<ImageQuadTreeRect, ImageQuadTreeData, ImageQuadTree>(rect) {

    fun isLeaf(): Boolean = !this.isDivided || this.childrenNonNull().isEmpty()

    init {
        // Average color is stored in bounds rect for quad tree
        if (averageDistance(bounds) >= threshold) {
            // Split children, else it's a leaf node with the bounds average color
            val subDivided = bounds.subDivide()
            topRight = createIfNonNull(image, threshold, subDivided.topRight)
            topLeft = createIfNonNull(image, threshold, subDivided.topLeft)
            bottomLeft = createIfNonNull(image, threshold, subDivided.bottomLeft)
            bottomRight = createIfNonNull(image, threshold, subDivided.bottomRight)
            this.isDivided = true
        } else {
        }
    }

    fun childrenNonNull(): List<ImageQuadTree> = arrayOf(topRight, topLeft, bottomLeft, bottomRight).filterNotNull()

    private fun getAverageColor(rect: Rect): RGB {
        if (rect is ImageQuadTreeRect && rect.averageColor != null)
            return rect.averageColor!!
        val x = rect.x.toInt()
        val y = rect.y.toInt()
        var sumRGB = RGB(0.0, 0.0, 0.0)
        var numPass: Double = 0.0
        repeat(rect.width.toInt()) { rowIndex ->
            val row = rowIndex + y
            repeat(rect.height.toInt()) { colIndex ->
                val col = colIndex + x
                try {
                    val currRGB = getImageRGB(row, col)
                    sumRGB += currRGB
                    numPass++
                } catch (e: Exception) {
                }
            }
        }
        return with((sumRGB / numPass).coerceIn()) {
            if (rect is ImageQuadTreeRect)
                rect.averageColor = this
            this
        }
    }

    // Manhattan Distance
    private fun averageDistance(rect: Rect): Double {
        var distanceSum = 0.0
        val averageColor = getAverageColor(rect)
        val x = rect.x.toInt()
        val y = rect.y.toInt()
        var numPassing = 0.0
        repeat(rect.width.toInt()) { rowIndex ->
            val row = rowIndex + y
            repeat(rect.height.toInt()) { colIndex ->
                val col = colIndex + x
                try {
                    val currColor = getImageRGB(row, col)
                    val currSum = (averageColor - currColor).abs().sum()
                    numPassing++
                    distanceSum += currSum
                } catch (e: Exception) {
                }
            }
        }
        return distanceSum / (numPassing * 3)
    }

    private fun getImageRGB(row: Int, col: Int): RGB {
        val intVal = image.getRGB(col, row)
        val color = Color(intVal)
        val r = color.red / 255.0
        val g = color.green / 255.0
        val b = color.blue / 255.0
        return RGB(r, g, b)
    }

    private fun getRGB(x: Int, y: Int): RGB? {
        if (this.isLeaf()) {
            return this.bounds.averageColor
        }
        val child = this.childrenNonNull().firstOrNull { it.bounds.contains(ArrayPoint2d(x.toDouble(), y.toDouble())) }
        return child?.getRGB(x, y)
    }

    fun getColor(row: Int, col: Int): Color? = getRGB(col, row)?.toAwtColor()

    override fun collapseData(): ImageQuadTreeData {
        val ret = ImageQuadTreeData()
        collapseDataRecursive(ret)
        return ret
    }

    private fun collapseDataRecursive(currData: ImageQuadTreeData) {
        if (this.isLeaf()) {
            currData.rects.add(this.bounds)
        } else {
            this.childrenNonNull().forEach { it.collapseDataRecursive(currData) }
        }
    }

    object Thresholds {
        val VERY_LOW = 5 / 255.0
        val LOW = 10 / 255.0
        val MEDIUM = 20 / 255.0
        val HIGH = 30 / 255.0
        val VERY_HIGH = 40 / 255.0
    }

    companion object {

        private fun rectFromImage(image: BufferedImage): ImageQuadTreeRect =
            ImageQuadTreeRect(Rect(0.0, 0.0, image.width.toDouble(), image.height.toDouble()))

        private fun createIfNonNull(image: BufferedImage, threshold: Double, rect: Rect?): ImageQuadTree? =
            if (rect != null) ImageQuadTree(image, threshold, ImageQuadTreeRect(rect)) else null

    }
}