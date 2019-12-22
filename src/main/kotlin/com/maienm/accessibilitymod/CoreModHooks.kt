package com.maienm.accessibilitymod

import com.maienm.accessibilitymod.items.ItemMaterialOverlayRenderer
import net.minecraft.item.ItemStack

/**
 * All methods that are invoked via coremods are in this class.
 */
object CoreModHooks {
	// Invoked at the end of ItemRenderer.renderItemModelIntoGUI
	fun onItemRendererRenderItemModelIntoGUI(stack: ItemStack, x: Int, y: Int) =
		ItemMaterialOverlayRenderer.onRenderItemModelIntoGUI(stack, x, y)
}