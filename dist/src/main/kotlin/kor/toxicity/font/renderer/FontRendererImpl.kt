package kor.toxicity.font.renderer

import kor.toxicity.font.api.component.WidthComponent
import kor.toxicity.font.api.parser.FontParser
import kor.toxicity.font.api.parser.FontParserContext
import kor.toxicity.font.api.parser.FontParserTag
import kor.toxicity.font.api.renderer.ComponentKey
import kor.toxicity.font.api.renderer.FontAlign
import kor.toxicity.font.api.renderer.FontContent
import kor.toxicity.font.api.renderer.FontRenderer
import kor.toxicity.font.extension.NEGATIVE_ONE_SPACE
import kor.toxicity.font.extension.NEW_LAYER
import kor.toxicity.font.extension.toSpaceComponent
import kor.toxicity.font.parser.FontParserImpl
import net.kyori.adventure.text.Component
import kotlin.math.ceil
import kotlin.math.floor

class FontRendererImpl(
    private val content: FontContent,
    private val key: List<ComponentKey>,
    private val space: Int,
    multiplier: Double
): FontRenderer {
    private val fontCharMap = HashMap<Char, Int>().apply {
        content.contents.forEach {
            val k = it.key
            it.value.forEach { char ->
                put(char, ceil(k.range.width * multiplier).toInt())
            }
        }
    }
    private val parserList = key.map {
        FontParserImpl(space) { _, char ->
            if (char == ' ') {
                FontParserContext(
                    ' ',
                    4,
                    it.key
                )
            } else {
                fontCharMap[char]?.let { width ->
                    FontParserContext(
                        char,
                        width,
                        it.key
                    )
                }
            }
        }.builder()
            .append("image") { args, _ ->
                if (args.isNotEmpty()) {
                    it.imageMap[args[0]]?.let {
                        FontParserTag.selfClosingTag(it)
                    } ?: FontParserTag.selfClosingTag(null)
                } else FontParserTag.selfClosingTag(null)
            }
            .build()
    }

    override fun getFontContent(): FontContent = content

    override fun getKeys(): List<ComponentKey> = key

    override fun getCharSpace(): Int = space
    override fun getParser(): List<FontParser> {
        return parserList
    }
    override fun builder(): FontRenderer.Builder = BuildImpl()

    private inner class BuildImpl: FontRenderer.Builder {

        private var comp: WidthComponent = WidthComponent.EMPTY
        private var align = FontAlign.LEFT
        private var max = 0

        override fun align(align: FontAlign): FontRenderer.Builder {
            this.align = align
            return this
        }

        override fun append(component: WidthComponent): FontRenderer.Builder {
            comp = comp.append(component).append(NEGATIVE_ONE_SPACE).append(NEW_LAYER)
            if (max < component.width) max = component.width
            return this
        }

        override fun append(strings: Array<String>): FontRenderer.Builder {
            if (strings.size > key.size) throw ArrayIndexOutOfBoundsException("${strings.size} > ${key.size}")
            val map = strings.mapIndexed { index, s ->
                val width = parserList[index].parse(s)
                if (max < width.width) max = width.width
                width
            }
            val maxComponent = (-max).toSpaceComponent()
            when (align) {
                FontAlign.LEFT -> {
                    map.forEachIndexed { index, widthComponent ->
                        comp = comp.append(widthComponent).append((max - widthComponent.width).toSpaceComponent())
                        if (index < map.lastIndex) comp = comp.append(maxComponent)
                    }
                }
                FontAlign.CENTER -> {
                    map.forEachIndexed { index, widthComponent ->
                        val widthMinus = (max.toDouble() - widthComponent.width.toDouble()) / 2.0
                        val floor = floor(widthMinus).toInt().toSpaceComponent()
                        val ceil = ceil(widthMinus).toInt().toSpaceComponent()
                        comp = comp.append(floor).append(widthComponent).append(ceil)
                        if (index < map.lastIndex) comp = comp.append(maxComponent)
                    }
                }
                FontAlign.RIGHT -> {
                    map.forEachIndexed { index, widthComponent ->
                        comp = comp.append((max - widthComponent.width).toSpaceComponent().append(widthComponent))
                        if (index < map.lastIndex) comp = comp.append(maxComponent)
                    }
                }
            }
            return this
        }

        override fun build(): WidthComponent = comp
    }
}