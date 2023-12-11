plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "FontAPI"

include(
    "api",
    "dist",
    "example",
    "platform:bukkit",
    "platform:bungee",
    "platform:velocity"
)