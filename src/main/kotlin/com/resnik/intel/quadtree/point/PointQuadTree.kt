package com.resnik.intel.quadtree.point

import com.resnik.intel.quadtree.AbstractQuadTree
import com.resnik.math.linear.array.ArrayPoint2d
import com.resnik.math.linear.array.geometry.Rect

open class PointQuadTree(bounds: PointQuadTreeRect) :
    AbstractQuadTree<PointQuadTreeRect, PointQuadTreeData, PointQuadTree>(bounds) {

    private fun subdivide() {
        val subDivided = bounds.subDivide()
        topRight = createIfNonNull(subDivided.topRight)
        topLeft = createIfNonNull(subDivided.topLeft)
        bottomLeft = createIfNonNull(subDivided.bottomLeft)
        bottomRight = createIfNonNull(subDivided.bottomRight)
        this.bounds.points.forEach { pt ->
            insertRectIfContained(topRight, topLeft, bottomLeft, bottomRight, point = pt)
        }
        this.isDivided = true
    }

    // Possible failure if detail is large causing stackoverflow on recursion here...
    fun insertIfContained(point: ArrayPoint2d) {
        if (point !in bounds)
            return
        // Contained check already done, so insert is safe
        if (bounds.points.size < CAPACITY) {
            bounds.insert(point)
        } else {
            if (this.isNotDivided()) {
                this.subdivide()
            }
            insertIfContained(topRight, topLeft, bottomLeft, bottomRight, point = point)
        }
    }

    private fun isNotDivided(): Boolean = !isDivided

    override fun collapseData(): PointQuadTreeData {
        val ret = PointQuadTreeData()
        collapseDataRecursive(ret)
        return ret
    }

    private fun collapseDataRecursive(currData: PointQuadTreeData) {
        if (this.isNotDivided() && this.bounds.isNotEmpty()) {
            currData.rects.add(bounds)
            currData.points.addAll(this.bounds.points)
        } else {
            collapseIfNonNull(topRight, topLeft, bottomLeft, bottomRight, data = currData)
        }
    }

    companion object {
        const val CAPACITY = 4

        private fun createIfNonNull(rect: Rect?): PointQuadTree? =
            if (rect != null) PointQuadTree(PointQuadTreeRect(rect)) else null

        private fun insertRectIfContained(vararg quads: PointQuadTree?, point: ArrayPoint2d) {
            quads.forEach { quad -> quad?.bounds?.insertIfContained(point) }
        }

        private fun insertIfContained(vararg quads: PointQuadTree?, point: ArrayPoint2d) {
            quads.forEach { it?.insertIfContained(point) }
        }

        private fun collapseIfNonNull(vararg quads: PointQuadTree?, data: PointQuadTreeData) {
            quads.forEach { it?.collapseDataRecursive(data) }
        }
    }

}