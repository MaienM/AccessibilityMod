package com.maienm.accessibilitymod

import com.maienm.accessibilitymod.gui.screens.ConfigScreen
import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import com.maienm.accessibilitymod.items.matchers.TagItemMatcher
import io.opencubes.boxlin.adapter.BoxlinContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.network.FMLNetworkConstants
import org.apache.commons.lang3.tuple.Pair
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Supplier

@Mod(AccessibilityMod.ID)
object AccessibilityMod {
	const val ID = "accessibilitymod"
	val DEBUG by lazy { System.getProperty("com.maienm.accessibilitymod.debug") != null }

	init {
		val modLoadingContext = ModLoadingContext.get()

		// Mark as not required on the other side.
		modLoadingContext.registerExtensionPoint(ExtensionPoint.DISPLAYTEST) {
			Pair.of(Supplier { FMLNetworkConstants.IGNORESERVERONLY }, BiPredicate { incoming, isNetwork -> true })
		}

		DistExecutor.runWhenOn(Dist.CLIENT) { Runnable(::initClient) }
	}

	private fun initClient() {
		IItemMatcher.TypeRegistry
			.register("tag", TagItemMatcher.FIELDS, TagItemMatcher.Companion::fromMap, TagItemMatcher.VALIDATOR)

		val modLoadingContext = ModLoadingContext.get()
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, Config.spec)
		modLoadingContext.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY) {
			BiFunction { minecraft: Minecraft, screen: Screen -> ConfigScreen(minecraft, screen) }
		}

		val boxlinContext = BoxlinContext.get()
		boxlinContext.addListener(::onConfigReload)
	}

	private fun onConfigReload(event: ModConfig.ModConfigEvent) {
		IItemMatcher.InstanceRegistry.reload()
	}
}

