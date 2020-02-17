package com.maienm.accessibilitymod.gui.screens

import com.maienm.accessibilitymod.AccessibilityMod
import com.maienm.accessibilitymod.gui.helpers.Area
import com.maienm.accessibilitymod.gui.helpers.ILayoutableWidget
import com.maienm.accessibilitymod.gui.helpers.ILayoutableWidgetContainer
import com.maienm.accessibilitymod.gui.helpers.addButton
import com.maienm.accessibilitymod.gui.helpers.setX
import com.maienm.accessibilitymod.gui.helpers.setY
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.Widget
import net.minecraft.client.resources.I18n
import net.minecraft.util.text.StringTextComponent

/**
 * A somewhat extended Screen.
 */
abstract class BaseScreen(minecraft: Minecraft, protected val lastScreen: Screen?, title: String) :
		Screen(StringTextComponent(title)), ILayoutableWidgetContainer {
	override val layoutableWidgets: MutableList<ILayoutableWidget<*>> = mutableListOf()
	override val font by lazy { minecraft.fontRenderer }
	private var initialized = false

	init {
		this.minecraft = minecraft
	}

	override fun <T : Widget> add(widget: T) = addButton(widget)
	override fun <T : Widget> remove(widget: T) {
		buttons.remove(widget)
		children.remove(widget)
	}

	override fun getWidget(index: Int) = buttons[(buttons.size + index) % buttons.size]

	override fun getArea() = Area(0, 0, width, height)

	protected fun toScreen(screen: Screen) = minecraft!!.displayGuiScreen(screen)
	protected fun toScreen(constructor: (Minecraft, Screen?) -> BaseScreen) = toScreen(constructor(minecraft!!, this))

	override fun init() {
		super.init()

		if (AccessibilityMod.DEBUG) {
			addButton("RL", ::reinit).setX(5, 25).setY(-25, -5)
		}

		initialized = true
	}

	private fun reinit() {
		buttons.clear()
		children.clear()
		layoutableWidgets.clear()
		init()
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
		fun i18n(key: String, vararg parameters: Any) = I18n.format("$I18N_PREFIX.$key", *parameters)
	}
}
