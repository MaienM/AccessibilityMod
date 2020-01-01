package com.maienm.accessibilitymod

import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import com.maienm.accessibilitymod.items.matchers.TagItemMatcher
import io.opencubes.boxlin.adapter.BoxlinContext
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig


@Mod(AccessibilityMod.ID)
object AccessibilityMod {
	const val ID = "accessibilitymod"
	val DEBUG by lazy { System.getProperty("com.maienm.accessibilitymod.debug") != null }

	init {
		IItemMatcher.TypeRegistry.register("tag", TagItemMatcher.Companion::fromMap)

		val modLoadingContext = ModLoadingContext.get()
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, Config.spec)

		BoxlinContext.get().eventBus.addListener { event: ModConfig.ModConfigEvent -> reload(event) }
	}

	private fun reload(event: ModConfig.ModConfigEvent) {
		IItemMatcher.InstanceRegistry.reload()
	}
}

