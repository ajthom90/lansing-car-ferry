package dev.ajthom.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleRestrictions(
    val allowed: List<String>,
    val prohibited: List<String>,
    val sizeLimits: SizeLimits,
)

@Serializable
data class SizeLimits(
    val heightFeet: Int,
    val lengthFeet: Int,
    val widthFeetInches: String,
    val weightTons: Int,
)
