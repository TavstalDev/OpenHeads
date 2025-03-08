plugins {
    id("java")
}

group = "io.github.tavstal"
version = "1.0-SNAPSHOT"

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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    implementation("com.samjakob:SpiGUI:1.3.1")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation(files("libs/MineCoreLib-1.0.jar"))
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