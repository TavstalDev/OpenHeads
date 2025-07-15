plugins {
    id("java")
}
val junitVersion: String by project
val paperApiVersion: String by project
val hikariCpVersion: String by project
val mineCoreLibVersion: String by project
val vaultApiVersion: String by project
val protocolLibVersion: String by project
val spiGuiVersion: String by project
val signGuiVersion: String by project

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")
    implementation("com.zaxxer:HikariCP:${hikariCpVersion}")

    // Exclude the bukkit module from VaultAPI and ProtocolLib to avoid conflicts
    compileOnly("com.github.MilkBowl:VaultAPI:${vaultApiVersion}") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.comphenix.protocol:ProtocolLib:${protocolLibVersion}") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    implementation("com.samjakob:SpiGUI:${spiGuiVersion}")
    implementation("de.rapha149.signgui:signgui:${signGuiVersion}")

    implementation(files("libs/MineCoreLib-${mineCoreLibVersion}.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("mojangJar") {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    // Set .jar name
    archiveBaseName.set("openheads-mojang")

    // Set the duplicates strategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Collect runtime classpath files
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })

    // Optionally, include the compiled classes
    from(sourceSets.main.get().output)
}

tasks.register<Jar>("spigotJar") {
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }

    // Set .jar name
    archiveBaseName.set("openheads-spigot")

    // Set the duplicates strategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Collect runtime classpath files
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })

    // Optionally, include the compiled classes
    from(sourceSets.main.get().output)
}

tasks.register("buildJars") {
    dependsOn("mojangJar", "spigotJar")
}

tasks.named("build") {
    dependsOn("processResources")
    dependsOn("buildJars")
}

tasks.named<Jar>("jar") {
    enabled = false
}