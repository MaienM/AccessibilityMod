package com.maienm.accessibilitymod.items.matchers

import com.maienm.accessibilitymod.Config
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import com.electronwill.nightconfig.core.Config as NCConfig

typealias ItemMatcherInitializer<T> = (Map<String, Any>) -> T

interface IItemMatcher {
	/**
	 * Get the material name(s) for for the given item.
	 *
	 * @param itemStack - The itemstack to get the material of. Guaranteed to be non-empty.
	 * @returns The material name(s) for the given item.
	 */
	fun getMaterials(itemStack: ItemStack): Iterable<String>

	companion object {
		const val I18N_PREFIX = "accessibilitymod.gui.matchers"
		fun i18n(key: String, vararg parameters: Any) = I18n.format("${I18N_PREFIX}.$key", *parameters)
	}

	/**
	 * Simple validator class to validate the contents & presence of a config map.
	 *
	 * Note that extra fields (that is, for which no validator is defined) are considered an error as well.
	 */
	open class Validator {
		private val validators: MutableMap<String, (Any?) -> Sequence<String>> = mutableMapOf()

		init {
			setValidator("type") { emptySequence() }
		}

		protected fun setValidator(field: String, required: Boolean = true, validator: (Any) -> Sequence<String>) {
			if (required) {
				validators[field] = {
					sequence {
						if (it == null) {
							yield(i18n("errors.missing-field", field))
						} else {
							yieldAll(validator(it))
						}
					}
				}
			} else {
				validators.put(field) { it?.run(validator) ?: emptySequence() }
			}
		}

		fun validate(map: Map<String, Any>): Result {
			val errors = validators.mapValues { (field, validator) -> validator(map[field]) }.toMutableMap()
			map.keys.filterNot(validators::containsKey).forEach { field ->
				errors[field] = sequenceOf(i18n("errors.unknown-field", field))
			}
			return Result(errors)
		}

		data class Result(private val fieldErrors: Map<String, Sequence<String>>) {
			private val accessedFields: MutableList<String> = mutableListOf()

			fun get(field: String): Sequence<String> {
				accessedFields.add(field)
				return fieldErrors.getOrDefault(field, emptySequence())
			}

			fun getMissed() = sequence<String> {
				fieldErrors.filterKeys { !accessedFields.contains(it) }.values.forEach { yieldAll(it) }
			}

			fun isValid() = fieldErrors.values.none { it.count() > 0 }
		}
	}

	/**
	 * Manage the available IItemMatcher types.
	 */
	object TypeRegistry {
		private val classes: MutableMap<String, Entry<*>> = mutableMapOf()

		fun <T : IItemMatcher> register(
			name: String,
			fields: Map<String, String>,
			initializer: ItemMatcherInitializer<T>,
			validator: Validator
		) {
			if (classes.containsKey(name)) {
				throw IllegalArgumentException("IItemMatcher type $name is already registered.")
			}
			classes[name] = Entry(fields, initializer, validator)
		}

		fun list() = classes.keys
		fun entry(type: String) =
			classes[type] ?: throw IllegalArgumentException("IItemMatcher type ${type} not found.")

		fun create(config: Map<String, Any>) = entry(config["type"] as String).initializer(config)
		fun validate(config: Map<String, Any>) = entry(config["type"] as String).validator.validate(config)

		data class Entry<T : IItemMatcher>(
			val fields: Map<String, String>,
			val initializer: ItemMatcherInitializer<T>,
			val validator: Validator
		)
	}

	object InstanceRegistry {
		private var instances: List<IItemMatcher> = listOf()

		fun reload() {
			instances = Config.ItemMaterialOverlay.matchers.map(NCConfig::valueMap).map(TypeRegistry::create)
		}

		fun getMaterials(itemStack: ItemStack) = instances.flatMap { im -> im.getMaterials(itemStack) }
	}
}
