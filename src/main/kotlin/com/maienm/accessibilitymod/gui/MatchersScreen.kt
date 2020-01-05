package com.maienm.accessibilitymod.gui

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.gui.helpers.*
import com.maienm.accessibilitymod.gui.widgets.ContainerWidget
import com.maienm.accessibilitymod.gui.widgets.PaginatedListWidget
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import org.apache.logging.log4j.LogManager
import com.electronwill.nightconfig.core.Config as NCConfig

class MatchersScreen(minecraft: Minecraft, lastScreen: Screen?) :
	BaseScreen(minecraft, lastScreen, "config.matchers.title") {

	lateinit var list: PaginatedListWidget<NCConfig>

	override fun init() {
		super.init()

		addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
		addText(i18n("config.matchers.description")).centerX(0.5).setY1(40)
		addButton(i18n("config.back")) { toScreen(lastScreen!!) }.centerX(0.6).setY(-30, -10)

		list = PaginatedListWidget(
			minecraft!!.fontRenderer,
			Config.ItemMaterialOverlay.matchers,
			minWidgetSize = Dimensions(1, 60),
			maxColumns = 1,
			minRowSpacing = 3
		) { matcher ->
			val type: String = matcher["type"]
			ContainerWidget(minecraft!!.fontRenderer).also {
				it.addText(i18n("matchers.$type.name")).setX1(3).setY1(3)
				matcher.valueMap().entries.filter { it.key != "type" }.forEachIndexed { i, entry ->
					it.addText("${i18n("matchers.$type.${entry.key}")}: ${entry.value}")
						.setX1(3)
						.setY1(18 + i * minecraft!!.fontRenderer.FONT_HEIGHT)
				}
				it.addButton(i18n("config.edit")) {}.setX1(-0.2).setX2(-2).setY1(2).setY2(0.5, -1)
				it.addButton(i18n("config.delete")) {}.setX1(-0.2).setX2(-2).setY1(0.5, 1).setY2(-2)
			}
		}
		layout(list).centerX(0.6).setY(80, -40)
	}

	companion object {
		val LOGGER = LogManager.getLogger()
	}
}
