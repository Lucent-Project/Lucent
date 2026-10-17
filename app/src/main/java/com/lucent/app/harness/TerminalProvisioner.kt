package com.lucent.app.harness

import android.content.Context
import com.lucent.app.AppScope
import com.lucent.app.harness.terminal.TerminalSessions
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch

object TerminalProvisioner {

    private val isProvisioning = AtomicBoolean(false)

    fun ensureProvisioned(context: Context) {
        val rootfs = BuiltinShell.rootfsDir(context)
        val marker = File(rootfs, "var/lib/lucent-toolset-provisioned")
        if (marker.exists()) return
        if (!BuiltinShell.builtinAvailable(context) || !File(rootfs, "bin/bash").exists()) return
        if (!isProvisioning.compareAndSet(false, true)) return

        AppScope.io.launch {
            try {
                if (marker.exists()) return@launch
                val success = runProvisioning(context, rootfs)
                if (success) {
                    writePkgShim(rootfs)
                    appendBashrcMotd(rootfs)
                    marker.parentFile?.mkdirs()
                    marker.writeText("1\n")
                } else {
                    TerminalSessions.manager.writeToCurrent("\r\n[notice] Toolset provisioning failed; will retry next session.\r\n")
                    HarnessRuntime.note("terminal: toolset provisioning failed")
                }
            } catch (_: Throwable) {
                TerminalSessions.manager.writeToCurrent("\r\n[notice] Toolset provisioning failed; will retry next session.\r\n")
                HarnessRuntime.note("terminal: toolset provisioning failed")
            } finally {
                isProvisioning.set(false)
            }
        }
    }

    private fun runProvisioning(context: Context, rootfs: File): Boolean {
        BuiltinRuntimeInstaller.ensureHostIdentity(rootfs)
        ensureResolvConf(rootfs)

        val libDir = File(context.applicationInfo.nativeLibraryDir)
        val proot = File(libDir, "libproot.so")
        if (!proot.exists()) return false

        val tmpDir = File(context.filesDir, "proot-tmp").apply { mkdirs() }

        val args = mutableListOf<String>()
        args.add(proot.absolutePath)
        args.add("--kill-on-exit")
        args.add("-0")
        args.add("-r")
        args.add(rootfs.absolutePath)

        for (bindPath in listOf("/dev", "/proc", "/sys")) {
            if (File(bindPath).exists()) {
                args.add("-b")
                args.add(bindPath)
            }
        }

        val packages = "curl wget git openssh-client vim nano htop python3 python3-pip unzip zip iputils-ping file jq tmux less sudo dnsutils iproute2 ca-certificates gnupg"
        val command = "apt-get update && apt-get install -y $packages"

        args.add("/bin/bash")
        args.add("-c")
        args.add(command)

        val pb = ProcessBuilder(args)
        pb.environment()["PROOT_LOADER"] = File(libDir, "libprootloader.so").absolutePath
        val nativeLibDir = File(context.filesDir, "native-lib")
        pb.environment()["LD_LIBRARY_PATH"] = "${nativeLibDir.absolutePath}:${libDir.absolutePath}"
        pb.environment()["PROOT_TMP_DIR"] = tmpDir.absolutePath
        pb.environment()["HOME"] = "/root"
        pb.environment()["USER"] = "root"
        pb.environment()["PATH"] = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
        pb.environment()["TERM"] = "xterm"
        pb.environment()["DEBIAN_FRONTEND"] = "noninteractive"
        pb.environment()["SSL_CERT_FILE"] = "/etc/ssl/certs/ca-certificates.crt"
        pb.redirectErrorStream(true)

        val process = pb.start()
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { }
        }
        val exitCode = process.waitFor()
        return exitCode == 0
    }

    private fun ensureResolvConf(rootfs: File) {
        val resolv = File(rootfs, "etc/resolv.conf")
        if (!resolv.exists() || resolv.length() == 0L) {
            resolv.parentFile?.mkdirs()
            resolv.writeText("nameserver 223.5.5.5\nnameserver 119.29.29.29\nnameserver 8.8.8.8\nnameserver 1.1.1.1\n")
        }
    }

    private fun writePkgShim(rootfs: File) {
        val binDir = File(rootfs, "usr/local/bin")
        binDir.mkdirs()
        val pkgFile = File(binDir, "pkg")
        val content = """#!/bin/sh
cmd="${'$'}1"
if [ -n "${'$'}cmd" ]; then
  shift
fi
case "${'$'}cmd" in
  update)
    exec apt-get update "${'$'}@"
    ;;
  install)
    exec apt-get install -y "${'$'}@"
    ;;
  uninstall)
    exec apt-get remove -y "${'$'}@"
    ;;
  search)
    exec apt-cache search "${'$'}@"
    ;;
  list-installed)
    exec dpkg -l "${'$'}@"
    ;;
  upgrade)
    apt-get update && exec apt-get upgrade -y "${'$'}@"
    ;;
  --help|-h|help|"")
    echo "Usage: pkg <command> [arguments]"
    echo ""
    echo "Commands:"
    echo "  update          Update package lists"
    echo "  install <pkg>   Install package(s)"
    echo "  uninstall <pkg> Remove package(s)"
    echo "  search <query>  Search for packages"
    echo "  list-installed  List installed packages"
    echo "  upgrade         Upgrade all packages"
    exit 0
    ;;
  *)
    echo "pkg: unknown command '${'$'}cmd'"
    echo "Run 'pkg --help' for usage."
    exit 1
    ;;
esac
"""
        pkgFile.writeText(content)
        pkgFile.setExecutable(true, false)
        try {
            android.system.Os.chmod(pkgFile.absolutePath, 493)
        } catch (_: Throwable) {
        }
    }

    private fun appendBashrcMotd(rootfs: File) {
        val bashrc = File(rootfs, "root/.bashrc")
        val motd = "\necho \"Use 'pkg install <package>' to install packages.\"\necho \"Run 'pkg --help' for usage.\"\n"
        if (bashrc.exists()) {
            val current = bashrc.readText()
            if (!current.contains("pkg install")) {
                bashrc.appendText(motd)
            }
        } else {
            bashrc.parentFile?.mkdirs()
            bashrc.writeText(motd.trimStart())
        }
    }
}
