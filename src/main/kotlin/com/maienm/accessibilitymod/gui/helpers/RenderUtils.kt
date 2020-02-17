package com.maienm.accessibilitymod.gui.helpers

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats

private fun render(minecraft: Minecraft, action: (Tessellator) -> Unit) {
	val tessellator = Tessellator.getInstance()

	minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION)
	GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)

	action(tessellator)
}

private fun renderRect(tessellator: Tessellator, x: Double, y: Double, width: Double, height: Double, shadeColor: Int) {
	val bufferbuilder = tessellator.buffer

	fun vertex(x: Double, y: Double) = bufferbuilder
		.pos(x, y, 0.0)
		.tex(x / 32.0f, y / 32.0f)
		.color(shadeColor, shadeColor, shadeColor, 255).endVertex()

	bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
	vertex(x, y + height)
	vertex(x + width, y + height)
	vertex(x + width, y)
	vertex(x, y)
	tessellator.draw()
}

fun renderBackground(minecraft: Minecraft, area: Area, shadeColor: Int = 32) =
	renderBackground(minecraft, area.x, area.y, area.width, area.height, shadeColor)

fun renderBackground(minecraft: Minecraft, x: Int, y: Int, width: Int, height: Int, shadeColor: Int = 32) =
	renderBackground(minecraft, x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), shadeColor)

fun renderBackground(minecraft: Minecraft, x: Double, y: Double, width: Double, height: Double, shadeColor: Int = 32) {
	render(minecraft) {
		renderRect(it, x, y, width, height, shadeColor)
	}
}

fun renderBorder(minecraft: Minecraft, area: Area, borderWidth: Double = 1.0, color: Int = 16) =
	renderBorder(minecraft, area.x, area.y, area.width, area.height, borderWidth, color)

fun renderBorder(
	minecraft: Minecraft,
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	borderWidth: Double = 1.0,
	color: Int = 16
) = renderBorder(minecraft, x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), borderWidth, color)

fun renderBorder(
	minecraft: Minecraft,
	x: Double,
	y: Double,
	width: Double,
	height: Double,
	borderWidth: Double = 1.0,
	color: Int = 16
) {
	render(minecraft) {
		renderRect(it, x, y, width, borderWidth, color)
		renderRect(it, x, y + height - borderWidth, width, borderWidth, color)
		renderRect(it, x, y, borderWidth, height, color)
		renderRect(it, x + width - borderWidth, y, borderWidth, height, color)
	}
}
