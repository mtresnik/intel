package com.resnik.intel.quadtree.point

import com.resnik.intel.quadtree.QuadTreeRect
import com.resnik.math.linear.array.ArrayPoint2d
import com.resnik.math.linear.array.geometry.Rect

class PointQuadTreeRect(x: Double, y: Double, width: Double, height: Double) : QuadTreeRect(x, y, width, height) {

    constructor(other: Rect) : this(other.x, other.y, other.width, other.height)

    val points = mutableSetOf<ArrayPoint2d>()

    fun insertIfContained(point: ArrayPoint2d): Boolean {
        if (point !in this)
            return false
        return points.add(point)
    }

    fun insert(point: ArrayPoint2d): Boolean {
        return points.add(point)
    }

    fun isEmpty(): Boolean = this.points.isEmpty()

    fun isNotEmpty(): Boolean = !this.isEmpty()


}