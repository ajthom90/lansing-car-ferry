package dev.ajthom.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)
