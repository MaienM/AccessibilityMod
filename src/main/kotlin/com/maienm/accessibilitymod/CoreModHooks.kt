package com.maienm.accessibilitymod

import com.maienm.accessibilitymod.items.ItemMaterialOverlayRenderer
import net.minecraft.client.renderer.Rectangle2d
import net.minecraft.item.ItemStack

/**
 * All methods that are invoked via coremods are in this class.
 */
object CoreModHooks {
	// Invoked at the end of ItemRenderer.renderItemModelIntoGUI
	fun onItemRendererRenderItemModelIntoGUI(stack: ItemStack, x: Int, y: Int) =
		ItemMaterialOverlayRenderer.renderOverlay(stack, x, y)

	// Invoked at the end of ItemStackFastRenderer.uncheckedRenderItemAndEffectIntoGUI (in JEI).
	fun onItemStackFastRendererUncheckedRenderItemAndEffectIntoGUI(stack: ItemStack, area: Rectangle2d) =
		ItemMaterialOverlayRenderer.renderOverlay(stack, area.x, area.y)
}
