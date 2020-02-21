package com.maienm.accessibilitymod.gui.helpers

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats

private fun render(minecraft: Minecraft, action: (Tessellator) -> Unit) {
	val tessellator = Tessellator.getInstance()

	GlStateManager.disableTexture()
	GlStateManager.enableBlend()
	GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
	action(tessellator)
	GlStateManager.enableTexture()
}

private fun renderRect(tessellator: Tessellator, x: Double, y: Double, width: Double, height: Double, opacity: Double) {
	val bufferbuilder = tessellator.buffer

	fun vertex(x: Double, y: Double) = bufferbuilder
		.pos(x, y, 0.0)
		.tex(x / 32.0f, y / 32.0f)
		.color(0, 0, 0, (opacity * 255).toInt()).endVertex()

	bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
	vertex(x, y + height)
	vertex(x + width, y + height)
	vertex(x + width, y)
	vertex(x, y)
	tessellator.draw()
}

fun renderBackground(minecraft: Minecraft, area: Area, opacity: Double = 0.3) =
	renderBackground(minecraft, area.x, area.y, area.width, area.height, opacity)

fun renderBackground(minecraft: Minecraft, x: Int, y: Int, width: Int, height: Int, opacity: Double = 0.3) =
	renderBackground(minecraft, x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), opacity)

fun renderBackground(minecraft: Minecraft, x: Double, y: Double, width: Double, height: Double, opacity: Double = 0.3) {
	render(minecraft) {
		renderRect(it, x, y, width, height, opacity)
	}
}

fun renderBorder(minecraft: Minecraft, area: Area, borderWidth: Double = 1.0, opacity: Double = 1.0) =
	renderBorder(minecraft, area.x, area.y, area.width, area.height, borderWidth, opacity)

fun renderBorder(
	minecraft: Minecraft,
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	borderWidth: Double = 1.0,
	opacity: Double = 1.0
) = renderBorder(minecraft, x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), borderWidth, opacity)

fun renderBorder(
	minecraft: Minecraft,
	x: Double,
	y: Double,
	width: Double,
	height: Double,
	borderWidth: Double = 1.0,
	opacity: Double = 1.0
) {
	render(minecraft) {
		renderRect(it, x, y, width, borderWidth, opacity)
		renderRect(it, x, y + height - borderWidth, width, borderWidth, opacity)
		renderRect(it, x, y, borderWidth, height, opacity)
		renderRect(it, x + width - borderWidth, y, borderWidth, height, opacity)
	}
}
