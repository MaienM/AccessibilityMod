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

		var enable by builder
			.comment("Whether this functionality should be enabled.")
			.define("enable", true)

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

		var materialNames by builder
			.comment(
				"The mappings determine the material names of the items, but these can be quite long and somewhat cryptic.",
				"In addition, some of them might not be useful to you. Because of this, these material names are not shown",
				"directly, but are instead used in this mapping to determine the text that should be shown.",
				"If a material is not in this mapping, no overlay will be shown for it."
			)
			.define("materialNames", configOf("iron" to "Fe", "gold" to "Au"))

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
