package com.resnik.intel.quadtree

interface QuadTreeInterface<RECT : QuadTreeRect, DATA : QuadTreeData<RECT>> {

    fun collapseData(): DATA

}