package com.maienm.accessibilitymod.gui.widgets

import com.maienm.accessibilitymod.gui.helpers.Area
import com.maienm.accessibilitymod.gui.helpers.ILayoutableWidget
import com.maienm.accessibilitymod.gui.helpers.ILayoutableWidgetContainer
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.IGuiEventListener
import net.minecraft.client.gui.INestedGuiEventHandler
import net.minecraft.client.gui.widget.Widget

/**
 * A simple widget that can act as a container for ILayoutable widgets.
 */
open class ContainerWidget(override val font: FontRenderer) :
	Widget(0, 0, 0, 0, ""),
	INestedGuiEventHandler,
	ILayoutableWidgetContainer {

	private var oldArea = Area(-1, -1, -1, -1)

	protected val widgets: MutableList<Widget> = mutableListOf()
	override val layoutableWidgets: MutableList<ILayoutableWidget<*>> = mutableListOf()

	override fun <T : Widget> add(widget: T): T = widget.also { widgets.add(it) }

	override fun getArea(): Area = Area(x, y, width, height)

	open fun renderBackground() {}

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		val area = getArea()
		if (area != oldArea) {
			oldArea = area
			onResize(oldArea, area)
		}
		renderBackground()
		widgets.forEach { it.render(mouseX, mouseY, partialT) }
	}

	open fun onResize(oldArea: Area, newArea: Area) {
		updateAreas()
	}

	override fun children(): MutableList<out IGuiEventListener> = widgets

	// Implementation of methods of INestedGuiHandler. Identical to the one found in FocusableGui.

	private var focused: IGuiEventListener? = null
	private var isDragging = false

	override fun isDragging(): Boolean = isDragging
	override fun setDragging(p_setDragging_1_: Boolean) {
		isDragging = p_setDragging_1_
	}

	override fun getFocused(): IGuiEventListener? = focused
	override fun setFocused(p_setFocused_1_: IGuiEventListener?) {
		this.focused = p_setFocused_1_
	}

	// Methods are implemented in both Widget and INestedGuiHandler. We use the implementations of the latter.

	override fun mouseClicked(p_mouseClicked_1_: Double, p_mouseClicked_3_: Double, p_mouseClicked_5_: Int): Boolean =
		super<INestedGuiEventHandler>.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)

	override fun mouseReleased(
		p_mouseReleased_1_: Double,
		p_mouseReleased_3_: Double,
		p_mouseReleased_5_: Int
	): Boolean = super<INestedGuiEventHandler>.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_)

	override fun mouseDragged(
		p_mouseDragged_1_: Double,
		p_mouseDragged_3_: Double,
		p_mouseDragged_5_: Int,
		p_mouseDragged_6_: Double,
		p_mouseDragged_8_: Double
	): Boolean = super<INestedGuiEventHandler>.mouseDragged(
		p_mouseDragged_1_,
		p_mouseDragged_3_,
		p_mouseDragged_5_,
		p_mouseDragged_6_,
		p_mouseDragged_8_
	)

	override fun changeFocus(p_changeFocus_1_: Boolean): Boolean =
		super<INestedGuiEventHandler>.changeFocus(p_changeFocus_1_)
}