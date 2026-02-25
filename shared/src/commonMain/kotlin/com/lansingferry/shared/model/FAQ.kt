package com.lansingferry.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class FAQ(
    val question: String,
    val answer: String,
)
