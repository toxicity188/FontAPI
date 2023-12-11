package kor.toxicity.font

import kor.toxicity.font.api.IFontAPI
import kor.toxicity.font.api.manager.ResourcePackManager
import kor.toxicity.font.api.parser.FontParser
import kor.toxicity.font.manager.ResourcePackManagerImpl
import kor.toxicity.font.parser.FontParserImpl

object FontMain: IFontAPI {
    private val managers = arrayOf(
        ResourcePackManagerImpl
    )

    override fun reload() {
        managers.forEach {
            it.reload()
        }
    }

    override fun getResourcePackManager(): ResourcePackManager = ResourcePackManagerImpl
}