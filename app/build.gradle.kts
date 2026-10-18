import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.baselineprofile")
}

val MARKETING_VERSION = "3.1.2"

val ciVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull()
    ?: MARKETING_VERSION.replace(".", "").toIntOrNull()
    ?: 1

val ciVersionName = (project.findProperty("versionName") as String?)
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: MARKETING_VERSION

android {
    namespace = "com.lucent.app"
    compileSdk = 36

    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "com.jiaying.yuan.lucentapp"
        minSdk = 28
        targetSdk = 36
        versionCode = ciVersionCode
        versionName = ciVersionName

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                arguments += "-DLUCENT_ENABLE_VULKAN=ON"

                System.getenv("LUCENT_SPIRV_HEADERS_DIR")?.takeIf { it.isNotBlank() }?.let { dir ->
                    arguments += "-DSPIRV-Headers_DIR=$dir"
                    arguments += "-DCMAKE_FIND_ROOT_PATH_MODE_PACKAGE=BOTH"
                }

                System.getenv("LUCENT_VULKAN_INCLUDE_DIR")?.takeIf { it.isNotBlank() }?.let { inc ->
                    arguments += "-DVulkan_INCLUDE_DIR=$inc"
                }
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.directories += layout.buildDirectory.dir("rustJniLibs").get().asFile.path
            assets.directories += layout.buildDirectory.dir("generated/lucentAssets").get().asFile.path
        }
        getByName("test") {
            kotlin.directories += rootProject.file("shared/src/commonTest/kotlin").path
        }
        getByName("androidTest") {
            assets.directories += rootProject.file("shared/schemas").path
        }
    }

    val releaseStorePath = System.getenv("LUCENT_KEYSTORE_FILE")
    signingConfigs {
        if (releaseStorePath != null) {
            create("release") {
                storeFile = file(releaseStorePath)
                storePassword = System.getenv("LUCENT_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("LUCENT_KEY_ALIAS") ?: "lucent"
                keyPassword = System.getenv("LUCENT_KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        aidl = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    lint {
        abortOnError = true
        baseline = file("lint-baseline.xml")
        disable += listOf(
            "NewerVersionAvailable",
            "GradleDependency",
            "AndroidGradlePluginVersion"
        )
    }
}

baselineProfile {
    automaticGenerationDuringBuild = false
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

val rustProjectDir = rootProject.file("rust")
val rustOutDir = layout.buildDirectory.dir("rustJniLibs")

fun toolWorks(vararg cmd: String): Boolean = try {
    val p = ProcessBuilder(*cmd).redirectErrorStream(true).start()
    p.inputStream.readBytes()
    p.waitFor() == 0
} catch (t: Throwable) {
    false
}

val cargoNdkReady = rustProjectDir.exists() && toolWorks("cargo", "ndk", "--version")

val cargoNdkBuild = tasks.register<Exec>("cargoNdkBuild") {
    group = "build"
    description = "Compile rust/ into liblucent_native.so for every packaged ABI"
    workingDir = rustProjectDir
    val rustTargets = listOf("arm64-v8a")
    commandLine(
        buildList {
            add("cargo"); add("ndk")
            rustTargets.forEach { add("-t"); add(it) }
            add("-o"); add(rustOutDir.get().asFile.absolutePath)
            add("build"); add("--release")
        }
    )
}

if (cargoNdkReady) {
    tasks.named("preBuild") { dependsOn(cargoNdkBuild) }
} else {
    logger.lifecycle(
        "lucent(:app, config): cargo-ndk not found - the ANDROID build would fall back to its " +
            "Kotlin implementations (irrelevant to :desktop tasks). To enable the Rust fast paths: " +
            "install rustup, run `cargo install cargo-ndk`, and add the Android targets."
    )
}

val downloadUbuntuRootfs = tasks.register("downloadUbuntuRootfs") {
    val rootfsUrl = "https://github.com/Lucent-Project/lucent-assets/releases/download/assets-v1/ubuntu-rootfs-arm64.tar.xz"
    val expectedSha256 = "981e34f621738f39bfbf187d9c2b2b2316bc7519af46c8f9adc4373b328c29dc"
    val cacheDir = layout.buildDirectory.dir("lucent-assets-cache").get().asFile
    val cachedFile = cacheDir.resolve("ubuntu-rootfs-arm64.tar.xz")
    val outputDir = layout.buildDirectory.dir("generated/lucentAssets/runtime").get().asFile
    val outputFile = outputDir.resolve("ubuntu-rootfs-arm64.tar.xz")

    inputs.property("url", rootfsUrl)
    inputs.property("sha256", expectedSha256)
    outputs.file(outputFile)

    doLast {
        fun calcSha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(65536)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
            return digest.digest().joinToString("") { b -> "%02x".format(b) }
        }

        val cacheValid = cachedFile.exists() && calcSha256(cachedFile).equals(expectedSha256, ignoreCase = true)
        if (!cacheValid) {
            cacheDir.mkdirs()
            val tempFile = File.createTempFile("ubuntu-rootfs-", ".tmp", cacheDir)
            try {
                var currentUrl = rootfsUrl
                var connection = URI(currentUrl).toURL().openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                while (connection.responseCode in listOf(301, 302, 303, 307, 308)) {
                    val location = connection.getHeaderField("Location")
                        ?: throw GradleException("Redirect without Location header from $currentUrl")
                    currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                        location
                    } else {
                        URI(currentUrl).resolve(location).toString()
                    }
                    connection.disconnect()
                    connection = URI(currentUrl).toURL().openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = true
                }
                if (connection.responseCode != 200) {
                    throw GradleException("Failed to download $currentUrl: HTTP ${connection.responseCode}")
                }
                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                connection.disconnect()

                val downloadedSha256 = calcSha256(tempFile)
                if (!downloadedSha256.equals(expectedSha256, ignoreCase = true)) {
                    tempFile.delete()
                    throw GradleException("SHA-256 mismatch for ubuntu-rootfs-arm64.tar.xz: expected $expectedSha256, got $downloadedSha256")
                }
                Files.move(
                    tempFile.toPath(),
                    cachedFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }

        outputDir.mkdirs()
        Files.copy(
            cachedFile.toPath(),
            outputFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}

tasks.named("preBuild") {
    dependsOn(downloadUbuntuRootfs)
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(downloadUbuntuRootfs)
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)

    implementation(libs.biometric)
    implementation(libs.fragment.ktx)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)

    implementation(libs.sqlcipher.android)
    implementation(libs.sqlite.ktx)

    implementation(libs.datastore.preferences)

    implementation(libs.okhttp)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.aidl)

    implementation(libs.haze)
    implementation(libs.haze.materials)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.org.json)
    testImplementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)

    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    implementation(libs.commons.compress)
    implementation(libs.xz)
}
