package com.maienm.colorblindtools

import io.opencubes.boxlin.adapter.BoxlinContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.lwjgl.opengl.GL11

@Mod(ColorblindTools.ID)
object ColorblindTools {
	const val ID = "colorblindtools"
	private lateinit var FONT: FontRenderer

	init {
		BoxlinContext.get().eventBus.addListener { event: FMLCommonSetupEvent? -> setup(event) }
	}

	private fun setup(e: FMLCommonSetupEvent?) {
		this.FONT = Minecraft.getInstance().fontRenderer!!
	}

	// Invoked via coremod at the end of ItemRenderer.renderItemModelIntoGUI
	fun onRenderItemModelIntoGUI(itemRenderer: ItemRenderer, stack: ItemStack, x: Int, y: Int, model: IBakedModel) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		if (stack.isItemEqual(Items.IRON_INGOT.defaultInstance)) {
			FONT.drawStringWithShadow("Fe", x.toFloat(), y.toFloat(), TextFormatting.WHITE.color!!)
		}
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}

