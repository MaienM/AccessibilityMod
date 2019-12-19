package com.maienm.colorblindtools

import io.opencubes.boxlin.adapter.BoxlinContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

@Mod(ColorblindTools.ID)
object ColorblindTools {
	const val ID = "colorblindtools"

	init {
		BoxlinContext.get().eventBus.addListener { event: FMLCommonSetupEvent? -> setup(event) }
	}

	private fun setup(e: FMLCommonSetupEvent?) {
	}
}

