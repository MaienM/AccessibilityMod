package com.maienm.accessibilitymod.gui.screens

import com.maienm.accessibilitymod.gui.helpers.*
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen

class ConfigScreen(minecraft: Minecraft, lastScreen: Screen?) : BaseScreen(minecraft, lastScreen, "config.title") {
	override fun init() {
		super.init()

		addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
		addButton(i18n("config.matchers.title")) { toScreen(::MatchersScreen) }.centerX(0.6).setY(50, 70)
		addButton(i18n("config.close")) { toScreen(lastScreen!!) }.centerX(0.6).setY(-30, -10)
	}
}