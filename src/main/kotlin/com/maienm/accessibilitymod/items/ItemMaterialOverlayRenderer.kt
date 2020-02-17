package com.maienm.accessibilitymod.items

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.helpers.clamp
import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting

object ItemMaterialOverlayRenderer {
	private val FONT: FontRenderer = Minecraft.getInstance().fontRenderer!!
	private val TEXTURE_MANAGER: TextureManager = Minecraft.getInstance().textureManager!!
	private val MAX_WIDTH = 16

	fun renderOverlay(stack: ItemStack, x: Int, y: Int) {
		if (!Config.ItemMaterialOverlay.enable) {
			return
		}

		val text = IItemMatcher.InstanceRegistry.getMaterials(stack)
			.map(::listOf)
			.mapNotNull<List<String>, String>(Config.ItemMaterialOverlay.materialNames::get)
			.firstOrNull()
			?: return

		GlStateManager.pushMatrix()
		GlStateManager.disableDepthTest()

		GlStateManager.translated(x.toDouble(), y.toDouble(), 0.0)

		val width = FONT.getStringWidth(text)
		val scale = clamp(
			MAX_WIDTH.toDouble() / width,
			Config.ItemMaterialOverlay.Text.minScale,
			Config.ItemMaterialOverlay.Text.maxScale
		)
		GlStateManager.scaled(scale, scale, scale)

		FONT.drawStringWithShadow(text, 0f, 0f, TextFormatting.WHITE.color!!)

		TEXTURE_MANAGER.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
		GlStateManager.enableDepthTest()
		GlStateManager.popMatrix()
	}
}
