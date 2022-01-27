package com.resnik.intel.quadtree

abstract class AbstractQuadTree<RECT : QuadTreeRect, DATA : QuadTreeData<RECT>, CHILD : QuadTreeInterface<RECT, DATA>>
    (protected val bounds : RECT)
    : QuadTreeInterface<RECT, DATA> {

    protected var topRight : CHILD? = null
    protected var topLeft : CHILD? = null
    protected var bottomLeft : CHILD? = null
    protected var bottomRight : CHILD? = null
    protected var isDivided : Boolean = false

}