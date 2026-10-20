package com.lucent.app.harness

import com.lucent.app.platform.PlatformContext
import com.lucent.app.platform.appContext
import com.lucent.app.data.DataKeys
import com.lucent.app.data.FileCrypto
import com.lucent.app.data.LocalSecrets
import com.lucent.app.data.secureRandomBytes
import kotlin.concurrent.Volatile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

object HarnessVault {

    private val lock = Any()
    @Volatile private var cachedKey: ByteArray? = null
    @Volatile private var cachedKeyDir: String = ""

    fun bindContext(context: PlatformContext?) {
        if (context == null) return
        try {
            val k = DataKeys.attachmentKey(context.appContext())
            if (k.size == 32) cachedKey = k
        } catch (_: Throwable) {
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun resolveKey(context: PlatformContext? = null): ByteArray {
        if (context != null) {
            try {
                val k = DataKeys.attachmentKey(context.appContext())
                if (k.size == 32) {
                    cachedKey = k
                    return k
                }
            } catch (_: Throwable) {
            }
        }
        val base = runCatching { HarnessRuntime.filesDirPath() }.getOrDefault("")
        val existing = cachedKey
        if (existing != null && cachedKeyDir == base) return existing
        synchronized(lock) {
            val again = cachedKey
            if (again != null && cachedKeyDir == base) return again
            val keyFile = (base.ifBlank { okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString() }.toPath()) / "keys" / "harness.key"
            val loaded = try {
                if (FileSystem.SYSTEM.exists(keyFile)) {
                    val raw = FileSystem.SYSTEM.read(keyFile) { readUtf8() }.trim()
                    val dec = LocalSecrets.decrypt(raw)
                    val bytes = Base64.decode(dec)
                    if (bytes.size == 32) bytes else null
                } else null
            } catch (_: Throwable) {
                null
            }
            val finalKey = loaded ?: secureRandomBytes(32).also { fresh ->
                try {
                    keyFile.parent?.let { FileSystem.SYSTEM.createDirectories(it) }
                    val enc = LocalSecrets.encrypt(Base64.encode(fresh))
                    FileSystem.SYSTEM.write(keyFile) { writeUtf8(enc) }
                } catch (_: Throwable) {
                }
            }
            cachedKey = finalKey
            cachedKeyDir = base
            return finalKey
        }
    }

    fun writeBytes(context: PlatformContext?, file: Path, plain: ByteArray) {
        try {
            file.parent?.let { FileSystem.SYSTEM.createDirectories(it) }
            val keyBytes = resolveKey(context)
            val bytes = FileCrypto.encrypt(plain, keyBytes)
            FileSystem.SYSTEM.write(file) { write(bytes) }
        } catch (_: Throwable) {
        }
    }

    fun readBytes(context: PlatformContext?, file: Path): ByteArray {
        if (!FileSystem.SYSTEM.exists(file)) return ByteArray(0)
        return try {
            val raw = FileSystem.SYSTEM.read(file) { readByteArray() }
            if (!FileCrypto.isEncrypted(FileSystem.SYSTEM, file)) return raw
            val keyBytes = resolveKey(context)
            FileCrypto.decrypt(raw, keyBytes)
        } catch (_: Throwable) {
            ByteArray(0)
        }
    }

    fun write(context: PlatformContext?, file: Path, text: String) {
        writeBytes(context, file, text.toByteArray(Charsets.UTF_8))
    }

    fun read(context: PlatformContext?, file: Path): String {
        val bytes = readBytes(context, file)
        return if (bytes.isEmpty()) "" else String(bytes, Charsets.UTF_8)
    }
}
