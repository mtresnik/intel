package com.resnik.intel.quadtree.image

import java.awt.Color

data class RGB(val r: Double, val g: Double, val b: Double) {

    operator fun plus(other: RGB): RGB = RGB(this.r + other.r, this.g + other.g, this.b + other.b)

    operator fun div(other: Number): RGB =
        RGB(this.r / other.toDouble(), this.g / other.toDouble(), this.b / other.toDouble())

    operator fun minus(other: RGB): RGB = RGB(this.r - other.r, this.g - other.g, this.b - other.b)

    fun abs(): RGB = RGB(kotlin.math.abs(this.r), kotlin.math.abs(this.g), kotlin.math.abs(this.b))

    fun coerceIn(): RGB = RGB(this.r.coerceIn(0.0, 1.0), this.g.coerceIn(0.0, 1.0), this.b.coerceIn(0.0, 1.0))

    fun toArray(): DoubleArray = doubleArrayOf(this.r, this.g, this.b)

    fun sum(): Double = this.r + this.g + this.b

    fun toAwtColor(): Color {
        val coerced = this.abs().coerceIn()
        return Color(coerced.r.toFloat(), coerced.g.toFloat(), coerced.b.toFloat())
    }

}