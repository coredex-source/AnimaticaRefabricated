plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.3-fabric"

tasks.register<Delete>("clean") {
    group = "build"
    delete(layout.projectDirectory.dir("bin"))
}

tasks.register("chiseledBuild") {
    group = "build"
    dependsOn(stonecutter.versions.map { ":${it.project}:build" })
}

tasks.register("binJar") {
    group = "build"
    dependsOn(stonecutter.versions.map { ":${it.project}:binJar" })
}
