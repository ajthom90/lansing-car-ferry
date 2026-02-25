package com.lansingferry.shared.repository

import com.lansingferry.shared.cache.CacheStorage
import com.lansingferry.shared.model.FerryInfo
import com.lansingferry.shared.network.FerryApiClient
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FerryRepository(
    private val apiClient: FerryApiClient = FerryApiClient(),
    private val cacheStorage: CacheStorage = CacheStorage(),
) {
    private var inMemoryCache: FerryInfo? = null
    private var lastFetchTime: Instant? = null

    suspend fun getFerryInfo(): FerryResult {
        // 1. Return in-memory cache immediately if fresh
        inMemoryCache?.let { cached ->
            if (!isCacheStale()) {
                return FerryResult.Success(cached)
            }
        }

        // 2. Load from persistent storage if no in-memory cache
        if (inMemoryCache == null) {
            cacheStorage.load()?.let { persisted ->
                inMemoryCache = persisted
            }
        }

        // 3. Try fetching fresh data from network
        return try {
            val fresh = apiClient.fetchFerryInfo()
            inMemoryCache = fresh
            lastFetchTime = Clock.System.now()
            cacheStorage.save(fresh)
            FerryResult.Success(fresh)
        } catch (e: Exception) {
            // 4. Fall back to cached data on network failure
            inMemoryCache?.let { cached ->
                FerryResult.Success(cached)
            } ?: FerryResult.Error("No cached data available. Please check your connection.")
        }
    }

    suspend fun refresh(): FerryResult {
        return try {
            val fresh = apiClient.fetchFerryInfo()
            inMemoryCache = fresh
            lastFetchTime = Clock.System.now()
            cacheStorage.save(fresh)
            FerryResult.Success(fresh)
        } catch (e: Exception) {
            inMemoryCache?.let { cached ->
                FerryResult.Success(cached)
            } ?: FerryResult.Error("Unable to refresh. Please check your connection.")
        }
    }

    private fun isCacheStale(): Boolean {
        val fetchTime = lastFetchTime ?: return true
        val elapsed = Clock.System.now() - fetchTime
        return elapsed.inWholeMinutes >= CACHE_TTL_MINUTES
    }

    companion object {
        private const val CACHE_TTL_MINUTES = 30
    }
}

sealed class FerryResult {
    data class Success(val data: FerryInfo) : FerryResult()
    data class Error(val message: String) : FerryResult()
}
