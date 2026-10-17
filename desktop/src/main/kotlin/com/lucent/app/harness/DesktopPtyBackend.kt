package com.lucent.app.harness

import com.lucent.app.harness.terminal.PtyBackend
import com.lucent.app.harness.terminal.PtyProcess
import com.lucent.app.harness.terminal.PtyStartRequest
import com.lucent.app.platform.desktopPlatformContext
import com.lucent.app.platform.getFilesDir
import java.io.File
import okio.Sink
import okio.Source
import okio.sink
import okio.source

object DesktopPtyBackend : PtyBackend {

    override fun isReady(): Boolean = true

    override fun describe(): String = "desktop shell terminal"

    override fun start(request: PtyStartRequest): PtyProcess {
        val windows = System.getProperty("os.name", "").lowercase().contains("win")
        val command = if (windows) {
            listOf("powershell.exe", "-NoLogo")
        } else {
            listOf("/bin/bash", "-li")
        }
        val pb = ProcessBuilder(command)
        val dir = request.workdir?.toFile() ?: File(HarnessRuntime.workspacePath())
        if (dir.exists()) pb.directory(dir)
        pb.environment()["TERM"] = "xterm-256color"
        request.env.forEach { (key, value) -> pb.environment()[key] = value }
        if (windows) {
            val pathKey = pb.environment().keys.firstOrNull { it.equals("PATH", ignoreCase = true) } ?: "PATH"
            val currentPath = pb.environment()[pathKey]
            pb.environment()[pathKey] = resolveWindowsPath(currentPath)
        }
        pb.redirectErrorStream(true)
        return DesktopPtyProcess(pb.start())
    }

    private fun resolveWindowsPath(currentPath: String?): String {
        val paths = mutableListOf<String>()

        val bundledBin = resolveBundledBinDir()
        val hasBundledAdb = bundledBin != null && File(bundledBin, "adb.exe").exists()

        if (bundledBin != null && bundledBin.exists()) {
            paths.add(bundledBin.absolutePath)
        }

        val currentEntries = currentPath?.split(";")?.filter { it.isNotBlank() } ?: emptyList()
        paths.addAll(currentEntries)

        val pathHasAdb = hasBundledAdb || currentEntries.any { dir ->
            File(dir, "adb.exe").exists()
        }

        if (!pathHasAdb) {
            val androidHome = System.getenv("ANDROID_HOME")
            val sdkRoot = System.getenv("ANDROID_SDK_ROOT")
            val candidates = listOfNotNull(
                androidHome?.let { File(it, "platform-tools") },
                sdkRoot?.let { File(it, "platform-tools") }
            )
            for (candidate in candidates) {
                if (File(candidate, "adb.exe").exists()) {
                    paths.add(candidate.absolutePath)
                    break
                }
            }
        }

        return paths.distinct().joinToString(";")
    }

    private fun resolveBundledBinDir(): File? {
        val composeResources = System.getProperty("compose.application.resources.dir")?.let { File(it, "bin") }
        if (composeResources != null && File(composeResources, "adb.exe").exists()) return composeResources

        val devBin = File("desktop/src/main/resources/bin")
        if (File(devBin, "adb.exe").exists()) return devBin

        val userDirBin = File(System.getProperty("user.dir", ""), "desktop/src/main/resources/bin")
        if (File(userDirBin, "adb.exe").exists()) return userDirBin

        val targetDir = File(desktopPlatformContext.getFilesDir().toString(), "bin")
        val files = listOf("adb.exe", "AdbWinApi.dll", "AdbWinUsbApi.dll", "pkg.cmd", "pkg.ps1")
        val needsExtract = files.any { !File(targetDir, it).exists() }

        if (needsExtract) {
            targetDir.mkdirs()
            for (name in files) {
                val stream = DesktopPtyBackend::class.java.getResourceAsStream("/bin/$name")
                    ?: DesktopPtyBackend::class.java.getResourceAsStream("bin/$name")
                if (stream != null) {
                    val dest = File(targetDir, name)
                    stream.use { input ->
                        dest.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }

        return if (targetDir.exists()) targetDir else null
    }
}

private class DesktopPtyProcess(private val process: Process) : PtyProcess {
    override val input: Sink get() = process.outputStream.sink()
    override val output: Source get() = process.inputStream.source()
    override fun isAlive(): Boolean = process.isAlive
    override fun waitFor(): Int = process.waitFor()
    override fun destroy() = process.destroy()
    override fun destroyForcibly() {
        process.destroyForcibly()
    }
    override fun resize(cols: Int, rows: Int) = Unit
}
