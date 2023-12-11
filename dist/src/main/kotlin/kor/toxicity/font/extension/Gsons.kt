package kor.toxicity.font.extension

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.stream.JsonWriter
import java.io.File

val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

fun JsonElement.saveTo(file: File): Result<Unit> {
    return runCatching {
        JsonWriter(file.bufferedWriter()).use {
            GSON.toJson(this, it)
        }
    }
}
fun JsonElement.saveToWithIndent(file: File): Result<Unit> {
    return runCatching {
        JsonWriter(file.bufferedWriter()).use {
            it.setIndent(" ")
            GSON.toJson(this, it)
        }
    }
}