plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "uk.co.webdent"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":paper-1.21"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
