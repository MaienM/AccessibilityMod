package com.maienm.accessibilitymod.items

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11

object ItemMaterialOverlayRenderer {
	private val FONT: FontRenderer = Minecraft.getInstance().fontRenderer!!

	fun onRenderItemModelIntoGUI(stack: ItemStack, x: Int, y: Int) {
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
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}