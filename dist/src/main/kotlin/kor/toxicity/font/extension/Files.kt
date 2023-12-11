package kor.toxicity.font.extension

import kor.toxicity.font.api.FontAPI
import java.awt.image.RenderedImage
import java.io.File
import javax.imageio.ImageIO

fun dataFolder() = FontAPI.getPlatform().dataFolder.apply {
    if (!exists()) mkdir()
}
fun File.subFolder(dir: String) = File(this, dir).apply {
    if (!exists()) mkdir()
}
fun File.readToImage() = runCatching {
    ImageIO.read(this)
}

fun RenderedImage.saveTo(file: File): Result<Unit> {
    return runCatching {
        ImageIO.write(this, "png", file)
    }
}