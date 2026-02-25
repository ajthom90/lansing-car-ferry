package dev.ajthom.lansingferry.shared.cache

import dev.ajthom.lansingferry.shared.model.raw.RawFerryInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

class CacheStorage(
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val cachePath: Path = platformCachePath(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun save(rawFerryInfo: RawFerryInfo) {
        val data = json.encodeToString(rawFerryInfo)
        fileSystem.sink(cachePath).buffer().use { sink ->
            sink.writeUtf8(data)
        }
    }

    fun load(): RawFerryInfo? {
        if (!fileSystem.exists(cachePath)) return null
        return try {
            val data = fileSystem.source(cachePath).buffer().use { source ->
                source.readUtf8()
            }
            json.decodeFromString<RawFerryInfo>(data)
        } catch (e: Exception) {
            null
        }
    }
}
