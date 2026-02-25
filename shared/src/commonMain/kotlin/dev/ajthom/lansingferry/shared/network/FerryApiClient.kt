package dev.ajthom.lansingferry.shared.network

import dev.ajthom.lansingferry.shared.model.raw.RawFerryInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class FerryApiClient(
    private val httpClient: HttpClient = createHttpClient(),
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchFerryInfo(): RawFerryInfo {
        return httpClient.get("$baseUrl/ferry-info.json").body()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://cdn.jsdelivr.net/gh/ajthom90/lansing-car-ferry@main/data"
    }
}
