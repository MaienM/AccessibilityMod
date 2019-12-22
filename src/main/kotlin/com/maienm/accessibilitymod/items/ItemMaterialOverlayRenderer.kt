package com.maienm.accessibilitymod.items

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11

object ItemMaterialOverlayRenderer {
	private val FONT: FontRenderer = Minecraft.getInstance().fontRenderer!!

	fun onRenderItemModelIntoGUI(stack: ItemStack, x: Int, y: Int) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		if (stack.isItemEqual(Items.IRON_INGOT.defaultInstance)) {
			FONT.drawStringWithShadow("Fe", x.toFloat(), y.toFloat(), TextFormatting.WHITE.color!!)
		}
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}