package com.maienm.accessibilitymod.gui.helpers

import net.minecraft.client.gui.widget.Widget

/**
 * Interface for wrapper classes around widgets to make these widgets ILayoutable.
 */
interface ILayoutableWidget<T> : ILayoutable {
	val widget: T
	val container: ILayoutableWidgetContainer
	val positionable: Layoutable

	fun setArea(area: Area)

	fun updateArea() = this.setArea(positionable.toArea(container.getArea()))

	override fun doSetX1(calculator: IFirstPositionCalculator) = positionable.doSetX1(calculator)
	override fun doSetX2(calculator: ISecondPositionCalculator) = positionable.doSetX2(calculator)
	override fun doSetY1(calculator: IFirstPositionCalculator) = positionable.doSetY1(calculator)
	override fun doSetY2(calculator: ISecondPositionCalculator) = positionable.doSetY2(calculator)

	companion object {
		fun <T : Widget> of(container: ILayoutableWidgetContainer, widget: T): ILayoutableWidget<T> =
			LayoutableWidget<T>(container, widget)
	}
}

private class LayoutableWidget<T : Widget>(
	override val container: ILayoutableWidgetContainer,
	override val widget: T
) : ILayoutableWidget<T> {
	override val positionable = Layoutable()

	override fun setArea(area: Area) = widget.setArea(area)
}
