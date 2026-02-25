package com.lansingferry.shared.network

import com.lansingferry.shared.model.FerryInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class FerryApiClient(
    private val httpClient: HttpClient = createHttpClient(),
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchFerryInfo(): FerryInfo {
        return httpClient.get("$baseUrl/ferry-info.json").body()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://ajthom90.github.io/lansing-car-ferry"
    }
}
