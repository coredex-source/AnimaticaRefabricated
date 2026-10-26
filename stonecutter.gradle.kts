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
    dependsOn(":26.3-fabric:build", ":26.3-neoforge:build")
}

tasks.register("binJar") {
    group = "build"
    dependsOn(":26.3-fabric:binJar", ":26.3-neoforge:binJar")
}
