package com.maienm.accessibilitymod.helpers

fun clamp(value: Int, min: Int, max: Int) = if (value < min) min else if (value > max) max else value
fun clamp(value: Double, min: Double, max: Double) = if (value < min) min else if (value > max) max else value
fun clamp(value: Float, min: Float, max: Float) = if (value < min) min else if (value > max) max else value
