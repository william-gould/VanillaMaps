plugins {
    id("fabric-loom") version "1.16-SNAPSHOT"
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")
    modImplementation(files("${rootDir}/cloth-config-21.11.153-fabric.jar"))
    implementation(project(":common"))
    include(project(":common"))
    compileOnly("org.spongepowered:mixin:0.8.5")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveBaseName.set("VanillaMaps-Fabric-1.21.11")
    archiveVersion.set(project.version.toString())

    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}