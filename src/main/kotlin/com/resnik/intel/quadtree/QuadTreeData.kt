package com.resnik.intel.quadtree

abstract class QuadTreeData<RECT : QuadTreeRect> {
    val rects = mutableListOf<RECT>()
}