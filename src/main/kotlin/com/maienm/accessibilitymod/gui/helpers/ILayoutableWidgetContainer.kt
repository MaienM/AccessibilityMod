package com.maienm.accessibilitymod.gui.helpers

import com.maienm.accessibilitymod.gui.widgets.PaginatedListWidget
import com.maienm.accessibilitymod.gui.widgets.TextWidget
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

	fun getPosition(): Position

	fun <T : Widget> add(widget: T): T
	fun <T : ILayoutableWidget<*>> add(wrapped: T): T = wrapped.also { layoutableWidgets.add(it) }
	fun <T : Widget> layout(widget: T): ILayoutableWidget<T> =
		ILayoutableWidget.of(this, add(widget)).also { layoutableWidgets.add(it) }

	fun updatePositions() = layoutableWidgets.forEach(ILayoutableWidget<*>::updatePosition)
}

fun <T : ILayoutableWidgetContainer> T.addButton(text: String, action: (GuiButtonExt) -> Unit) =
	layout(GuiButtonExt(0, 0, 0, 0, text, Button.IPressable { action(it as GuiButtonExt) }))

fun <T : ILayoutableWidgetContainer> T.addText(
	text: String,
	alignment: TextWidget.Alignment = TextWidget.Alignment.LEFT
) = layout(TextWidget(font, text, alignment = alignment))

fun <T : ILayoutableWidgetContainer, I> T.addList(items: List<I>, initializer: (I) -> Widget) =
	layout(PaginatedListWidget(font, items, initializer = initializer))
