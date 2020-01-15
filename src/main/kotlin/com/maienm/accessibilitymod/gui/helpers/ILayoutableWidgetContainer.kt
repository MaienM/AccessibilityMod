package com.maienm.accessibilitymod.gui.helpers

import com.maienm.accessibilitymod.gui.widgets.TextWidget
import com.maienm.accessibilitymod.gui.widgets.TextWidget.Alignment
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.gui.widget.button.Button
import net.minecraftforge.fml.client.config.GuiButtonExt

/**
 * Interface for a container that can accept ILayoutable widgets.
 */
interface ILayoutableWidgetContainer {
	val font: FontRenderer
	val layoutableWidgets: MutableList<ILayoutableWidget<*>>

	fun getArea(): Area

	fun <T : Widget> add(widget: T): T
	fun <T : Widget> layout(widget: T): ILayoutableWidget<T> =
		ILayoutableWidget.of(this, add(widget)).also { layoutableWidgets.add(it); it.updateArea() }

	fun <T : Widget> remove(widget: T)
	fun <T : Widget> unlayout(widget: T) {
		layoutableWidgets.removeIf { it.widget == widget }
		remove(widget)
	}
	fun <T : Widget, L : ILayoutableWidget<T>> unlayout(layoutable: L) {
		layoutableWidgets.remove(layoutable)
		remove(layoutable.widget)
	}

	fun updateAreas() = layoutableWidgets.forEach(ILayoutableWidget<*>::updateArea)
}

fun <T : ILayoutableWidgetContainer> T.addButton(text: String, action: (GuiButtonExt) -> Unit) =
	layout(GuiButtonExt(0, 0, 0, 0, text, Button.IPressable { action(it as GuiButtonExt) }))

fun <T : ILayoutableWidgetContainer> T.addText(text: String, alignment: Alignment = Alignment.LEFT) =
	layout(TextWidget(font, text, alignment = alignment))
