package com.blockchain.nabu.models.responses.tokenresponse

import kotlinx.serialization.Serializable

@Serializable
data class NabuSessionTokenResponse(
    val id: String,
    val userId: String,
    val token: String,
    val isActive: Boolean,
    val expiresAt: String,
    val insertedAt: String,
    val updatedAt: String? = ""
) {
    val authHeader
        get() = "Bearer $token"
}
