package com.resnik.intel.quadtree.image

import com.resnik.intel.quadtree.QuadTreeRect
import com.resnik.math.linear.array.geometry.Rect

class ImageQuadTreeRect(x: Double, y: Double, width: Double, height: Double) : QuadTreeRect(x, y, width, height) {

    var averageColor: RGB? = null

    constructor(other: Rect) : this(other.x, other.y, other.width, other.height)

}