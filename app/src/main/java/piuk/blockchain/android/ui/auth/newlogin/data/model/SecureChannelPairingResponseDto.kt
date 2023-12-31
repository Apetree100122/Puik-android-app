package piuk.blockchain.android.ui.auth.newlogin.data.model

import kotlinx.serialization.Serializable

@Serializable
internal data class SecureChannelPairingResponseDto(
    val channelId: String,
    val pubkey: String,
    val success: Boolean,
    val message: String
)
