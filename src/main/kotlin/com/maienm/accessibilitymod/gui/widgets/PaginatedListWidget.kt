package com.maienm.accessibilitymod.gui.widgets

import com.maienm.accessibilitymod.gui.helpers.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraftforge.fml.client.config.GuiButtonExt
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

/**
 * A widget that can show lists of items.
 *
 * Each of the input items is shown as a widget, which is created by calling the initializer with said item.
 * If there are more items than fit on a single page, you will be able to switch between pages of widgets.
 *
 * The widgets may be destroyed/recreated at any time, so make sure to store state in the items, not in the widgets!
 *
 * It attempts to fit as many rows and columns of widgets based on the minimum widget size and minimum spacing, up to the given maximums.
 * The grow mode variables control what is done with the leftover space.
 */
class PaginatedListWidget<T>(
	font: FontRenderer,
	items: List<T> = listOf(),
	minWidgetSize: Dimensions = Dimensions(1, 1),
	growColumns: GrowMode = GrowMode.STRETCH,
	growRows: GrowMode = GrowMode.NONE,
	minColumnSpacing: Int = 0,
	minRowSpacing: Int = 0,
	maxColumns: Int = Int.MAX_VALUE,
	maxRows: Int = Int.MAX_VALUE,
	paginationHeight: Int = 20,
	private val widgetCreator: (T) -> Widget
) : ContainerWidget(font) {
	var needsUpdate = true
	private inline fun <T> updateOnChange(initialValue: T, crossinline validate: (T) -> Boolean = { true }) =
		Delegates.vetoable(initialValue) { _, oldValue, newValue ->
			oldValue == newValue || validate(newValue).also {
				needsUpdate = true
			}
		}

	var items: List<T> by updateOnChange(items)
	var minWidgetSize by updateOnChange(Dimensions(1, 1)) { it.width > 0 && it.height > 0 }
	var growColumns by updateOnChange(growColumns)
	var growRows by updateOnChange(growRows)
	var minColumnSpacing by updateOnChange(0) { it >= 0 }
	var minRowSpacing by updateOnChange(0) { it >= 0 }
	var maxColumns by updateOnChange(Int.MAX_VALUE) { it > 0 }
	var maxRows by updateOnChange(Int.MAX_VALUE) { it > 0 }
	var paginationHeight by updateOnChange(20) { it > 1 }
	var page by updateOnChange(1) { it in 1..pages }

	private val pageSize: Int
		get() = rows * columns

	private val currentItems: List<T>
		get() = items.subList((page - 1) * pageSize, min(page * pageSize, items.size - 1))

	private val paginationContainer: ILayoutableWidget<ContainerWidget>
	private val paginationButtonPrev: ILayoutableWidget<GuiButtonExt>
	private val paginationText: ILayoutableWidget<TextWidget>
	private val paginationButtonNext: ILayoutableWidget<GuiButtonExt>

	private var rows = 1
	private var columns = 1
	private var rowSpacing = 0
	private var columnSpacing = 0
	private var widgetWidth = 1
	private var widgetHeight = 1
	private var pages = 0
	private var itemWidgetItems: List<T> = listOf()
	private var itemWidgets: List<Widget> = listOf()

	init {
		this.minWidgetSize = minWidgetSize
		this.minColumnSpacing = minColumnSpacing
		this.minRowSpacing = minRowSpacing
		this.maxColumns = maxColumns
		this.maxRows = maxRows
		this.paginationHeight = paginationHeight

		paginationContainer = layout(ContainerWidget(font))
		paginationButtonPrev = paginationContainer.widget.addButton("<<") { page -= 1 }.setX1(0.0).setX2(0.5, -25)
		paginationText = paginationContainer.widget.addText("0/0", TextWidget.Alignment.CENTER).centerX(50)
		paginationButtonNext = paginationContainer.widget.addButton(">>") { page += 1 }.setX1(0.5, 25).setX2(1.0)
	}

	override fun onResize(oldArea: Area, newArea: Area) {
		super.onResize(oldArea, newArea)
		needsUpdate = true
	}

	private fun updateWidgets() {
		rows = clamp((height - paginationHeight + minRowSpacing) / (minWidgetSize.height + minRowSpacing), 1, maxRows)
		columns = clamp(width / (minWidgetSize.width + minColumnSpacing), 1, maxColumns)

		widgetHeight = minWidgetSize.height
		rowSpacing = minRowSpacing
		val rowRemainder = height - paginationHeight - ((widgetHeight + rowSpacing) * rows)
		when (growRows) {
			GrowMode.STRETCH -> widgetHeight += rowRemainder / rows
			GrowMode.SPACING -> rowSpacing += rowRemainder / rows
		}

		widgetWidth = minWidgetSize.width
		columnSpacing = minColumnSpacing
		val columnRemainder = width - ((widgetWidth + columnSpacing) * columns) + columnSpacing
		when (growColumns) {
			GrowMode.STRETCH -> widgetWidth += columnRemainder / columns
			GrowMode.SPACING -> columnSpacing += columnRemainder / (columns - 1)
		}

		pages = ceil(items.size / pageSize.toFloat()).toInt()
		page = min(page, pages)

		widgets.removeAll(itemWidgets)
		itemWidgetItems = currentItems.toList()
		itemWidgets = itemWidgetItems.map(widgetCreator)
		widgets.addAll(itemWidgets)

		paginationButtonPrev.widget.active = page > 1
		paginationText.widget.message = "$page/$pages"

		needsUpdate = false
	}

	override fun renderBackground() {
		val minecraft = Minecraft.getInstance()
		renderBackground(minecraft, getArea())
		itemWidgets.forEach { renderBackground(minecraft, it.getArea(), 20) }
	}

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		if (needsUpdate || currentItems != itemWidgetItems) {
			updateWidgets()
		}

		itemWidgets.forEachIndexed { i, widget ->
			widget.setArea(
				Area(
					x + (i % columns) * (widgetWidth + columnSpacing),
					y + (i / columns) * (widgetHeight + rowSpacing),
					widgetWidth,
					widgetHeight
				)
			)
		}

		paginationContainer.setY1(-paginationHeight)
		paginationText.setY1((paginationHeight - font.FONT_HEIGHT) / 2)

		super.render(mouseX, mouseY, partialT)
	}

	enum class GrowMode {
		/** Don't fill up extra space with anything, just leave it at the end. */
		NONE,
		/** Stretch widgets to fill up extra space. */
		STRETCH,
		/** Increase spacing between widgets to fill up extra space. */
		SPACING
	}
}

private fun clamp(value: Int, minValue: Int, maxValue: Int) = min(max(value, minValue), maxValue)
