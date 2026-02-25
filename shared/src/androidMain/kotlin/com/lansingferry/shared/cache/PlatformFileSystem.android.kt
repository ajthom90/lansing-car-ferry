package com.lansingferry.shared.cache

import okio.Path
import okio.Path.Companion.toPath

actual fun platformCachePath(): Path {
    return "/data/data/com.lansingferry.android/files/ferry-cache.json".toPath()
}
