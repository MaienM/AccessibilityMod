package com.maienm.accessibilitymod.gui.screens.itemoverlayrenderer

import com.maienm.accessibilitymod.Config
import com.maienm.accessibilitymod.gui.helpers.Dimensions
import com.maienm.accessibilitymod.gui.helpers.YEdge
import com.maienm.accessibilitymod.gui.helpers.addButton
import com.maienm.accessibilitymod.gui.helpers.addText
import com.maienm.accessibilitymod.gui.helpers.centerX
import com.maienm.accessibilitymod.gui.helpers.renderBackground
import com.maienm.accessibilitymod.gui.helpers.setHeight
import com.maienm.accessibilitymod.gui.helpers.setX
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
import com.maienm.accessibilitymod.items.matchers.IItemMatcher
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraftforge.fml.client.config.GuiButtonExt
import org.apache.logging.log4j.LogManager
import com.electronwill.nightconfig.core.Config as NCConfig

class MatchersScreen(minecraft: Minecraft, lastScreen: Screen?) :
		BaseScreen(minecraft, lastScreen, i18n("config.matchers.title")) {
	override fun init() {
		super.init()

		addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
		addText(i18n("config.matchers.description")).centerX(0.5).setY1(getWidget(-2), offset = 10)
		layout(PaginatedListWidget(
			minecraft!!.fontRenderer,
			Config.ItemMaterialOverlay.matchers,
			minWidgetSize = Dimensions(1, 50),
			maxColumns = 1,
			minRowSpacing = 3
		) { matcher -> MatcherEntry(minecraft!!.fontRenderer, matcher) })
			.centerX(0.6).setY1(getWidget(-2), offset = 10).setY2(-40)
		addText(i18n("config.matchers.add"))
			.setX1(0.8, 10).setX2(-10).setY1(getWidget(-2), edge = YEdge.TOP, offset = 2)
		IItemMatcher.TypeRegistry.list().forEach { type ->
			val baseConfig = NCConfig.inMemory()
			baseConfig.set<String>("type", type)
			addButton(i18n("matchers.$type.name")) {
				toScreen(EditScreen(minecraft!!, this, NCConfig.copy(baseConfig)))
			}.setX1(0.8, 2).setX2(-2).setY1(getWidget(-2), offset = 4).setHeight(20)
		}
		addButton(i18n("config.back")) { toScreen(lastScreen!!) }.centerX(0.6).setY(-30, -10)
	}

	/**
	 * Widget for a single matcher in the listing. Has buttons to delete/edit.
	 */
	inner class MatcherEntry(font: FontRenderer, private val matcher: NCConfig) : ContainerWidget(font) {
		init {
			val type: String = matcher["type"]
			addText(i18n("matchers.$type.name")).setX1(3).setY1(3)
			IItemMatcher.TypeRegistry.entry(type).fields.entries.forEachIndexed { i, (key, i18nKey) ->
				addText("${i18n(i18nKey)}: ${matcher.get<String>(key)}")
					.setX1(3).setY1(18 + i * minecraft!!.fontRenderer.FONT_HEIGHT).setY2(-65)
			}
			addButton(i18n("config.edit"), ::edit).setX1(-60).setX2(-2).setY1(2).setY2(0.5, -1)
			addButton(i18n("config.delete"), ::delete).setX1(-60).setX2(-2).setY1(0.5, 1).setY2(-2)
		}

		private fun edit() {
			toScreen(EditScreen(minecraft!!, this@MatchersScreen, matcher))
		}

		private fun delete() {
			setOverlay(DeleteConfirm(font, matcher) { setOverlay(null) })
		}
	}

	/**
	 * Widget to confirm/cancel a deletion. Meant to be rendered on top of a MatcherEntry.
	 */
	inner class DeleteConfirm(font: FontRenderer, private val matcher: NCConfig, private val close: () -> Unit) :
			ContainerWidget(font) {
		init {
			addText(i18n("config.matchers.confirm-delete"), TextWidget.Alignment.CENTER).setY1(5)
			addButton(i18n("config.delete-confirm"), ::confirm).setX1(2).setX2(0.5, -2).setY(-22, -2)
			addButton(i18n("config.delete-cancel"), ::cancel).setX1(0.5, 2).setX2(-2).setY(-22, -2)
		}

		private fun confirm() {
			Config.ItemMaterialOverlay.matchers.remove(matcher)
			close()
		}

		private fun cancel() {
			close()
		}

		override fun renderBackground() {
			super.renderBackground()
			renderBackground(minecraft!!, getArea(), 20)
		}
	}

	/**
	 * Screen to create/edit a matcher.
	 */
	inner class EditScreen(minecraft: Minecraft, lastScreen: Screen?, private val matcher: NCConfig) : BaseScreen(
		minecraft,
		lastScreen,
		i18n(
			"config.matchers.edit.title-${if (matcher.size() == 1) "new" else "existing"}",
			i18n("matchers.${matcher.get<String>("type")}.name")
		)
	) {
		private val type: String = matcher["type"]

		private val textWidgets: Map<String, TextFieldWidgetEx> by lazy {
			IItemMatcher.TypeRegistry.entry(type).fields.mapValues { (_, i18nKey) ->
				TextFieldWidgetEx(minecraft.fontRenderer, 0, 0, 0, 0, i18n(i18nKey))
			}
		}
		private val generalValidationWidget = TextWidget(minecraft.fontRenderer, "")
		private val validationWidgets: Map<String, TextWidget> by lazy {
			IItemMatcher.TypeRegistry.entry(type).fields.mapValues {
				TextWidget(minecraft.fontRenderer, "")
			}
		}
		private lateinit var saveButton: GuiButtonExt

		private val data: MutableMap<String, Any> = HashMap(matcher.valueMap())
		private val needsUpdate: Boolean
			get() = textWidgets.any { (key, widget) -> data[key] != widget.text }

		private var validationResult: IItemMatcher.Validator.Result? = null
		private val valid: Boolean
			get() = validationResult?.isValid() ?: false

		init {
			textWidgets.forEach { (key, field) -> field.text = data[key] as? String ?: "" }
		}

		override fun init() {
			super.init()

			addText(title.formattedText, TextWidget.Alignment.CENTER).setY1(15)
			layout(generalValidationWidget).centerX(0.5).setY1(getWidget(-2), offset = 10)

			var relativeTo = generalValidationWidget
			val textOffset = 10 - minecraft!!.fontRenderer.FONT_HEIGHT / 2
			IItemMatcher.TypeRegistry.entry(type).fields.entries.forEachIndexed { i, (key, i18nKey) ->
				val textWidget = textWidgets[key] ?: return
				val validationWidget = validationWidgets[key] ?: return

				// Position relative to the bottom of the validation message above, to allow repositioning when validation errors are shown.
				val fieldLayout = layout(textWidget)
					.setX(0.3, 0.8)
					.setY1(relativeTo, offset = if (i == 0) 10 else 3)
					.setHeight(20)

				// Label to the left and slightly down, to center it vertically.
				addText("${i18n(i18nKey)}:").setX1(0.2).setY1(fieldLayout.widget, YEdge.TOP, textOffset)

				// Validation text below the input. Keep reference to position the next element relative to it.
				layout(validationWidget).setX(0.3, 0.8).setY1(textWidget, offset = 3)
				relativeTo = validationWidget
			}

			addButton(i18n("config.edit-cancel")) { toScreen(lastScreen!!) }.setX1(0.3).setX2(0.5, -1).setY(-30, -10)
			saveButton = addButton(i18n("config.edit-save"), ::save).setX1(0.5, 1).setX2(0.7).setY(-30, -10).widget

			validate()
		}

		private fun validate() {
			if (!needsUpdate) {
				return
			}

			textWidgets.forEach { (key, field) -> data[key] = field.text }
			val result = IItemMatcher.TypeRegistry.validate(data)
			if (validationResult == result) {
				return
			}

			applyValidationResult(result)
			updateAreas()
		}

		private fun applyValidationResult(result: IItemMatcher.Validator.Result) {
			validationResult = result
			validationWidgets.forEach { (key, text) ->
				text.message = result.get(key).joinToString("\n")
				text.calculateHeight()
			}
			generalValidationWidget.message = result.getMissed().joinToString("\n")
			generalValidationWidget.calculateHeight()
			saveButton.active = valid
		}

		private fun save() {
			if (!valid) {
				return
			}
			matcher.valueMap().putAll(data)
			if (!Config.ItemMaterialOverlay.matchers.contains(matcher)) {
				Config.ItemMaterialOverlay.matchers.add(matcher)
			}
			toScreen(lastScreen!!)
		}

		override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
			validate()
			super.render(mouseX, mouseY, partialT)
		}
	}

	companion object {
		val LOGGER = LogManager.getLogger()
	}
}
