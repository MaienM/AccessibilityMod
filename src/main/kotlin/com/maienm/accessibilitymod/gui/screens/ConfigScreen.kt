package com.maienm.accessibilitymod.gui.screens

import com.maienm.accessibilitymod.gui.helpers.addButton
import com.maienm.accessibilitymod.gui.helpers.addText
import com.maienm.accessibilitymod.gui.helpers.centerX
import com.maienm.accessibilitymod.gui.helpers.setHeight
import com.maienm.accessibilitymod.gui.helpers.setY
import com.maienm.accessibilitymod.gui.helpers.setY1
import com.maienm.accessibilitymod.gui.screens.itemoverlayrenderer.OverviewScreen
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen

class ConfigScreen(minecraft: Minecraft, lastScreen: Screen?) :
		BaseScreen(minecraft, lastScreen, i18n("config.title")) {
	override fun init() {
		super.init()

		addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
		addButton(i18n("config.overlay.title")) { toScreen(::OverviewScreen) }
			.centerX(0.6).setY1(getWidget(-2), offset = 10).setHeight(20)
		addButton(i18n("config.close")) { toScreen(lastScreen) }.centerX(0.6).setY(-30, -10)
	}
}
