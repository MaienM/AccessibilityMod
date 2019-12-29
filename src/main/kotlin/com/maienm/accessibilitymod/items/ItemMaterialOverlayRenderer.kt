package com.maienm.accessibilitymod.items

import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11

object ItemMaterialOverlayRenderer {
	private val FONT: FontRenderer = Minecraft.getInstance().fontRenderer!!

	fun onRenderItemModelIntoGUI(stack: ItemStack, x: Int, y: Int) {
		val material = IItemMatcher.InstanceRegistry.getMaterials(stack).firstOrNull() ?: return

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		FONT.drawStringWithShadow(material, x.toFloat(), y.toFloat(), TextFormatting.WHITE.color!!)
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}