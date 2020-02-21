package com.maienm.accessibilitymod.gui.screens.itemoverlayrenderer

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.gui.helpers.Dimensions
import com.maienm.accessibilitymod.gui.helpers.YEdge
import com.maienm.accessibilitymod.gui.helpers.addButton
import com.maienm.accessibilitymod.gui.helpers.addText
import com.maienm.accessibilitymod.gui.helpers.centerX
import com.maienm.accessibilitymod.gui.helpers.setHeight
import com.maienm.accessibilitymod.gui.helpers.setX1
import com.maienm.accessibilitymod.gui.helpers.setX2
import com.maienm.accessibilitymod.gui.helpers.setY
import com.maienm.accessibilitymod.gui.helpers.setY1
import com.maienm.accessibilitymod.gui.helpers.setY2
import com.maienm.accessibilitymod.gui.screens.BaseScreen
import com.maienm.accessibilitymod.gui.widgets.ContainerWidget
import com.maienm.accessibilitymod.gui.widgets.PaginatedListWidget
import com.maienm.accessibilitymod.gui.widgets.TextFieldWidgetEx
import com.maienm.accessibilitymod.gui.widgets.TextWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraftforge.fml.client.config.GuiButtonExt
import org.apache.logging.log4j.LogManager

class MappingsScreen(minecraft: Minecraft, lastScreen: Screen?) :
		BaseScreen(minecraft, lastScreen, i18n("config.mappings.title")) {
	private val list: PaginatedListWidget<String>

	init {
		list = PaginatedListWidget(
			minecraft.fontRenderer,
			minWidgetSize = Dimensions(1, 16),
			maxColumns = 1,
			minRowSpacing = 3
		) { key -> MappingEntry(minecraft.fontRenderer, key) }
	}

	override fun init() {
		super.init()

		addText(title.formattedText, 1.2, TextWidget.Alignment.CENTER).setY1(15)
		addText(i18n("config.mappings.description")).centerX(0.5).setY1(getWidget(-2), offset = 10)
		layout(list).centerX(0.6).setY1(getWidget(-2), offset = 10).setY2(-40)
		addButton(i18n("config.mappings.add")) {
			Config.ItemMaterialOverlay.materialNames.add("", "")
			updateList()
			list.page = 1
		}.setX1(0.8, 2).setX2(-2).setY1(getWidget(-2), edge = YEdge.TOP).setHeight(20)
		addButton(i18n("config.back")) { toScreen(lastScreen!!) }.centerX(0.6).setY(-30, -10)

		updateList()
	}

	private fun updateList() {
		list.items = Config.ItemMaterialOverlay.materialNames.entrySet().toList().map { it.key }.sorted()
	}

	/**
	 * Widget for a single mapping in the listing. Has buttons to edit/delete.
	 */
	inner class MappingEntry(font: FontRenderer, private val key: String) : ContainerWidget(font) {
		val keyText: TextWidget
		val valueText: TextWidget

		init {
			keyText = addText(key).setX1(6).setX2(0.5, -58).setY1(4).widget
			valueText = addText(Config.ItemMaterialOverlay.materialNames[key]).setX1(0.5, -55).setX2(-113).setY1(4).widget
			addButton(i18n("config.edit"), ::edit).setX1(-118).setX2(-60).setY1 { _ -> -2 }.setY2 { height -> height + 2 }
			addButton(i18n("config.delete"), ::delete).setX1(-60).setX2(-2).setY1 { _ -> -2 }.setY2 { height -> height + 2 }

			if (key.isEmpty()) {
				edit()
			}
		}

		private fun delete() {
			setOverlay(DeleteOverlay(font, key) {
				setOverlay(null)
				updateList()
			})
		}

		private fun edit() {
			setOverlay(EditOverlay(font, key) {
				setOverlay(null)
				Config.ItemMaterialOverlay.materialNames.remove<String>("")
				updateList()
			})
		}
	}

	/**
	 * Widget to confirm/cancel a deletion. Meant to be rendered on top of a MappingEntry.
	 */
	inner class DeleteOverlay(font: FontRenderer, private val key: String, private val close: () -> Unit) :
			ContainerWidget(font) {
		init {
			addText(i18n("config.mappings.confirm-delete")).setX1(6).setY1 { dim -> (dim - font.FONT_HEIGHT) / 2 + 1 }
			addButton(i18n("config.delete-confirm"), ::confirm)
				.setX1(-118).setX2(-60).setY1 { _ -> -2 }.setY2 { height -> height + 2 }
			addButton(i18n("config.delete-cancel"), ::cancel)
				.setX1(-60).setX2(-2).setY1 { _ -> -2 }.setY2 { height -> height + 2 }
		}

		private fun confirm() {
			Config.ItemMaterialOverlay.materialNames.remove<String>(key)
			close()
		}

		private fun cancel() {
			close()
		}
	}

	/**
	 * Widget to edit a mapping. Meant to be rendered on top of a MappingEntry.
	 */
	inner class EditOverlay(font: FontRenderer, private val key: String, private val close: () -> Unit) :
			ContainerWidget(font) {
		private val keyField = TextFieldWidgetEx(minecraft!!.fontRenderer, 0, 0, 0, 0, "", key)
		private val nameField = TextFieldWidgetEx(
			minecraft!!.fontRenderer,
			0,
			0,
			0,
			0,
			"",
			Config.ItemMaterialOverlay.materialNames[key]
		)
		private val saveButton: GuiButtonExt

		init {
			if (key.isEmpty()) {
				layout(keyField).setX1(2).setX2(0.5, -62)
			} else {
				addText(key).setX1(6).setX2(0.5, -58).setY1(4)
			}
			layout(nameField).setX1(0.5, -59).setX2(-119)
			addButton(i18n("config.edit-save"), ::confirm)
				.setX1(-118).setX2(-60).setY1 { _ -> -2 }.setY2 { height -> height + 2 }.also {
					saveButton = it.widget
				}
			addButton(i18n("config.edit-cancel"), ::cancel)
				.setX1(-60).setX2(-2).setY1 { _ -> -2 }.setY2 { height -> height + 2 }
		}

		private fun confirm() {
			Config.ItemMaterialOverlay.materialNames.set<String>(keyField.text, nameField.text)
			updateList()
			list.goToItem(keyField.text)
			close()
		}

		private fun cancel() {
			updateList()
			close()
		}

		override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
			saveButton.active = !keyField.text.isEmpty() && !nameField.text.isEmpty()
			super.render(mouseX, mouseY, partialT)
		}
	}

	companion object {
		val LOGGER = LogManager.getLogger()
	}
}
