plugins {
    `java-library`
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    group = "kor.toxicity.font"
    version = "1.0"

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        compileOnly("net.kyori:adventure-api:4.14.0")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }
    }
}

dependencies {
    implementation(project(":dist", configuration = "shadow"))

    implementation(project(":api"))

    implementation(project(":platform:bukkit"))
    implementation(project(":platform:bungee"))
    implementation(project(":platform:velocity"))
}

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        fun prefix(pattern: String) {
            relocate(pattern, "${project.group}.shaded.$pattern")
        }
        prefix("kotlin")
        prefix("com.google.gson")
    }
}

val targetJavaVersion = 17

kotlin {
    jvmToolchain(targetJavaVersion)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}