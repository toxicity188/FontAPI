package kor.toxicity.font.platform

import com.google.inject.Inject
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kor.toxicity.font.FontMain
import kor.toxicity.font.api.FontAPI
import kor.toxicity.font.api.FontPlatform
import kor.toxicity.font.api.IFontAPI
import kor.toxicity.font.api.LoggerLevel
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.logging.Level

@Plugin(
    id = "FontAPI",
    description = "An API for font.",
    version = "1.0",
    authors = ["toxicity"]
)
class FontPlatformVelocity @Inject constructor(private val proxyServer: ProxyServer, private val logger: Logger, @DataDirectory private val path: Path): FontPlatform {
    init {
        FontAPI.setPlatform(this)
        proxyServer.commandManager.register(BrigadierCommand(
            LiteralArgumentBuilder.literal<CommandSource?>("fontapi")
                .requires { source ->
                    source.hasPermission("fontapi.reload")
                }
                .executes { context ->
                    FontMain.reloadAsync().thenRun {
                        context.source.sendMessage(Component.text("Reload completed."))
                    }
                    Command.SINGLE_SUCCESS
                }
                .build()
        ))
    }
    override fun getFontAPI(): IFontAPI {
        return FontMain
    }
    override fun getDataFolder(): File {
        return path.toFile()
    }

    override fun log(log: String, level: LoggerLevel) {
        when (level) {
            LoggerLevel.INFO -> logger.info(log)
            LoggerLevel.WARN -> logger.warn(log)
        }
    }
}