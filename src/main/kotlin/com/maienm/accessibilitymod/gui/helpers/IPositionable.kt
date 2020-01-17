package com.maienm.accessibilitymod.gui.helpers

import net.minecraft.client.gui.widget.Widget

// Could be an actual interface once https://github.com/Kotlin/KEEP/pull/87 is in.
//interface IPositionable {
//	fun getPosition(): Position
//	fun setPosition(position: Position)
//}

fun <T : Widget> T.getArea() = Area(this.x, this.y, this.width, this.height)
fun <T : Widget> T.setArea(area: Area) {
	this.x = area.x
	this.y = area.y
	this.width = area.width
	this.height = area.height
}
