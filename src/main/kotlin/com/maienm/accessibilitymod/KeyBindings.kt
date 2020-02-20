package com.maienm.accessibilitymod

import net.minecraft.client.settings.KeyBinding
import net.minecraft.client.util.InputMappings
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.client.settings.KeyModifier
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.lwjgl.glfw.GLFW

object KeyBindings {
	private val bindings: MutableList<KeyBinding> = mutableListOf()

	val config = bindKey("config", KeyModifier.CONTROL, GLFW.GLFW_KEY_SEMICOLON)

	fun init() {
		bindings.forEach(ClientRegistry::registerKeyBinding)
	}

	private fun bindKey(name: String, modifier: KeyModifier, key: Int): KeyBinding {
		val keybind = KeyBinding(
			"accessibilitymod.key.$name",
			KeyConflictContext.IN_GAME,
			modifier,
			InputMappings.Type.KEYSYM.getOrMakeInput(key),
			"accessibilitymod.key.category"
		)
		bindings.add(keybind)
		return keybind
	}
}
