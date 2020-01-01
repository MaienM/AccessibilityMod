package com.maienm.accessibilitymod.gui.helpers

import net.minecraft.client.gui.widget.Widget

data class Position(val x: Int, val y: Int, val width: Int, val height: Int)

// Could be an actual interface once https://github.com/Kotlin/KEEP/pull/87 is in.
//interface IPositionable {
//	fun getPosition(): Position
//	fun setPosition(position: Position)
//}

fun <T: Widget> T.getPosition() = Position(this.x, this.y, this.width, this.height)
fun <T: Widget> T.setPosition(position: Position) {
	this.x = position.x
	this.y = position.y
	this.width = position.width
	this.height = position.height
}