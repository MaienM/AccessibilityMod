package com.maienm.accessibilitymod.items.matchers

import com.maienm.accessibilitymod.Config
import net.minecraft.item.ItemStack
import com.electronwill.nightconfig.core.Config as NCConfig

typealias ItemMatcherInitializer = (Map<String, Any>) -> IItemMatcher

interface IItemMatcher {
	/**
	 * Get the material name(s) for for the given item.
	 *
	 * @param itemStack - The itemstack to get the material of. Guaranteed to be non-empty.
	 * @returns The material name(s) for the given item.
	 */
	fun getMaterials(itemStack: ItemStack): Iterable<String>

	/**
	 * Manage the available IItemMatcher types.
	 */
	object TypeRegistry {
		private val classes: MutableMap<String, ItemMatcherInitializer> = mutableMapOf()

		fun register(name: String, initializer: ItemMatcherInitializer) {
			if (classes.containsKey(name)) {
				throw IllegalArgumentException("IItemMatcher type $name is already registered.")
			}
			classes[name] = initializer
		}

		fun create(config: Map<String, Any>): IItemMatcher {
			val initializer = classes[config["type"]]
				?: throw IllegalArgumentException("IItemMatcher type ${config["type"]} not found.")
			return initializer(config)
		}
	}

	object InstanceRegistry {
		private var instances: List<IItemMatcher> = listOf()

		fun reload() {
			instances = Config.ItemMaterialOverlay.matchers.map(NCConfig::valueMap).map(TypeRegistry::create)
		}

		fun getMaterials(itemStack: ItemStack) = instances.flatMap { im -> im.getMaterials(itemStack) }
	}
}
