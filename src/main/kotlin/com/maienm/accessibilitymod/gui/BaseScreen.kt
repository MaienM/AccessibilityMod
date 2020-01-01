package com.maienm.accessibilitymod.gui

import com.maienm.accessibilitymod.AccessibilityMod
import com.maienm.accessibilitymod.gui.helpers.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.resources.I18n
import net.minecraft.util.text.TranslationTextComponent

/**
 * A somewhat extended Screen.
 */
abstract class BaseScreen(
	minecraft: Minecraft,
	protected val lastScreen: Screen?,
	title: String
) : Screen(TranslationTextComponent("$I18N_PREFIX.$title")), ILayoutableWidgetContainer {
	override val layoutableWidgets: MutableList<ILayoutableWidget<*>> = mutableListOf()
	override val font by lazy { minecraft.fontRenderer }
	private var initialized = false

	init {
		this.minecraft = minecraft
	}

	override fun <T : Widget> add(widget: T) = addButton(widget)

	override fun getArea() = Area(0, 0, width, height)

	protected fun toScreen(screen: Screen) = minecraft!!.displayGuiScreen(screen)
	protected fun toScreen(constructor: (Minecraft, Screen?) -> BaseScreen) = toScreen(constructor(minecraft!!, this))

	protected fun i18n(key: String) = I18n.format("$I18N_PREFIX.$key")

	override fun init() {
		super.init()

		if (AccessibilityMod.DEBUG) {
			addButton("RL") { init() }.setX(5, 25).setY(-25, -5)
		}

		initialized = true
	}

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		updateAreas()
		renderBackground()
		super.render(mouseX, mouseY, partialT)
	}

	override fun resize(minecraft: Minecraft, width: Int, height: Int) {
		if (!initialized) {
			init(minecraft, width, height)
		}
		this.width = width
		this.height = height
	}

	companion object {
		const val I18N_PREFIX = "accessibilitymod.gui"
	}
}
