package com.maienm.accessibilitymod.items

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11

object ItemMaterialOverlayRenderer {
	private val FONT: FontRenderer = Minecraft.getInstance().fontRenderer!!
	private val TEXTURE_MANAGER: TextureManager = Minecraft.getInstance().textureManager!!

	fun renderOverlay(stack: ItemStack, x: Int, y: Int) {
		if (!Config.ItemMaterialOverlay.enable) {
			return
		}

		val text = IItemMatcher.InstanceRegistry.getMaterials(stack)
			.map(::listOf)
			.mapNotNull<List<String>, String>(Config.ItemMaterialOverlay.materialNames::get)
			.firstOrNull()
			?: return

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		FONT.drawStringWithShadow(text, x.toFloat(), y.toFloat(), TextFormatting.WHITE.color!!)
		TEXTURE_MANAGER.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}