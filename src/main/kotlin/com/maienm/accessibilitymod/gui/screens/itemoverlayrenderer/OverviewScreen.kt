package com.maienm.accessibilitymod.gui.screens.itemoverlayrenderer

import com.maienm.accessibilitymod.gui.helpers.*
import com.maienm.accessibilitymod.gui.screens.BaseScreen
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen

class OverviewScreen(minecraft: Minecraft, lastScreen: Screen?) :
	BaseScreen(minecraft, lastScreen, "config.overlay.title") {

	override fun init() {
		super.init()

		addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
		addText(i18n("config.overlay.description")).centerX(0.5).setY1(40)
		addButton(i18n("config.matchers.title")) { toScreen(::MatchersScreen) }.centerX(0.6).setY(100, 120)
		addButton(i18n("config.back")) { toScreen(lastScreen!!) }.centerX(0.6).setY(-30, -10)
	}
}