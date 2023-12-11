package kor.toxicity.font.manager

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kor.toxicity.font.api.LoggerLevel
import kor.toxicity.font.api.component.WidthComponent
import kor.toxicity.font.api.manager.ResourcePackManager
import kor.toxicity.font.api.renderer.*
import kor.toxicity.font.extension.*
import kor.toxicity.font.renderer.FontRendererImpl
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.Collections
import kotlin.math.ceil

object ResourcePackManagerImpl: ResourcePackManager {

    private val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)

    private const val FONT_RENDER_RANGE = 16

    private val rendererMap = HashMap<String, FontRenderer>()

    override fun reload() {
        rendererMap.clear()

        //Load default folder
        val font = dataFolder().subFolder("font")
        val layout = dataFolder().subFolder("layout")
        val imageFolder = dataFolder().subFolder("image")
        val asset = dataFolder().subFolder("build").apply {
            deleteRecursively()
            mkdir()
        }.subFolder("assets").subFolder("fontapi")
        val assetFont = asset.subFolder("font")
        val assetTextures = asset.subFolder("textures")
        val assetTexturesImage = assetTextures.subFolder("image")
        val assetOutput = assetTextures.subFolder("output")
        //Load default folder end

        //Load space font
        BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB).apply {
            createGraphics().run {
                composite = alphaComposite
                dispose()
            }
        }.saveTo(File(assetTextures.subFolder("font"), "splitter.png")).onFailure {
            LoggerLevel.WARN.log("Unable to save splitter.png")
        }
        JsonObject().apply {
            add("providers", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "fontapi:font/splitter.png")
                    addProperty("ascent", -9999)
                    addProperty("height", - 2)
                    add("chars", JsonArray().apply {
                        add(NEW_LAYER_FONT_KEY.toComponentChar())
                    })
                })
                add(JsonObject().apply {
                    addProperty("type", "space")
                    add("advances", JsonObject().apply {
                        for (i in -8192..8192) {
                            addProperty((i + FONT_LOCATION_KEY).toComponentChar(), i)
                        }
                    })
                })
            })
        }.saveTo(File(assetFont, "space.json")).onFailure {
            LoggerLevel.WARN.log("Unable to save space.json")
        }
        //Load space font end

        //Setup map
        val map = HashMap<String, FontContent>()
        //Setup map end

        //Font render start
        fun parseFont(folder: File, file: File, prefix: String = "") {
            if (file.isDirectory) {
                val subFolder = folder.subFolder(file.name)
                file.listFiles()?.forEach {
                    parseFont(subFolder, it, "$prefix${it.name}/")
                }
            } else {
                when (file.extension) {
                    "ttf", "otf" -> {
                        runCatching {
                            val yaml: FontInfoJson = File(file.parentFile, "${file.nameWithoutExtension}.json").apply {
                                if (!exists()) JsonObject().apply {
                                    addProperty("size", 16F)
                                }.saveToWithIndent(this)
                            }.bufferedReader().use {
                                GSON.fromJson(it, FontInfoJson::class.java)
                            }
                            val targetFolder = folder.subFolder(file.nameWithoutExtension)
                            val contentMap = HashMap<FontImage, MutableList<Char>>()
                            val fontFile = file.inputStream().buffered().use {
                                Font.createFont(Font.TRUETYPE_FONT, it)
                            }.deriveFont(yaml.size.coerceAtLeast(16F))
                            val size = ceil(yaml.size).toInt()
                            val imageList = HashMap<FontRange, MutableList<Pair<Char, BufferedImage>>>()
                            (Char.MIN_VALUE..Char.MAX_VALUE).forEach {
                                if (fontFile.canDisplay(it)) {
                                    getSubImage(BufferedImage(size, ceil(size * 1.4).toInt(), BufferedImage.TYPE_INT_ARGB).apply {
                                        createGraphics().run {
                                            composite = alphaComposite
                                            renderingHints[RenderingHints.KEY_TEXT_ANTIALIASING] = RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                                            renderingHints[RenderingHints.KEY_FRACTIONALMETRICS] = RenderingHints.VALUE_FRACTIONALMETRICS_ON
                                            drawString(it.toString(), 0, size)
                                            dispose()
                                        }
                                    })?.let { image ->
                                        imageList.getOrPut(FontRange(image.width, image.height)) {
                                            ArrayList()
                                        }.add(it to image)
                                    }
                                }
                            }
                            var max = 0
                            imageList.forEach { entry ->
                                val imageSize = entry.key
                                val targetList = entry.value

                                if (imageSize.height > max) max = imageSize.height

                                var i = 0
                                var sizeIndex = 0
                                val square = FONT_RENDER_RANGE * FONT_RENDER_RANGE

                                fun save(list: MutableList<Pair<Char, BufferedImage>>) {
                                    val name = "${file.nameWithoutExtension}_${imageSize.width}_${imageSize.height}_${++i}.png"
                                    val addList = contentMap.getOrPut(FontImage(name, entry.key)) {
                                        ArrayList()
                                    }
                                    BufferedImage(imageSize.width * FONT_RENDER_RANGE.coerceAtMost(list.size), imageSize.height * (list.size / FONT_RENDER_RANGE).coerceAtLeast(1), BufferedImage.TYPE_INT_ARGB).apply {
                                        createGraphics().run {
                                            composite = alphaComposite
                                            renderingHints[RenderingHints.KEY_TEXT_ANTIALIASING] = RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                                            renderingHints[RenderingHints.KEY_FRACTIONALMETRICS] = RenderingHints.VALUE_FRACTIONALMETRICS_ON
                                            list.forEachIndexed { index, pair ->
                                                addList.add(pair.first)
                                                drawImage(pair.second, (index % FONT_RENDER_RANGE) * imageSize.width, (index / FONT_RENDER_RANGE) * imageSize.height, null, null)
                                            }
                                            dispose()
                                        }
                                    }.saveTo(File(targetFolder, name)).onFailure {
                                        LoggerLevel.WARN.log("Unable to save this image: $name")
                                        LoggerLevel.WARN.log("Reason: ${it.message}")
                                    }
                                }

                                while (sizeIndex < targetList.size) {
                                    val subList = targetList.subList(sizeIndex, (sizeIndex + square).coerceAtMost(targetList.size))


                                    if (subList.size == square) {
                                        save(subList)
                                    } else {
                                        if (subList.size > FONT_RENDER_RANGE) {
                                            val lastDiv = (subList.size / FONT_RENDER_RANGE) * FONT_RENDER_RANGE
                                            save(subList.subList(0, lastDiv))
                                            if (subList.size % FONT_RENDER_RANGE != 0) save(subList.subList(lastDiv, subList.size))
                                        } else {
                                            save(subList)
                                        }
                                    }
                                    sizeIndex += square
                                }
                            }
                            map["$prefix${file.name}"] = FontContent(
                                max,
                                contentMap
                            )
                        }.onFailure {
                            LoggerLevel.WARN.log("Unable to load this font: ${file.path}")
                            LoggerLevel.WARN.log("Reason: ${it.message}")
                        }
                    }
                }
            }
        }
        font.listFiles()?.forEach {
            parseFont(assetOutput, it)
        }
        //Font render end

        //Load JSON
        fun loadLayout(folder: File, file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    loadLayout(folder.subFolder(it.name), it)
                }
            } else if (file.extension == "json") {
                runCatching {
                    val targetName = file.nameWithoutExtension
                    val targetFolder = folder.subFolder(targetName)
                    val loadInfo = file.bufferedReader().use {
                        GSON.fromJson(it, FontLoadJson::class.java)
                    }
                    val loadAsset = map[loadInfo.name] ?: run {
                        LoggerLevel.WARN.log("This font file doesn't exist: ${loadInfo.name}")
                        return@runCatching
                    }
                    val folderName = loadInfo.name.substringBeforeLast('.')
                    val maxMultiplier = ceil(loadAsset.maxHeight * loadInfo.multiplier).toInt()
                    val keyList = loadInfo.line.map { line ->
                        val array = JsonArray().apply {
                            add(JsonObject().apply {
                                addProperty("type", "space")
                                add("advances", JsonObject().apply {
                                    addProperty(" ", 4)
                                })
                            })
                        }
                        loadAsset.contents.forEach { entry ->
                            val key = entry.key
                            val value = entry.value
                            array.add(JsonObject().apply {
                                addProperty("type", "bitmap")
                                addProperty("file", "fontapi:output/$folderName/${key.file}")
                                addProperty("ascent", -ceil(loadInfo.multiplier * (loadAsset.maxHeight - key.range.height) + line).toInt() + maxMultiplier)
                                addProperty("height", ceil(key.range.height * loadInfo.multiplier).toInt())
                                add("chars", JsonArray().apply {
                                    val sb = StringBuilder()
                                    value.forEachIndexed { index, c ->
                                        sb.append(c)
                                        if ((index + 1) % FONT_RENDER_RANGE == 0) {
                                            add(sb.toString())
                                            sb.setLength(0)
                                        }
                                    }
                                    if (sb.isNotEmpty()) {
                                        add(sb.toString())
                                    }
                                })
                            })
                        }
                        val componentMap = HashMap<String, WidthComponent>()
                        val saveName = "${targetName}_${loadInfo.multiplier}_$line"
                        val key = Key.key("fontapi:output/$targetName/$saveName")
                        var image = FONT_LOCATION_KEY
                        loadInfo.images.forEach {
                            val png = File(imageFolder, it.value)
                            if (png.exists()) {
                                val pngMeta = File(imageFolder, "${it.value.substringBeforeLast('.')}.json").apply {
                                    if (!exists()) GSON.toJsonTree(FontImageJson()).saveToWithIndent(this)
                                }.bufferedReader().use { reader ->
                                    GSON.fromJson(reader, FontImageJson::class.java)
                                }
                                png.readToImage().onSuccess { rawImage ->
                                    getSubImage(rawImage)?.let { newSubImage ->
                                        val outputFile = File(assetTexturesImage, it.value)
                                        newSubImage.saveTo(outputFile).onSuccess { _ ->
                                            array.add(JsonObject().apply {
                                                addProperty("type", "bitmap")
                                                addProperty("file", "fontapi:image/${it.value}")
                                                addProperty("ascent", -ceil(pngMeta.multiplier * (loadAsset.maxHeight - newSubImage.height) + line).toInt() + ceil(loadAsset.maxHeight * pngMeta.multiplier).toInt() + pngMeta.ascent)
                                                addProperty("height", ceil(newSubImage.height * pngMeta.multiplier).toInt())
                                                val comp = (++image).toComponentChar()
                                                add("chars", JsonArray().apply {
                                                    add(comp)
                                                })
                                                componentMap[it.key] = WidthComponent(ceil(newSubImage.width * pngMeta.multiplier).toInt(), Component.text(comp).font(key))
                                            })
                                        }.onFailure { error ->
                                            LoggerLevel.WARN.log("Unable to save this image: ${outputFile.path}")
                                            LoggerLevel.WARN.log("Reason: ${error.message}")
                                        }
                                    }
                                }.onFailure { error ->
                                    LoggerLevel.WARN.log("Unable to read this image: ${png.path}")
                                    LoggerLevel.WARN.log("Reason: ${error.message}")
                                }
                            } else {
                                LoggerLevel.WARN.log("Cannot find that file: ${png.path}")
                            }
                        }
                        JsonObject().apply {
                            add("providers", array)

                        }.saveTo(File(targetFolder, "${saveName}.json")).onFailure {
                            LoggerLevel.WARN.log("Unable to save this file: $saveName")
                            LoggerLevel.WARN.log("Reason: ${it.message}")
                        }
                        ComponentKey(
                            key,
                            Collections.unmodifiableMap(componentMap)
                        )
                    }
                    rendererMap[file.nameWithoutExtension] = FontRendererImpl(
                        loadAsset,
                        keyList,
                        loadInfo.space,
                        loadInfo.multiplier
                    )
                }.onFailure {
                    LoggerLevel.WARN.log("Unable to make a json: ${file.path}")
                    LoggerLevel.WARN.log("Reason: ${it.message}")
                }
            } else {
                LoggerLevel.WARN.log("Unsupported file extension found: ${file.path}")
            }
        }
        val layoutOutput = assetFont.subFolder("output")
        layout.listFiles()?.forEach {
            loadLayout(layoutOutput,it)
        }
        //Load JSON end
    }

    private fun getSubImage(image: BufferedImage): BufferedImage? {
        var heightA = 0
        var heightB = image.height

        var widthA = 0
        var widthB = image.width

        for (i1 in 0..<image.width) {
            for (i2 in 0..<image.height) {
                if (image.getRGB(i1, i2) != 0) {
                    if (widthA < i1) widthA = i1
                    if (widthB > i1) widthB = i1
                    if (heightA < i2) heightA = i2
                    if (heightB > i2) heightB = i2
                }
            }
        }
        val width = widthA - widthB + 1
        val height = heightA - heightB + 1

        if (width <= 0 || height <= 0) return null

        val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = newImage.createGraphics()
        graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)

        graphics.drawImage(image.getSubimage(widthB, heightB, width, height), null, null)

        graphics.dispose()
        return newImage
    }

    override fun getRenderer(id: String): FontRenderer? {
        return rendererMap[id]
    }

    private data class FontInfoJson(
        var size: Float = 16F
    )
    private data class FontLoadJson(
        var name: String = "",
        var space: Int = 1,
        var multiplier: Double = 1.0,
        var images: Map<String, String> = emptyMap(),
        var line: List<Int> = emptyList()
    )

    private data class FontImageJson(
        var multiplier: Double = 1.0,
        var ascent: Int = 0
    )
}