plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    compileOnly(project(":api"))
    implementation("com.google.code.gson:gson:2.10.1")
}