package com.resnik.intel.kd

class KDTreeNode<T>(var value: KDTreeValue<T>, var parent : KDTreeNode<T>? = null) {

    var left : KDTreeNode<T>? = null
    var right : KDTreeNode<T>? = null

    fun hasChildren() = this.left != null || this.right != null

}