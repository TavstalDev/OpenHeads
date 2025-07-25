/**
 * Configures the Gradle build script for the OpenHeads project.
 * This script sets up plugins, repositories, dependencies, and tasks required for building the project.
 */

plugins {
    // Apply the Java plugin for building Java projects
    id("java")
    // Apply the Shadow plugin for creating fat JARs
    id("com.gradleup.shadow") version "8.3.0"
    // Apply the Run-Paper plugin for running Paper Minecraft servers
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

// Define project properties for versions and package name
val javaVersion: String by project
val paperApiVersion: String by project
val snakeYamlVersion: String by project
val hikariCpVersion: String by project
val mineCoreLibVersion: String by project
val vaultApiVersion: String by project
val protocolLibVersion: String by project
val spiGuiVersion: String by project
val signGuiVersion: String by project
val projectPackageName = "${project.group}.openheads"

// Configure Java toolchain and compatibility settings
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

// Define repositories for dependency resolution
repositories {
    mavenCentral() // Central Maven repository
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io") // JitPack repository
    }
    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/") // ProtocolLib repository
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/") // Sonatype repository
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

// Define project dependencies
dependencies {
    // Paper API for Minecraft server development
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")

    // YAML parsing library
    implementation("org.yaml:snakeyaml:${snakeYamlVersion}")
    // HikariCP for database connection pooling
    implementation("com.zaxxer:HikariCP:${hikariCpVersion}")
    // SpiGUI for GUI creation
    implementation("com.samjakob:SpiGUI:${spiGuiVersion}")
    // SignGUI for sign-based input
    implementation("de.rapha149.signgui:signgui:${signGuiVersion}")
    // Custom library for core functionality
    implementation(files("libs/MineCoreLib-${mineCoreLibVersion}.jar"))
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.1")

    // Vault API for economy and permissions integration
    compileOnly("com.github.MilkBowl:VaultAPI:${vaultApiVersion}") {
        exclude(group = "org.bukkit", module = "bukkit") // Exclude Bukkit module
    }
    // ProtocolLib for packet manipulation
    compileOnly("com.comphenix.protocol:ProtocolLib:${protocolLibVersion}") {
        exclude(group = "org.bukkit", module = "bukkit") // Exclude Bukkit module
    }
}

// Disable the default JAR task
tasks.jar {
    enabled = false
}

// Configure the Shadow JAR task
tasks.shadowJar {
    archiveClassifier.set("") // Set the classifier for the JAR
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot" // Add custom manifest attributes
    }
    // Relocate packages to avoid conflicts
    relocate("org.yaml", "${projectPackageName}.shadow.snakeyaml")
    relocate("com.zaxxer.hikari", "${projectPackageName}.shadow.hikari")
    relocate("com.samjakob.spigui", "${projectPackageName}.shadow.spigui")
    relocate("de.rapha149.signgui", "${projectPackageName}.shadow.signgui")
    relocate("org.slf4j", "${projectPackageName}.shadow.slf4j")
}

// Ensure the Shadow JAR task runs during the build process
tasks.build {
    dependsOn(tasks.shadowJar)
}

// Configure additional tasks
tasks {
    // Configure the RunServer task for running a Paper server
    named<xyz.jpenilla.runpaper.task.RunServer>("runServer") {
        minecraftVersion("1.21") // Specify the Minecraft version
    }

    // Configure Java compilation settings
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8" // Set the file encoding
        val javaVersionInt = javaVersion.toInt()
        if (javaVersionInt >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(javaVersionInt) // Set the release version for Java
        }
    }

    // Process resources and expand placeholders in `plugin.yml`
    processResources {
        val props = mapOf("version" to project.version.toString()) // Define properties for resource filtering
        inputs.properties(props)
        filteringCharset = "UTF-8" // Set the charset for filtering
        filesMatching("plugin.yml") {
            expand(props) // Replace placeholders in `plugin.yml`
        }
    }
}