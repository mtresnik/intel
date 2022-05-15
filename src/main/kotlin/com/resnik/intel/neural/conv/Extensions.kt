package com.resnik.intel.neural.conv

import com.resnik.intel.neural.TransferFunction
import com.resnik.math.linear.array.*
import java.awt.Color
import java.awt.image.BufferedImage

fun BufferedImage.split(): Triple<ArrayMatrix, ArrayMatrix, ArrayMatrix> {
    val rMatrix = ArrayMatrix(this.height, this.width)
    val gMatrix = ArrayMatrix(this.height, this.width)
    val bMatrix = ArrayMatrix(this.height, this.width)
    repeat(this.height) { row ->
        repeat(this.width) { col ->
            val color = Color(this.getRGB(col, row))
            rMatrix[row][col] = color.red / 255.0
            gMatrix[row][col] = color.green / 255.0
            bMatrix[row][col] = color.blue / 255.0
        }
    }
    return Triple(rMatrix, gMatrix, bMatrix)
}

fun BufferedImage.toGreyScaleMatrix(): ArrayMatrix {
    val ret = ArrayMatrix(this.height, this.width)
    repeat(this.height) { row ->
        repeat(this.width) { col ->
            val color = Color(this.getRGB(col, row))
            ret[row][col] = (color.red + color.green + color.blue) / (255.0 + 3.0)
        }
    }
    return ret
}

fun BufferedImage.applyGreyScaleFilter(filter: ArrayMatrix): BufferedImage =
    this.toGreyScaleMatrix().convolve(filter).toGreyscaleImage()

fun ArrayMatrix.toGreyscaleImage(): BufferedImage {
    val normalized = this.relu()
    val ret = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB)
    repeat(this.height) { row ->
        repeat(this.width) { col ->
            val maxVal = normalized[row][col].toFloat().coerceAtMost(1.0f)
            ret.setRGB(col, row, Color(maxVal, maxVal, maxVal).rgb)
        }
    }
    return ret
}

fun ArrayMatrix.applyFilter(other: ArrayMatrix): ArrayMatrix {
    val ret = ArrayMatrix(this.height - other.height + 1, this.width - other.width + 1)
    val centerRow = other.height / 2
    val centerCol = other.width / 2
    repeat(ret.height) { row ->
        repeat(ret.width) { col ->
            var sum = 0.0
            repeat(other.height) { row1 ->
                val expectedRow = row + row1 - centerRow
                if (expectedRow < this.height && expectedRow >= 0) {
                    repeat(other.width) { col1 ->
                        val expectedCol = col + col1 - centerCol
                        if (col + col1 < this.width && expectedCol >= 0) {
                            sum += this[expectedRow][expectedCol] * other[row1][col1]
                        }
                    }
                }
            }
            ret[row][col] = sum / other.numElements()
        }
    }
    return ret
}

fun ArrayMatrix.relu(): ArrayMatrix = this.apply { TransferFunction.relu.activate(it) }

fun ArrayMatrix.convolve(filter: ArrayMatrix): ArrayMatrix = this.applyFilter(filter).relu()

fun ArrayMatrix.pool(poolSize: Int = 2): ArrayMatrix {
    val ret = ArrayMatrix(this.height / poolSize, this.width / poolSize)
    repeat(ret.height) { row ->
        repeat(ret.width) { col ->
            var max = 0.0
            repeat(poolSize) { row1 ->
                if (row * poolSize + row1 < this.height) {
                    repeat(poolSize) { col1 ->
                        if (col * poolSize + col1 < this.width) {
                            max = Math.max(this[row * poolSize + row1][col * poolSize + col1], max)
                        }
                    }
                }
            }
            ret[row][col] = max
        }
    }
    return ret
}

fun ArrayMatrix.flatten(): ArrayVector {
    val ret = ArrayVector(this.height * this.width)
    var i = 0
    repeat(this.height) { row ->
        repeat(this.width) { col ->
            ret[i] = this[row][col]
            i++
        }
    }
    return ret
}

fun BufferedImage.toTensor(): ArrayTensor {
    val retTensor = ArrayTensor(ArrayDimension(this.height, this.width, 3))
    repeat(this.height) { row ->
        repeat(this.width) { col ->
            val color = Color(this.getRGB(col, row))
            retTensor[ArrayTensorIndex(row, col, 0)] = color.red / 255.0
            retTensor[ArrayTensorIndex(row, col, 1)] = color.green / 255.0
            retTensor[ArrayTensorIndex(row, col, 2)] = color.blue / 255.0
        }
    }
    return retTensor
}

