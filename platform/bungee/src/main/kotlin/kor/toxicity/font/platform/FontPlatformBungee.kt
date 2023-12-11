package kor.toxicity.font.platform

import kor.toxicity.font.FontMain
import kor.toxicity.font.api.FontAPI
import kor.toxicity.font.api.FontPlatform
import kor.toxicity.font.api.IFontAPI
import kor.toxicity.font.api.LoggerLevel
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin

class FontPlatformBungee: Plugin(), FontPlatform {
    override fun onLoad() {
        FontAPI.setPlatform(this)
    }
    override fun onEnable() {
        ProxyServer.getInstance().pluginManager.registerCommand(this, object : Command("fontapi") {
            override fun execute(p0: CommandSender, p1: Array<String>) {
                if (p0.hasPermission("fontapi.reload")) {
                    FontMain.reloadAsync().thenRun {
                        p0.sendMessage(TextComponent("Reload completed."))
                    }
                } else p0.sendMessage(TextComponent("You have not permission to do that!"))
            }
        })
    }
    override fun getFontAPI(): IFontAPI {
        return FontMain
    }
    override fun log(log: String, level: LoggerLevel) {
        when (level) {
            LoggerLevel.INFO -> logger.info(log)
            LoggerLevel.WARN -> logger.warning(log)
        }
    }
}