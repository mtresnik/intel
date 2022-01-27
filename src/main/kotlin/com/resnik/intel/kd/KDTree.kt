package com.resnik.intel.kd

import com.resnik.math.linear.array.ArrayPoint

// TODO : Add kNN to this by finding closest node, going up tree to parent / nephews
class KDTree<T>(private val dim : Int) {

    var root : KDTreeNode<T>? = null

    operator fun plus(value : KDTreeValue<T>) = insert(value, root, 0);

    operator fun set(point: ArrayPoint, value: T) = plus(KDTreeValue(point, value))

    fun insert(value : KDTreeValue<T>, current : KDTreeNode<T>?, currDim : Int) : KDTreeNode<T> {
        if(root == null){
            root = KDTreeNode(value)
            return root!!;
        }
        if(current == null){
            return KDTreeNode(value)
        }
        // Find location same way as lookup.
        if(value.point[currDim] < current.value.point[currDim]){
            current.left = insert(value, current.left, (currDim + 1) % dim)
            current.left!!.parent = current
        }else{
            current.right = insert(value, current.right, (currDim + 1) % dim)
            current.right!!.parent = current
        }
        return current
    }

    operator fun get(point: ArrayPoint) : KDTreeNode<T>? = get(point, root, 0)

    fun get(point: ArrayPoint, current : KDTreeNode<T>?, currDim : Int) : KDTreeNode<T>? {
        if(current == null || root == null){
            return null
        }
        if(!root!!.hasChildren()){
            return root
        }
        var temp : KDTreeNode<T>? = null
        // Change dim comparing to at each step.
        if(point[currDim] < current.value.point[currDim]){
            // Keep going in a "less" direction
            temp = get(point, current.left, (currDim + 1) % dim)
            if (temp == null) {
                return if (current.left == null) {
                    current
                } else current.left
            }
            return temp
        } else {
            // Keep going in a "greater / equal" direction
            temp = get(point, current.right, (currDim + 1) % dim)
            if (temp == null) {
                return if (current.right == null) {
                    current
                } else current.right
            }
            return temp
        }
    }

}