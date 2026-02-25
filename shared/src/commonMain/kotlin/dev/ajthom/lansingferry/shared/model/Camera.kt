package dev.ajthom.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Camera(
    val id: String,
    val name: String,
    val streamUrl: String,
    val snapshotUrl: String,
)
