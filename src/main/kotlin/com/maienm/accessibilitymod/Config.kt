package com.maienm.accessibilitymod

import com.electronwill.nightconfig.toml.TomlFormat
import io.opencubes.boxlin.getValue
import io.opencubes.boxlin.setValue
import net.minecraftforge.common.ForgeConfigSpec
import com.electronwill.nightconfig.core.CommentedConfig as NCCConfig
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
			Text
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
			.define("materialNames", commentedConfigOf("iron" to "Fe", "gold" to "Au"))

		init {
			builder.pop()
		}

		object Text {
			init {
				builder
					.comment("Settings related to the appearance of the text.")
					.push("text")
			}

			var minScale by builder
				.comment(
					"The smallest scale to render the font as.",
					"If the chosen text doesn't fit in the item's area at this size, it may extend to beyond this area."
				)
				.defineInRange("minScale", 0.4, 0.1, 2.0)

			var maxScale by builder
				.comment("The largest scale to render the font as.")
				.defineInRange("maxScale", 0.8, 0.1, 2.0)

			init {
				builder.pop()
			}
		}
	}
}

/**
 * Returns a new [NCConfig] with the specified contents, given as a list of pairs.
 *
 * @see [mutableMapOf] for how these pairs are processed
 */
fun configOf(vararg pairs: Pair<String, Any?>): NCConfig = NCConfig.wrap(mutableMapOf(*pairs), TomlFormat.instance())

/**
 * Like [configOf], but return a [NCCConfig] instead.
 */
fun commentedConfigOf(vararg pairs: Pair<String, Any?>): NCCConfig =
	NCCConfig.wrap(mutableMapOf(*pairs), TomlFormat.instance())
