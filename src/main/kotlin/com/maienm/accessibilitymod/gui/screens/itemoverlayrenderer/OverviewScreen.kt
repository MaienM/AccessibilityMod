package com.maienm.accessibilitymod.gui.screens.itemoverlayrenderer

import com.maienm.accessibilitymod.gui.helpers.addButton
import com.maienm.accessibilitymod.gui.helpers.addText
import com.maienm.accessibilitymod.gui.helpers.centerX
import com.maienm.accessibilitymod.gui.helpers.setHeight
import com.maienm.accessibilitymod.gui.helpers.setY
import com.maienm.accessibilitymod.gui.helpers.setY1
import com.maienm.accessibilitymod.gui.screens.BaseScreen
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen

class OverviewScreen(minecraft: Minecraft, lastScreen: Screen?) :
		BaseScreen(minecraft, lastScreen, i18n("config.overlay.title")) {
	override fun init() {
		super.init()

		addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
		addText(i18n("config.overlay.description")).centerX(0.5).setY1(getWidget(-2), offset = 10)
		addButton(i18n("config.matchers.title")) { toScreen(::MatchersScreen) }
			.centerX(0.6).setY1(getWidget(-2), offset = 10).setHeight(20)
		addButton(i18n("config.mappings.title")) { toScreen(::MappingsScreen) }
			.centerX(0.6).setY1(getWidget(-2), offset = 4).setHeight(20)
		addButton(i18n("config.back")) { toScreen(lastScreen!!) }.centerX(0.6).setY(-30, -10)
	}
}
