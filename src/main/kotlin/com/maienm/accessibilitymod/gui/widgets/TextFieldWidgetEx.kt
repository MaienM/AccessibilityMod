package com.maienm.accessibilitymod.gui.widgets

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.TextFieldWidget

class TextFieldWidgetEx(fontIn: FontRenderer, x: Int, y: Int, width: Int, height: Int, msg: String? = "", initial: String = "")
	: TextFieldWidget(fontIn, x, y, width, height, msg) {

	private var needsRefresh = false

	init {
		setText(initial)
	}

	override fun setText(textIn: String) {
		needsRefresh = true
		super.setText(textIn)
	}

	override fun render(mouseX: Int, mouseY: Int, partialT: Float) {
		if (needsRefresh) {
			moveCursorBy(0)
			needsRefresh = false
		}
		super.render(mouseX, mouseY, partialT)
	}
}