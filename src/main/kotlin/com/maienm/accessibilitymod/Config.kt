package com.maienm.accessibilitymod

import com.electronwill.nightconfig.toml.TomlFormat
import io.opencubes.boxlin.getValue
import io.opencubes.boxlin.setValue
import net.minecraftforge.common.ForgeConfigSpec
import com.electronwill.nightconfig.core.Config as NCConfig

object Config {
	private val builder = ForgeConfigSpec.Builder()

	val spec: ForgeConfigSpec by lazy {
		ItemMaterialOverlay
		builder.build()
	}

	object ItemMaterialOverlay {
		init {
			builder
				.comment("Render text indicating the material of an item over said item in inventories.")
				.push("itemMaterialOverlay")
		}

		var matchers by builder
			.comment(
				"The matchers are methods to determine the material of an item.",
				"This is primarily done by pattern matching on properties like id and tags."
			)
			.defineList("matchers", mutableListOf(
				configOf("type" to "tag", "pattern" to "forge:nuggets/(.*)"),
				configOf("type" to "tag", "pattern" to "forge:ingots/(.*)"),
				configOf("type" to "tag", "pattern" to "forge:storage_blocks/(.*)"),
				configOf("type" to "tag", "pattern" to "forge:ores/(.*)")
			), { true }) as ForgeConfigSpec.ConfigValue<MutableList<NCConfig>>

		init {
			builder.pop()
		}
	}
}

/**
 * Returns a new [NCConfig] with the specified contents, given as a list of pairs.
 *
 * @see [mutableMapOf] for how these pairs are processed
 */
fun configOf(vararg pairs: Pair<String, Any?>): NCConfig = NCConfig.wrap(mutableMapOf(*pairs), TomlFormat.instance())
