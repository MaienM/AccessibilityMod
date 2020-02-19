package com.maienm.accessibilitymod.items.matchers

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.streams.toList

/**
 * An item matcher that matches patterns against the ID of the item.
 */
class IDItemMatcher(pattern: String) : IItemMatcher {
	private val pattern: Pattern = pattern.toPattern()

	override fun getMaterials(itemStack: ItemStack) = listOf(itemStack.item.registryName)
		.filterNotNull()
		.map(ResourceLocation::toString)
		.map(pattern::matcher)
		.filter(Matcher::matches)
		.map { m -> m.group(1) }
		.toList()

	companion object {
		fun fromMap(map: Map<String, Any>) = IDItemMatcher(map.get("pattern").toString())

		val FIELDS = mapOf("pattern" to "matchers.id.pattern")

		val VALIDATOR = object : IItemMatcher.Validator() {
			init {
				setValidator("pattern", validator = ::validatePattern)
			}

			fun validatePattern(value: Any) = sequence<String> {
				try {
					val pattern = value.toString().toPattern()
					if (pattern.matcher("").groupCount() != 1) {
						yield(IItemMatcher.i18n("id.errors.pattern-num-capture-groups"))
					}
				} catch (e: PatternSyntaxException) {
					yield(IItemMatcher.i18n("id.errors.pattern-syntax", e.description.toLowerCase()))
				}
			}
		}
	}
}
