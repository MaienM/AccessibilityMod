package com.maienm.accessibilitymod

import com.maienm.accessibilitymod.items.ItemMaterialOverlayRenderer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.item.ItemStack

/**
 * All methods that are invoked via coremods are in this class.
 */
object CoreModHooks {
	// Invoked at the end of ItemRenderer.renderItemModelIntoGUI
	fun onItemRendererRenderItemModelIntoGUI(itemRenderer: ItemRenderer, stack: ItemStack, x: Int, y: Int, model: IBakedModel) =
		ItemMaterialOverlayRenderer.onRenderItemModelIntoGUI(itemRenderer, stack, x, y, model)
}