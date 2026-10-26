plugins {
    id("dev.isxander.modstitch.base")
    id("net.fabricmc.fabric-loom") apply false
    // Newer than the ModDevGradle ModStitch depends on, so this version is the one used
    id("net.neoforged.moddev") apply false
}

fun stringProp(name: String): String = property(name) as String
fun optionalProp(name: String, consumer: (String) -> Unit) {
    (findProperty(name) as? String)?.let(consumer)
}

val targetMinecraftVersion = stringProp("deps.minecraft")
val loader = project.name.substringAfterLast("-")

version = "${stringProp("mod.version")}+${project.name}"
base.archivesName = stringProp("archives.name")

modstitch {
    minecraftVersion = targetMinecraftVersion
    javaVersion = 25

    metadata {
        modId = stringProp("mod.id")
        modName = stringProp("mod.name")
        modVersion = project.version.toString()
        modGroup = stringProp("mod.group")
        modAuthor = stringProp("mod.author")
        modDescription = stringProp("mod.description")
        modLicense = stringProp("mod.license")

        replacementProperties.put("mod_sources", stringProp("mod.sources"))
        replacementProperties.put("mod_icon", stringProp("mod.icon"))
        optionalProp("mod.minecraft_dependency") {
            replacementProperties.put("minecraft_dependency", it)
        }
        optionalProp("mod.neoforge_minecraft_dependency") {
            replacementProperties.put("neoforge_minecraft_dependency", it)
        }
    }

    loom {
        fabricLoaderVersion = stringProp("deps.fabric_loader")
    }

    moddevgradle {
        optionalProp("deps.neoforge") {
            neoForgeVersion = it
        }

        defaultRuns(server = false)
    }

    mixin {
        addMixinsToModManifest = false
        configs.register("animatica")
    }
}

stonecutter {
    constants {
        put("fabric", loader == "fabric")
        put("neoforge", loader == "neoforge")
    }
}

dependencies {
    // Optional, only used for PBR texture compatibility
    optionalProp("deps.iris") {
        modstitchModCompileOnly("maven.modrinth:iris:$it")
    }

    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${stringProp("deps.fabric_api")}")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

java {
    withSourcesJar()
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.register<Copy>("binJar") {
    group = "build"
    dependsOn("build", "sourcesJar")
    from(modstitch.finalJarTask.map { it.archiveFile })
    from(tasks.named<Jar>("sourcesJar").map { it.archiveFile })
    into(rootProject.layout.projectDirectory.dir("bin"))
}
