package com.resnik.intel.neural.conv

import com.resnik.intel.neural.TransferFunction
import com.resnik.math.linear.array.ArrayDimension

class ReLULayer(inputDims : ArrayDimension) : ActivationLayer(inputDims, TransferFunction.relu) {
}