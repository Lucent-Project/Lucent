import org.jetbrains.compose.ComposeBuildConfig
import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    toolVersion = "1.23.8"
    buildUponDefaultConfig = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    sourceSets {
        main {
            kotlin.srcDir(rootProject.file("shared/src/main/kotlin"))
        }
        test {
            kotlin.srcDir(rootProject.file("shared/src/commonTest/kotlin"))
        }
    }
}

// kotlinx-datetime: shared is written against the 0.6.x API (kotlinx.datetime.Clock),
// but org.jetbrains.compose.material3:material3-desktop pulls 0.7.1, whose removal
// of Clock breaks shared at runtime. Pin the whole desktop graph to the catalog
// version. (material3's own datetime calls — LocalDateTime/TimeZone/Month — exist
// in 0.6.x, so this is safe for its DatePicker.)
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinxDatetime.get()}")
        force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:${libs.versions.kotlinxDatetime.get()}")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:${ComposeBuildConfig.composeMaterial3Version}")
    implementation(libs.compose.material.icons.extended)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(libs.okhttp)

    implementation(libs.org.json)

    testImplementation(kotlin("test"))
    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation(libs.kotlinx.serialization.json)
    // Explicit -jvm artifact: the root kotlinx-datetime module's variant matching
    // does not land the JVM classes on this plain-JVM module's runtime classpath
    // (NoClassDefFoundError: kotlinx/datetime at test runtime).
    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:${libs.versions.kotlinxDatetime.get()}")
    tasks.withType<Test> { useJUnitPlatform() }

    implementation(libs.sqlite.jdbc)

    implementation(libs.pdfbox)

    implementation(libs.jna.platform)

}

tasks.register<org.gradle.api.tasks.JavaExec>("cipherSelfCheck") {
    group = "verification"
    description = "Prove at-rest encryption end to end with the resolved sqlite-jdbc driver."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.lucent.desktop.CipherSelfCheckKt")
}

compose.desktop {
    application {
        mainClass = "com.lucent.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "Lucent"
            packageVersion = "3.1.2"

            includeAllModules = false
            modules("java.base", "java.desktop", "java.datatransfer", "java.sql", "java.net.http", "java.xml", "java.logging", "java.management", "jdk.unsupported", "jdk.crypto.ec", "jdk.crypto.mscapi")

            windows {
                menu = true
                menuGroup = "Lucent"
                shortcut = true
                upgradeUuid = "8f4e2a10-1c3b-4d5e-9a7f-2b6c8d0e1f23"
                dirChooser = true
                perUserInstall = true
                val icoFile = project.file("src/main/resources/icons/lucent.ico")
                if (icoFile.exists()) iconFile.set(icoFile)
            }
        }

        buildTypes.release.proguard {
            version.set("7.6.1")
            isEnabled.set(true)
            obfuscate.set(false)
            optimize.set(false)
            configurationFiles.from(project.file("proguard-desktop.pro"))
        }
    }
}

tasks.withType<org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask>().configureEach {
    stripDebug.set(true)
    noHeaderFiles.set(true)
    noManPages.set(true)
}

val stripNonWindowsNatives = tasks.register("stripNonWindowsNatives") {
    doLast {
        val keepMarkers = listOf("win32-x86-64", "windows-x86_64", "windows/x86-64")
        val dropDirMarkers = listOf(
            "win32-x86/", "win32-aarch64/",
            "windows/x86/", "windows/aarch64/", "windows/armv7/",
            "windows-x86/", "windows-aarch64/"
        )
        val dropOsTokens = listOf("linux", "mac", "darwin", "musl", "freebsd", "openbsd")
        val binariesDir = layout.buildDirectory.dir("compose/binaries").get().asFile
        if (!binariesDir.isDirectory) return@doLast
        var jarsTouched = 0
        var entriesDropped = 0
        binariesDir.walkTopDown()
            .filter { it.isDirectory && it.name == "app" }
            .forEach { appDir ->
                appDir.walkTopDown()
                    .filter { it.isFile && it.extension.equals("jar", ignoreCase = true) }
                    .forEach { jar ->
                        val tmp = kotlin.io.path.createTempFile("strip", ".jar").toFile()
                        var dropped = 0
                        java.util.zip.ZipFile(jar).use { zin ->
                            java.util.zip.ZipOutputStream(tmp.outputStream().buffered()).use { zout ->
                                val entries = zin.entries().asSequence().toList()
                                for (e in entries) {
                                    val lower = e.name.lowercase()
                                    val keep = keepMarkers.any { lower.contains(it) }
                                    val segments = lower.split('/')
                                    val drop = !keep && (
                                        lower.endsWith(".so") || lower.endsWith(".dylib") ||
                                        dropDirMarkers.any { lower.contains(it) } ||
                                        dropOsTokens.any { tok ->
                                            segments.any { seg -> seg == tok || seg.startsWith("$tok-") || seg.startsWith("${tok}_") }
                                        }
                                    )
                                    if (drop) {
                                        dropped++
                                    } else {
                                        zout.putNextEntry(java.util.zip.ZipEntry(e))
                                        zin.getInputStream(e).use { it.copyTo(zout) }
                                        zout.closeEntry()
                                    }
                                }
                            }
                        }
                        if (dropped > 0) {
                            java.nio.file.Files.move(
                                tmp.toPath(), jar.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            )
                            jarsTouched++
                            entriesDropped += dropped
                        } else {
                            tmp.delete()
                        }
                    }
            }
        logger.lifecycle("stripNonWindowsNatives: dropped $entriesDropped entries from $jarsTouched jars")
    }
}
tasks.configureEach {
    if (name == "packageReleaseDistributionForCurrentOS") dependsOn(stripNonWindowsNatives)
}
stripNonWindowsNatives.configure {
    dependsOn(tasks.matching { it.name == "createReleaseDistributable" })
    mustRunAfter(tasks.matching { it.name == "createReleaseDistributable" })
}
