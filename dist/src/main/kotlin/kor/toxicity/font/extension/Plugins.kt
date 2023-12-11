package kor.toxicity.font.extension

import kor.toxicity.font.api.FontAPI
import kor.toxicity.font.api.LoggerLevel

fun LoggerLevel.log(log: String) = FontAPI.getPlatform().log(log, this)