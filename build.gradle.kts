@file:Suppress("PropertyName")

import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

//Kotlin
val kotlin_version: String by project
val language_support_version: String by project

//Mod Properties
val mod_version: String by project
val maven_group: String by project

//Fabric
val minecraft_version: String by project
val parchment_mappings: String by project
val loader_version: String by project
val loom_vineflower_version: String by project

//Dependencies
val fabric_version: String by project
val cloth_config_version: String by project
val mod_menu_version: String by project

//ESL
val esl_version: String by project
val rx_streams_version: String by project
val reflections_version: String by project
val dateparser_version: String by project

//Artemis
val wynntils_version: String by project
val mixinextras_version: String by project

//Objenesis
val objenesis_version: String by project

//Acf
val acf_fabric_version: String by project
val acf_version: String by project
val devauth_version: String by project

//Buster
val buster_version: String by project
val ktor_version: String by project

plugins {
    id("fabric-loom") version "1.7.4"
    kotlin("jvm") version "2.0.0"

    id("maven-publish")
}

version = "$mod_version+MC-${minecraft_version}"
group = maven_group

loom {
    accessWidenerPath = file("src/main/resources/fuy_gg.accesswidener")
}

repositories {
    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }

    maven("https://jitpack.io")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")

    flatDir {
        dirs("./libs/acf")
    }

    mavenLocal()
    mavenCentral()
}

fun DependencyHandlerScope.shadow(dependencyNotation: Any) {
    implementation(dependencyNotation)
    include(dependencyNotation)
}

fun wynntils(): ConfigurableFileCollection {
    val url = URI.create(
        "https://github.com/Wynntils/Artemis/releases/download/v$wynntils_version/wynntils-$wynntils_version-fabric+MC-$minecraft_version.jar"
    ).toURL()

    val name = "wynntils-$wynntils_version"

    val out = projectDir.toPath()
        .resolve("libs")
        .resolve("artemis")
        .resolve("$name.jar")

    if (!out.exists()) {
        out.parent.createDirectories()

        url.openStream().use {
            Files.copy(
                it,
                out,
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        val run = projectDir.toPath()
            .resolve("run")
            .resolve("mods")
            .resolve("wynntils.jar")

        run.parent.createDirectories()

        out.copyTo(
            run,
            overwrite = true
        )
    }

    return files(out)
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")

    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchment_mappings}@zip")
    })

    //Fabric
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")

    //Language Support
    modImplementation("net.fabricmc:fabric-language-kotlin:$language_support_version+kotlin.$kotlin_version")

    //Config
    modApi("me.shedaniel.cloth:cloth-config-fabric:${cloth_config_version}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modApi("com.terraformersmc:modmenu:$mod_menu_version")

    //ESL
    shadow("org.reactivestreams:reactive-streams:$rx_streams_version")
    shadow("org.reflections:reflections:$reflections_version")
    shadow("org.javassist:javassist:3.29.2-GA") // Required for reflections
    shadow("com.github.sisyphsu:dateparser:$dateparser_version")
    shadow("com.github.essentuan:esl:v$esl_version")

    //Wynntils
    modCompileOnly(wynntils())

    //Objenesis
    shadow("org.objenesis:objenesis:$objenesis_version")

    //ACF
    modImplementation(files("libs/acf/acf-fabric-${acf_fabric_version}.jar"))
    include(group = "", name = "acf-fabric-${acf_fabric_version}")

    implementation(files("libs/acf/ACF-${acf_version}.jar"))
    include(group = "", name = "ACF-${acf_version}")

    //Buster
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")

    include("io.ktor:ktor-client-cio-jvm:$ktor_version")
    include("io.ktor:ktor-client-core-jvm:$ktor_version")
    include("io.ktor:ktor-websockets-jvm:$ktor_version")
    include("io.ktor:ktor-client-websockets-jvm:$ktor_version")
    include("io.ktor:ktor-websocket-serialization-jvm:$ktor_version")
    include("io.ktor:ktor-events-jvm:$ktor_version")
    include("io.ktor:ktor-http-cio-jvm:$ktor_version")
    include("io.ktor:ktor-http-jvm:$ktor_version")
    include("io.ktor:ktor-io-jvm:$ktor_version")
    include("io.ktor:ktor-network-jvm:$ktor_version")
    include("io.ktor:ktor-network-tls-jvm:$ktor_version")
    include("io.ktor:ktor-serialization-jvm:$ktor_version")
    include("io.ktor:ktor-utils-jvm:$ktor_version")

    shadow("com.github.Essentuan:buster:v$buster_version")

    //DevAuth
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${devauth_version}")
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    processResources {
        filteringCharset = "UTF-8"

        inputs.property("version", version)
        inputs.property("minecraft_version", minecraft_version)
        inputs.property("loader_version", loader_version)

        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.version,
                    "minecraft_version" to minecraft_version,
                    "loader_version" to loader_version,
                    "wynntils_version" to wynntils_version,
                    "cloth_config_version" to cloth_config_version,
                    "language_support_version" to "$language_support_version+kotlin.$kotlin_version"
                )
            )
        }
    }

    compileKotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName}" }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "fuy_gg"

            from(components["java"])
        }
    }
}
