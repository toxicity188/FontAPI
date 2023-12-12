package kor.toxicity.font.parser

import kor.toxicity.font.api.component.WidthComponent
import kor.toxicity.font.api.parser.FontParser
import kor.toxicity.font.api.parser.FontParser.Builder
import kor.toxicity.font.api.parser.FontParserContext
import kor.toxicity.font.api.parser.FontParserTag
import kor.toxicity.font.api.parser.FontTagMapper
import kor.toxicity.font.extension.FONT_LOCATION_KEY
import kor.toxicity.font.extension.SPACE_KEY
import kor.toxicity.font.extension.toComponentChar
import kor.toxicity.font.extension.toSpaceComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

class FontParserImpl private constructor(
    private val space: Int,
    private val preProcess: (Int, Char) -> FontParserContext?,
    private val parserMap: Map<String, FontTagMapper>
): FontParser {
    private val spaceComponent = WidthComponent(space, Component.text((space - 1 + FONT_LOCATION_KEY).toComponentChar()).font(SPACE_KEY))
    private val moveComponent = 4.toSpaceComponent()

    companion object {
        private val negativeOne = WidthComponent(0, Component.text((- 1 + FONT_LOCATION_KEY).toComponentChar()).font(SPACE_KEY))
        private val defaultMap = HashMap<String, FontTagMapper>().apply {
            NamedTextColor.NAMES.keyToValue().forEach {
                put(it.key.lowercase()) { _, context ->
                    context?.color = it.value
                    FontParserTag.tag(null)
                }
            }
            TextDecoration.entries.forEach {
                put(it.name.lowercase()) { _, context ->
                    context?.decorations?.put(it, TextDecoration.State.TRUE)
                    FontParserTag.tag(null)
                }
            }
            put("space") { args, _ ->
                if (args.isNotEmpty()) runCatching {
                    FontParserTag.selfClosingTag(args[0].toInt().toSpaceComponent())
                }.getOrNull() ?: FontParserTag.selfClosingTag(null) else FontParserTag.selfClosingTag(null)
            }
        }
    }
    constructor(space: Int, preProcess: (Int, Char) -> FontParserContext?): this(space, preProcess, defaultMap)

    override fun builder(): Builder {
        return BuilderImpl()
    }

    override fun parse(string: String): WidthComponent {
        val sb = StringBuilder()
        val stringList = ArrayList<FontParserContext>()
        val mapList = ArrayList<ParsedFunction>()
        var startIndex = 0
        var block = false
        var skip = false
        string.forEach { c ->
            if (skip) {
                startIndex++
                sb.append(c)
                skip = false
            } else when (c) {
                '\\' -> skip = true
                '<' -> {
                    block = true
                    sb.toString().forEach { s ->
                        stringList.addAll(s.toString().mapIndexedNotNull { index, target ->
                            preProcess(index, target)
                        })
                    }
                    sb.setLength(0)

                }
                '>' -> {
                    block = false
                    val str = sb.toString().split(':')
                    val name = str[0]
                    if (name.startsWith('/')) {
                        val sub = name.substring(1)
                        mapList.lastOrNull {
                            it.key == sub
                        }?.endIndex = startIndex
                    } else {
                        parserMap[str[0]]?.let { mapper ->
                            val args = str.subList(1, str.size).toTypedArray()
                            mapList.add(ParsedFunction(startIndex, Int.MAX_VALUE, name) {
                                mapper.apply(args, it)
                            })
                        }
                    }
                    sb.setLength(0)
                }
                else -> {
                    if (!block) startIndex++
                    sb.append(c)
                }
            }
        }
        if (sb.isNotEmpty()) {
            stringList.addAll(sb.mapIndexedNotNull { index, target ->
                preProcess(index, target)
            })
        }
        val componentCache = HashMap<Int, WidthComponent>()
        mapList.forEach {
            val last = it.endIndex.coerceAtMost(stringList.lastIndex)
            if (it.startIndex < last) {
                for (index in it.startIndex..<it.endIndex.coerceAtMost(stringList.lastIndex)) {
                    val tag = it.function(stringList[index])
                    tag.component?.let { component ->
                        componentCache[index] = component
                    }
                    when (tag.type) {
                        FontParserTag.Type.NONE -> {

                        }
                        FontParserTag.Type.SELF_CLOSING -> {
                            break
                        }
                    }
                }
            } else {
                val tag = it.function(if (it.startIndex == last) stringList[last] else null)
                tag.component?.let { component ->
                    componentCache[it.startIndex] = component
                }
            }
        }
        var widthComp = WidthComponent.EMPTY
        stringList.forEachIndexed { i, component ->
            widthComp = widthComp.append(if (component.content == ' ') moveComponent else WidthComponent(component.width, Component.text(component.content).style(Style.style(component.color).font(component.key).decorations(component.decorations))))
            if (component.content != ' ' && i < stringList.lastIndex) widthComp = widthComp.append(spaceComponent)
            componentCache[i + 1]?.let { comp ->
                widthComp = widthComp.append(comp).append(spaceComponent)
            }
        }
        return widthComp.append(negativeOne)
    }
    private class ParsedFunction(
        val startIndex: Int,
        var endIndex: Int,
        val key: String,
        val function: (FontParserContext?) -> FontParserTag
    )

    private inner class BuilderImpl: Builder {

        private val newMap = HashMap(parserMap)

        override fun append(
            tag: String,
            mapper: FontTagMapper
        ): Builder {
            newMap.putIfAbsent(tag, mapper)
            return this
        }

        override fun build(): FontParser {
            return FontParserImpl(space, preProcess, newMap)
        }
    }
}