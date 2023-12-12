package kor.toxicity.font.extension

import kor.toxicity.font.api.component.WidthComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

val SPACE_KEY = Key.key("fontapi:space")

const val FONT_LOCATION_KEY = 0xD0000
const val NEW_LAYER_FONT_KEY = 0xC0000

val NEW_LAYER = WidthComponent(0, Component.text(NEW_LAYER_FONT_KEY.toComponentChar()).font(SPACE_KEY))
val NEGATIVE_ONE_SPACE = WidthComponent(0, Component.text((-1 + FONT_LOCATION_KEY).toComponentChar()).font(SPACE_KEY))

fun Int.toComponentChar(): String {
    return if (this <= 0xFFFF) this.toChar().toString()
    else {
        val t = this - 0x10000
        return "${((t ushr 10) + 0xD800).toChar()}${((t and 1023) + 0xDC00).toChar()}"
    }
}

fun Int.toSpaceComponent() = WidthComponent(this, Component.text((FONT_LOCATION_KEY + this).toComponentChar()).font(SPACE_KEY))