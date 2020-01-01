package com.maienm.accessibilitymod.gui.widgets

import com.maienm.accessibilitymod.gui.helpers.*
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraftforge.fml.client.config.GuiButtonExt
import kotlin.math.ceil
import kotlin.math.min
import kotlin.properties.Delegates

/**
 * A widget that can show lists of items.
 *
 * Each of the input items is shown as a widget, which is created by calling the initializer with said item.
 * If there are more items than fit on a single page, you will be able to switch between pages of widgets.
 *
 * Widgets are destroyed when switching pages, so make sure to store state in the items, not in the widgets!
 */
class PaginatedListWidget<T>(
	font: FontRenderer,
	items: List<T> = listOf(),
	cols: Int = 1,
	rows: Int = 1,
	private val initializer: (T) -> Widget
) : ContainerWidget(font) {
	var needsUpdate = true
	private inline fun <T> updateOnChange(initialValue: T, crossinline validate: (T) -> Boolean) =
		Delegates.vetoable(initialValue) { _, oldValue, newValue ->
			validate(newValue).also {
				needsUpdate = needsUpdate || (oldValue != newValue)
			}
		}

	var rows by updateOnChange(rows) { it > 0 }
	var cols by updateOnChange(cols) { it > 0 }
	var page by updateOnChange(1) { it in 1..pages }
	var items: List<T> by updateOnChange(items) { true }
	var paginationHeight = 20
	var spacing = 0

	private val paginationContainer: ILayoutableWidget<ContainerWidget>
	private val paginationButtonPrev: ILayoutableWidget<GuiButtonExt>
	private val paginationText: ILayoutableWidget<TextWidget>
	private val paginationButtonNext: ILayoutableWidget<GuiButtonExt>

	private var pages = 0
	private var itemWidgets: List<Widget> = listOf()

	init {
		paginationContainer = layout(ContainerWidget(font)).centerX(0.6).setY1(-paginationHeight)
		paginationButtonPrev = paginationContainer.widget.addButton("<<") { page -= 1 }.setX1(0.0).setX2(0.5, -25)
		paginationText = paginationContainer.widget
			.addText("0/0", TextWidget.Alignment.CENTER)
			.centerX(50)
			.setY1((20 - font.FONT_HEIGHT) / 2)
		paginationButtonNext = paginationContainer.widget.addButton(">>") { page += 1 }.setX1(0.5, 25).setX2(1.0)
	}

	private fun updateWidgets() {
		val pageSize = rows * cols
		pages = ceil(items.size / pageSize.toFloat()).toInt()
		if (page > pages) {
			page = pages
		}
		widgets.removeAll(itemWidgets)
		itemWidgets = items.subList((page - 1) * pageSize, min(page * pageSize, items.size - 1)).map(initializer)
		widgets.addAll(itemWidgets)

		paginationButtonPrev.widget.active = page > 1
		paginationText.widget.message = "$page/$pages"
		paginationButtonNext.widget.active = page < pages

		needsUpdate = false
	}

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		if (needsUpdate) {
			updateWidgets()
		}

		val widgetWidth = (width - (cols - 1) * spacing) / cols
		val widgetHeight = (height - paginationHeight) / rows - spacing
		itemWidgets.forEachIndexed { i, widget ->
			widget.setPosition(
				Position(
					x + (i % cols) * (widgetWidth + spacing),
					y + (i / cols) * (widgetHeight + spacing),
					widgetWidth,
					widgetHeight
				)
			)
		}

		super.render(mouseX, mouseY, partialT)
	}
}
