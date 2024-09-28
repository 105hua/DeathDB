plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.2"
    id("com.diffplug.spotless") version "7.0.0.BETA2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.nearvanilla"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots") // For cloud
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-annotations:2.0.0")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0")
    implementation("com.zaxxer:HikariCP:6.0.0")
}

val targetJavaVersion = 21

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar{
    isEnableRelocation = true
    relocationPrefix = "${rootProject.property("group")}.${rootProject.property("name").toString().lowercase()}.lib"
    minimize()
    archiveClassifier.set("")
}

tasks.build{
    dependsOn("shadowJar")
}

tasks.runServer{
    minecraftVersion("1.21.1")
    jvmArgs("-Dcom.mojang.eula.agree=true")
}

spotless {
    format("misc") {
        target(listOf("**/*.gradle", "**/*.md"))
        trimTrailingWhitespace()
        indentWithSpaces(4)
    }
    kotlin {
        ktlint("1.1.0").editorConfigOverride(
            mapOf(
                "max_line_length" to 500
            )
        )
        licenseHeader("/* Licensed under GNU General Public License v3.0 */")
    }
}