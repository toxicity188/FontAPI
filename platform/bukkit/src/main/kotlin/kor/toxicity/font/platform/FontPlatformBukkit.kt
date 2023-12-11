package kor.toxicity.font.platform

import kor.toxicity.font.FontMain
import kor.toxicity.font.api.FontAPI
import kor.toxicity.font.api.FontPlatform
import kor.toxicity.font.api.IFontAPI
import kor.toxicity.font.api.LoggerLevel
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class FontPlatformBukkit: JavaPlugin(), FontPlatform {
    override fun onLoad() {
        FontAPI.setPlatform(this)
    }
    override fun onEnable() {
        getCommand("fontapi")?.setExecutor { sender, _, _, _ ->
            if (sender.hasPermission("fontapi.reload")) {
                FontMain.reloadAsync().thenRun {
                    sender.sendMessage("Reload completed.")
                }
            } else sender.sendMessage("You have not permission to do that!")
            true
        }
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