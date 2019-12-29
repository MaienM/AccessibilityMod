package com.maienm.accessibilitymod.items.matchers

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.streams.toList

/**
 * An item matcher that matches patterns against the tags of the item.
 */
class TagItemMatcher(pattern: String) : IItemMatcher {
	private val pattern: Pattern = pattern.toPattern()

	override fun getMaterials(itemStack: ItemStack) = itemStack.item.tags.stream()
		.map(ResourceLocation::toString)
		.map(pattern::matcher)
		.filter(Matcher::matches)
		.map { m -> m.group(1) }
		.toList()

	companion object {
		fun fromMap(map: Map<String, Any>) = TagItemMatcher(map.get("pattern").toString())
	}
}
