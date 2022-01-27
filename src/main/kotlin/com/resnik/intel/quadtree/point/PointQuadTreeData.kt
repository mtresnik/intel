package com.resnik.intel.quadtree.point

import com.resnik.intel.quadtree.QuadTreeData
import com.resnik.math.linear.array.ArrayPoint2d

class PointQuadTreeData : QuadTreeData<PointQuadTreeRect>() {

    val points = mutableListOf<ArrayPoint2d>()

}