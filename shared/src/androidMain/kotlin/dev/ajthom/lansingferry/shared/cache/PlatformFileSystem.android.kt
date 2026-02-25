package dev.ajthom.lansingferry.shared.cache

import okio.Path
import okio.Path.Companion.toPath

actual fun platformCachePath(): Path {
    return "/data/data/dev.ajthom.lansingferry/files/ferry-cache.json".toPath()
}
