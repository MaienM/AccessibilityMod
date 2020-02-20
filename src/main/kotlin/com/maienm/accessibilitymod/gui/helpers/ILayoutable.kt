package com.maienm.accessibilitymod.gui.helpers

import net.minecraft.client.gui.widget.Widget

/**
 * Interface for a UI element that has flexible positioning rules.
 *
 * Unlike most of the positioning, this is done with two points for x and y, rather than one point and a size.
 */
interface ILayoutable {
	fun doSetX1(calculator: IFirstPositionCalculator)
	fun doSetX2(calculator: ISecondPositionCalculator)
	fun doSetY1(calculator: IFirstPositionCalculator)
	fun doSetY2(calculator: ISecondPositionCalculator)
}

enum class XEdge { LEFT, RIGHT }
enum class YEdge { TOP, BOTTOM }

fun <T : ILayoutable> T.setX1(calculator: IFirstPositionCalculator) = apply { doSetX1(calculator) }
fun <T : ILayoutable> T.setX1(value: Int) = setX1(AbsoluteFPC(value))
fun <T : ILayoutable> T.setX1(fraction: Double, offset: Int = 0) = setX1(FractionFPC(fraction, offset))
fun <T : ILayoutable> T.setX1(calculation: (Int, Int) -> Int) = setX1(CustomFPC(calculation))
fun <T : ILayoutable> T.setX1(calculation: (Int) -> Int) = setX1 { dim, _ -> calculation(dim) }
fun <T : ILayoutable> T.setX1(widget: Widget, edge: XEdge = XEdge.RIGHT, offset: Int = 0, offsetWhenHidden: Int = 0) =
	setX1(RelativeXFPC(widget, edge, offset, offsetWhenHidden))

fun <T : ILayoutable> T.setX2(calculator: ISecondPositionCalculator) = apply { doSetX2(calculator) }
fun <T : ILayoutable> T.setX2(calculator: IFirstPositionCalculator) = setX2(FPCAsSPC(calculator))
fun <T : ILayoutable> T.setX2(value: Int) = setX2(AbsoluteFPC(value))
fun <T : ILayoutable> T.setX2(fraction: Double, offset: Int = 0) = setX2(FractionFPC(fraction, offset))
fun <T : ILayoutable> T.setX2(calculation: (Int, Int, Int) -> Int) = setX2(CustomSPC(calculation))
fun <T : ILayoutable> T.setX2(calculation: (Int, Int) -> Int) = setX2 { dim, pOffset, _ -> calculation(dim, pOffset) }
fun <T : ILayoutable> T.setX2(calculation: (Int) -> Int) = setX2 { dim, _, _ -> calculation(dim) }
fun <T : ILayoutable> T.setX2(widget: Widget, edge: XEdge = XEdge.RIGHT, offset: Int = 0, offsetWhenHidden: Int = 0) =
	setX2(RelativeXFPC(widget, edge, offset, offsetWhenHidden))

fun <T : ILayoutable> T.setWidth(value: Int) = setX2(AbsoluteSizeSPC(value))
fun <T : ILayoutable> T.setWidth(fraction: Double, offset: Int = 0) = setX2(FractionSizeSPC(fraction, offset))

fun <T : ILayoutable> T.setY1(calculator: IFirstPositionCalculator) = apply { doSetY1(calculator) }
fun <T : ILayoutable> T.setY1(value: Int) = setY1(AbsoluteFPC(value))
fun <T : ILayoutable> T.setY1(fraction: Double, offset: Int = 0) = setY1(FractionFPC(fraction, offset))
fun <T : ILayoutable> T.setY1(calculation: (Int, Int) -> Int) = setY1(CustomFPC(calculation))
fun <T : ILayoutable> T.setY1(calculation: (Int) -> Int) = setY1 { dim, _ -> calculation(dim) }
fun <T : ILayoutable> T.setY1(widget: Widget, edge: YEdge = YEdge.BOTTOM, offset: Int = 0) =
	setY1(RelativeYFPC(widget, edge, offset))

fun <T : ILayoutable> T.setY2(calculator: ISecondPositionCalculator) = apply { doSetY2(calculator) }
fun <T : ILayoutable> T.setY2(calculator: IFirstPositionCalculator) = setY2(FPCAsSPC(calculator))
fun <T : ILayoutable> T.setY2(value: Int) = setY2(AbsoluteFPC(value))
fun <T : ILayoutable> T.setY2(fraction: Double, offset: Int = 0) = setY2(FractionFPC(fraction, offset))
fun <T : ILayoutable> T.setY2(calculation: (Int, Int, Int) -> Int) = setY2(CustomSPC(calculation))
fun <T : ILayoutable> T.setY2(calculation: (Int, Int) -> Int) = setY2 { dim, pOffset, _ -> calculation(dim, pOffset) }
fun <T : ILayoutable> T.setY2(calculation: (Int) -> Int) = setY2 { dim, _, _ -> calculation(dim) }
fun <T : ILayoutable> T.setY2(widget: Widget, edge: YEdge = YEdge.BOTTOM, offset: Int = 0) =
	setY2(RelativeYFPC(widget, edge, offset))

fun <T : ILayoutable> T.setHeight(value: Int) = setY2(AbsoluteSizeSPC(value))
fun <T : ILayoutable> T.setHeight(fraction: Double, offset: Int = 0) = setY2(FractionSizeSPC(fraction, offset))

fun <T : ILayoutable> T.setX(x1: Int, x2: Int) = setX1(x1).setX2(x2)
fun <T : ILayoutable> T.setX(x1: Double, x2: Double) = setX1(x1).setX2(x2)
fun <T : ILayoutable> T.setY(y1: Int, y2: Int) = setY1(y1).setY2(y2)
fun <T : ILayoutable> T.setY(y1: Double, y2: Double) = setY1(y1).setY2(y2)

fun <T : ILayoutable> T.centerX(width: Int) = setX1(0.5, -width / 2).setX2(0.5, width / 2)
fun <T : ILayoutable> T.centerX(width: Double) = setX1((1 - width) / 2).setX2((width - 1) / 2)

fun <T : ILayoutable> T.centerY(height: Int) = setY1(0.5, -height / 2).setY2(0.5, height / 2)
fun <T : ILayoutable> T.centerY(height: Double) = setY1((1 - height) / 2).setY2((height - 1) / 2)

/**
 * Base implementation of IPositionable.
 */
class Layoutable(
	private var x1: IFirstPositionCalculator = FractionFPC(0.0),
	private var x2: ISecondPositionCalculator = FPCAsSPC(FractionFPC(1.0)),
	private var y1: IFirstPositionCalculator = FractionFPC(0.0),
	private var y2: ISecondPositionCalculator = FPCAsSPC(FractionFPC(1.0))
) : ILayoutable {
	override fun doSetX1(calculator: IFirstPositionCalculator) {
		x1 = calculator
	}

	override fun doSetX2(calculator: ISecondPositionCalculator) {
		x2 = calculator
	}

	override fun doSetY1(calculator: IFirstPositionCalculator) {
		y1 = calculator
	}

	override fun doSetY2(calculator: ISecondPositionCalculator) {
		y2 = calculator
	}

	fun toArea(parentArea: Area): Area {
		val x1 = x1.calculate(parentArea.width, parentArea.x)
		val x2 = x2.calculate(parentArea.width, parentArea.x, x1)
		val y1 = y1.calculate(parentArea.height, parentArea.y)
		val y2 = y2.calculate(parentArea.height, parentArea.y, y1)
		return Area(parentArea.x + x1, parentArea.y + y1, x2 - x1, y2 - y1)
	}
}

private fun wrap(value: Int, dimension: Int) = if (value < 0) value + dimension else value

// Calculation of x1 and y1.

interface IFirstPositionCalculator {
	fun calculate(dimension: Int, parentOffset: Int): Int
}

private class AbsoluteFPC(val value: Int) : IFirstPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int): Int = wrap(value, dimension)
}

private class FractionFPC(val fraction: Double, val offset: Int = 0) : IFirstPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int): Int =
		wrap((fraction * dimension + offset).toInt(), dimension)
}

private fun Widget.hidden() = width == 0 || height == 0

private class RelativeXFPC(
	val widget: Widget,
	val edge: XEdge,
	val offset: Int = 0,
	val offsetWhenHidden: Int
) : IFirstPositionCalculator {
	private val currentOffset: Int
		get() = if (widget.hidden()) offsetWhenHidden else offset
	override fun calculate(dimension: Int, parentOffset: Int): Int =
		widget.x - parentOffset + (if (edge == XEdge.RIGHT) widget.width else 0) + currentOffset
}

private class RelativeYFPC(
	val widget: Widget,
	val edge: YEdge,
	val offset: Int = 0,
	val offsetWhenHidden: Int = 0
) : IFirstPositionCalculator {
	private val currentOffset: Int
		get() = if (widget.hidden()) offsetWhenHidden else offset
	override fun calculate(dimension: Int, parentOffset: Int): Int =
		widget.y - parentOffset + (if (edge == YEdge.BOTTOM) widget.height else 0) + currentOffset
}

private class CustomFPC(val calculation: (Int, Int) -> Int) : IFirstPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int): Int = calculation(dimension, parentOffset)
}

// Calculation of x2 and y2

interface ISecondPositionCalculator {
	fun calculate(dimension: Int, parentOffset: Int, firstPosition: Int): Int
}

private class FPCAsSPC(val fpc: IFirstPositionCalculator) : ISecondPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int, firstPosition: Int): Int =
		fpc.calculate(dimension, parentOffset)
}

private class CustomSPC(val calculation: (Int, Int, Int) -> Int) : ISecondPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int, firstPosition: Int): Int =
		calculation(dimension, parentOffset, firstPosition)
}

private class AbsoluteSizeSPC(val size: Int) : ISecondPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int, firstPosition: Int): Int = firstPosition + size
}

private class FractionSizeSPC(val fraction: Double, val offset: Int = 0) : ISecondPositionCalculator {
	override fun calculate(dimension: Int, parentOffset: Int, firstPosition: Int): Int =
		firstPosition + (fraction * dimension).toInt() + offset
}
