package com.maienm.accessibilitymod

import com.maienm.accessibilitymod.gui.screens.ConfigScreen
import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import com.maienm.accessibilitymod.items.matchers.TagItemMatcher
import io.opencubes.boxlin.adapter.BoxlinContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import java.util.function.BiFunction

@Mod(AccessibilityMod.ID)
object AccessibilityMod {
	const val ID = "accessibilitymod"
	val DEBUG by lazy { System.getProperty("com.maienm.accessibilitymod.debug") != null }

	init {
		IItemMatcher.TypeRegistry
			.register("tag", TagItemMatcher.FIELDS, TagItemMatcher.Companion::fromMap, TagItemMatcher.VALIDATOR)

		val modLoadingContext = ModLoadingContext.get()
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, Config.spec)
		modLoadingContext.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY) { ->
			BiFunction { minecraft: Minecraft, screen: Screen -> ConfigScreen(minecraft, screen) }
		}

		BoxlinContext.get().eventBus.addListener { event: ModConfig.ModConfigEvent -> reload(event) }
	}

	private fun reload(event: ModConfig.ModConfigEvent) {
		IItemMatcher.InstanceRegistry.reload()
	}
}

