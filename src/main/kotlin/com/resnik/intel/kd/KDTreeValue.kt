package com.resnik.intel.kd

import com.resnik.math.linear.array.ArrayPoint

data class KDTreeValue<T>(val point: ArrayPoint, val data: T)