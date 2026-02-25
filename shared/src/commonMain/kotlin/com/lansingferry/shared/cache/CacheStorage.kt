package com.lansingferry.shared.cache

import com.lansingferry.shared.model.FerryInfo
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

    fun save(ferryInfo: FerryInfo) {
        val data = json.encodeToString(ferryInfo)
        fileSystem.sink(cachePath).buffer().use { sink ->
            sink.writeUtf8(data)
        }
    }

    fun load(): FerryInfo? {
        if (!fileSystem.exists(cachePath)) return null
        return try {
            val data = fileSystem.source(cachePath).buffer().use { source ->
                source.readUtf8()
            }
            json.decodeFromString<FerryInfo>(data)
        } catch (e: Exception) {
            null
        }
    }
}
