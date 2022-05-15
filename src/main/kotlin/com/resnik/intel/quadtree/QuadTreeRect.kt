package com.resnik.intel.quadtree

import com.resnik.math.linear.array.geometry.Rect

abstract class QuadTreeRect(x: Double, y: Double, width: Double, height: Double) : Rect(x, y, width, height) {

    constructor(other: Rect) : this(other.x, other.y, other.width, other.height)

}