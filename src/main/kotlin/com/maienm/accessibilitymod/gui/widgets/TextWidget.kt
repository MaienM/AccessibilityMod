package com.maienm.accessibilitymod.gui.widgets

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.Widget
import net.minecraft.util.text.TextFormatting
import kotlin.math.roundToInt

/**
 * A simple widget that displays text.
 */
class TextWidget(
	private val font: FontRenderer,
	message: String,
	var scale: Double = 1.0,
	var color: Int = TextFormatting.WHITE.color!!,
	var alignment: Alignment = Alignment.LEFT
) : Widget(0, 0, 0, 0, message) {
	enum class Alignment { LEFT, CENTER, RIGHT }

	private fun calculateLines(eachLine: (String, Double) -> Unit): Double {
		if (message.isEmpty()) {
			return 0.0
		}

		val height = font.FONT_HEIGHT * scale
		var yOffset = 0.0
		message.split("\n").forEach { paragraph ->
			font.listFormattedStringToWidth(paragraph, (width / scale).roundToInt()).forEach { line ->
				eachLine(line, yOffset)
				yOffset += LINE_SPACING * height
			}
			yOffset += (PARAGRAPH_SPACING - LINE_SPACING) * height
		}
		return yOffset - PARAGRAPH_SPACING * height + height
	}

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		GlStateManager.pushMatrix()
		GlStateManager.scaled(scale, scale, scale)
		this.height = calculateLines(::renderLine).roundToInt()
		GlStateManager.popMatrix()
	}

	private fun renderLine(line: String, yOffset: Double) {
		val xOffset = when (alignment) {
			Alignment.LEFT -> 0.0
			Alignment.CENTER -> (width - font.getStringWidth(line) * scale) / 2
			Alignment.RIGHT -> width - font.getStringWidth(line) * scale
		}
		font.drawStringWithShadow(line, ((x + xOffset) / scale).toFloat(), ((y + yOffset) / scale).toFloat(), color)
	}

	fun calculateHeight() = calculateLines { _, _ -> }.roundToInt().also { height = it }

	override fun setHeight(value: Int) {
		// Auto-determined, so don't actually set.
	}

	override fun clicked(p_clicked_1_: Double, p_clicked_3_: Double): Boolean = false

	companion object {
		const val PARAGRAPH_SPACING = 1.5f
		const val LINE_SPACING = 1f
	}
}
