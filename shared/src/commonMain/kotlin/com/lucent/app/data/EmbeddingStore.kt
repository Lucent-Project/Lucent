package com.lucent.app.data

import com.lucent.app.platform.PlatformContext
import kotlin.math.sqrt

object EmbeddingStore {

    data class ScoredNote(val noteId: Long, val similarity: Float)

    private data class CachedEntry(
        val updatedAt: Long,
        val dim: Int,
        val vec: FloatArray,
        val norm: Float
    )

    private val cacheLock = Any()
    private val cache = HashMap<String, CachedEntry>()

    private fun cacheKey(model: String, noteId: Long): String = "$model#$noteId"

    fun encode(vec: FloatArray): ByteArray {
        val bytes = ByteArray(vec.size * 4)
        for ((i, f) in vec.withIndex()) {
            val bits = f.toBits()
            bytes[i * 4] = (bits and 0xFF).toByte()
            bytes[i * 4 + 1] = ((bits shr 8) and 0xFF).toByte()
            bytes[i * 4 + 2] = ((bits shr 16) and 0xFF).toByte()
            bytes[i * 4 + 3] = ((bits shr 24) and 0xFF).toByte()
        }
        return bytes
    }

    fun decode(bytes: ByteArray): FloatArray {
        require(bytes.size % 4 == 0) { "embedding byte length ${bytes.size} is not a multiple of 4" }
        return FloatArray(bytes.size / 4) { i ->
            val bits = (bytes[i * 4].toInt() and 0xFF) or
                ((bytes[i * 4 + 1].toInt() and 0xFF) shl 8) or
                ((bytes[i * 4 + 2].toInt() and 0xFF) shl 16) or
                ((bytes[i * 4 + 3].toInt() and 0xFF) shl 24)
            Float.fromBits(bits)
        }
    }

    suspend fun store(
        db: AppDatabase,
        noteId: Long,
        model: String,
        vec: FloatArray,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        db.noteEmbeddingDao.upsert(
            NoteEmbedding(noteId = noteId, model = model, dim = vec.size, vec = encode(vec), updatedAt = updatedAt)
        )
        val n = norm(vec)
        synchronized(cacheLock) {
            cache[cacheKey(model, noteId)] = CachedEntry(updatedAt, vec.size, vec, n)
        }
    }

    suspend fun store(
        context: PlatformContext,
        noteId: Long,
        model: String,
        vec: FloatArray,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        store(createAppDatabase(context), noteId, model, vec, updatedAt)
    }

    suspend fun search(db: AppDatabase, queryVec: FloatArray, model: String, topK: Int): List<ScoredNote> {
        if (topK <= 0 || queryVec.isEmpty()) return emptyList()
        val queryNorm = norm(queryVec)
        if (queryNorm == 0f) return emptyList()

        return db.noteEmbeddingDao.getForModel(model)
            .mapNotNull { row ->
                if (row.vec.size != row.dim * 4) return@mapNotNull null
                val key = cacheKey(model, row.noteId)
                val cached = synchronized(cacheLock) { cache[key] }
                val entry = if (cached != null && cached.updatedAt == row.updatedAt && cached.dim == row.dim && cached.vec.size == row.dim) {
                    cached
                } else {
                    val decoded = try {
                        decode(row.vec)
                    } catch (_: IllegalArgumentException) {
                        return@mapNotNull null
                    }
                    val fresh = CachedEntry(row.updatedAt, row.dim, decoded, norm(decoded))
                    synchronized(cacheLock) { cache[key] = fresh }
                    fresh
                }
                if (entry.vec.size != queryVec.size || entry.norm == 0f) return@mapNotNull null
                val similarity = cosineSimilarityWithNorms(queryVec, entry.vec, queryNorm, entry.norm)
                ScoredNote(row.noteId, similarity)
            }
            .sortedByDescending { it.similarity }
            .take(topK)
    }

    suspend fun search(context: PlatformContext, queryVec: FloatArray, model: String, topK: Int): List<ScoredNote> =
        search(createAppDatabase(context), queryVec, model, topK)

    suspend fun delete(db: AppDatabase, noteId: Long, model: String) {
        db.noteEmbeddingDao.delete(noteId, model)
        synchronized(cacheLock) { cache.remove(cacheKey(model, noteId)) }
    }

    suspend fun delete(context: PlatformContext, noteId: Long, model: String) {
        delete(createAppDatabase(context), noteId, model)
    }

    suspend fun deleteAllForModel(db: AppDatabase, model: String) {
        db.noteEmbeddingDao.deleteAllForModel(model)
        synchronized(cacheLock) {
            val prefix = "$model#"
            val keys = cache.keys.filter { it.startsWith(prefix) }
            keys.forEach { cache.remove(it) }
        }
    }

    suspend fun deleteAllForModel(context: PlatformContext, model: String) {
        deleteAllForModel(createAppDatabase(context), model)
    }

    suspend fun clearAll(db: AppDatabase) {
        db.noteEmbeddingDao.clearAll()
        synchronized(cacheLock) { cache.clear() }
    }

    suspend fun clearAll(context: PlatformContext) {
        clearAll(createAppDatabase(context))
    }

    private fun norm(v: FloatArray): Float {
        var sumSq = 0.0
        for (x in v) sumSq += x.toDouble() * x.toDouble()
        return sqrt(sumSq).toFloat()
    }

    private fun cosineSimilarityWithNorms(a: FloatArray, b: FloatArray, normA: Float, normB: Float): Float {
        if (normA == 0f || normB == 0f) return 0f
        var dot = 0.0
        for (i in a.indices) {
            dot += a[i].toDouble() * b[i].toDouble()
        }
        return (dot / (normA.toDouble() * normB.toDouble())).toFloat()
    }
}
