package com.maienm.accessibilitymod.gui.helpers

data class Point(val x: Int, val y: Int)

data class Dimensions(val width: Int, val height: Int)

data class Area(val x: Int, val y: Int, val width: Int, val height: Int) {
	constructor(point: Point, dimensions: Dimensions) : this(point.x, point.y, dimensions.width, dimensions.height)
	constructor(x: Int, y: Int, dimensions: Dimensions) : this(x, y, dimensions.width, dimensions.height)
	constructor(point: Point, width: Int, height: Int) : this(point.x, point.y, width, height)

	val point by lazy { Point(x, y) }
	val dimensions by lazy { Dimensions(width, height) }
}
