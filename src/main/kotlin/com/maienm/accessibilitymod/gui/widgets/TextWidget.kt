package com.maienm.accessibilitymod.gui.widgets

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraft.util.text.TextFormatting

/**
 * A simple widget that displays text.
 */
class TextWidget(
	private val font: FontRenderer,
	message: String,
	var color: Int = TextFormatting.WHITE.color!!,
	var alignment: Alignment = Alignment.LEFT
) : Widget(0, 0, 0, 0, message) {
	enum class Alignment { LEFT, CENTER, RIGHT }

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		val height = font.getWordWrappedHeight(message, Int.MAX_VALUE).toFloat()
		var yOffset = 0f
		message.split("\n").forEach { paragraph ->
			font.listFormattedStringToWidth(paragraph, width).forEach { line ->
				renderLine(line, yOffset)
				yOffset += LINE_SPACING * height
			}
			yOffset += (PARAGRAPH_SPACING - LINE_SPACING) * height
		}
	}

	private fun renderLine(line: String, yOffset: Float) {
		val xOffset: Float = when (alignment) {
			Alignment.LEFT -> 0f
			Alignment.CENTER -> (width - font.getStringWidth(line)) / 2f
			Alignment.RIGHT -> (width - font.getStringWidth(line)).toFloat()
		}
		font.drawStringWithShadow(line, x + xOffset, y + yOffset, color)
	}

	override fun clicked(p_clicked_1_: Double, p_clicked_3_: Double): Boolean = false

	companion object {
		const val PARAGRAPH_SPACING = 0.9f
		const val LINE_SPACING = 0.5f
	}
}