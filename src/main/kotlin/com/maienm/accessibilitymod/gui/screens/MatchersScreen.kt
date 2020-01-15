package com.maienm.accessibilitymod.gui.screens

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.gui.helpers.*
import com.maienm.accessibilitymod.gui.widgets.ContainerWidget
import com.maienm.accessibilitymod.gui.widgets.PaginatedListWidget
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.button.Button
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
		) { matcher -> MatcherEntry(minecraft!!.fontRenderer, matcher) }
		layout(list).centerX(0.6).setY(80, -40)
	}

	inner class MatcherEntry(font: FontRenderer, private val matcher: NCConfig) : ContainerWidget(font) {
		private var deleteConfirm: ILayoutableWidget<DeleteConfirm>? = null

		init {
			val type: String = matcher["type"]
			addText(i18n("matchers.$type.name")).setX1(3).setY1(3)
			matcher.valueMap().entries.filter { it.key != "type" }.forEachIndexed { i, entry ->
				addText("${i18n("matchers.$type.${entry.key}")}: ${entry.value}")
					.setX1(3)
					.setY1(18 + i * minecraft!!.fontRenderer.FONT_HEIGHT)
			}
			addButton(i18n("config.edit"), ::edit).setX1(-0.2).setX2(-2).setY1(2).setY2(0.5, -1)
			addButton(i18n("config.delete"), ::delete).setX1(-0.2).setX2(-2).setY1(0.5, 1).setY2(-2)
		}

		private fun edit(button: Button) {}

		private fun delete(button: Button) {
			deleteConfirm?.also { unlayout(it) }
			deleteConfirm = layout(DeleteConfirm(font, matcher) { deleteConfirm?.also { unlayout(it) } })
		}
	}

	inner class DeleteConfirm(font: FontRenderer, private val matcher: NCConfig, private val close: () -> Unit) :
		ContainerWidget(font) {

		init {
			addText("config.matchers.confirm-delete", TextWidget.Alignment.CENTER).setX1(5)
			addButton(i18n("config.delete-confirm"), ::confirm).setX1(2).setX2(0.5, -2).setY(-22, -2)
			addButton(i18n("config.delete-cancel"), ::cancel).setX1(0.5, 2).setX2(-2).setY(-22, -2)
		}

		private fun confirm(button: Button) {
			Config.ItemMaterialOverlay.matchers.remove(matcher)
			close()
		}

		private fun cancel(button: Button) {
			close()
		}

		override fun renderBackground() {
			super.renderBackground()
			renderBackground(minecraft!!, getArea())
		}
	}

	companion object {
		val LOGGER = LogManager.getLogger()
	}
}
