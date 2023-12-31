package com.blockchain.analytics.events

import com.blockchain.analytics.AnalyticsEvent

@Deprecated("Analytics events should be defined near point of use")
sealed class WalletAnalytics(
    override val event: String,
    override val params: Map<String, String> = mapOf()
) : AnalyticsEvent {
    object AddNewWallet : WalletAnalytics("add_new")
    object ArchiveWallet : WalletAnalytics("archive")
    object ChangeDefault : WalletAnalytics("change_default")
    object EditWalletName : WalletAnalytics("edit_name")
    object ShowXpub : WalletAnalytics("show_xpubs")
    object UnArchiveWallet : WalletAnalytics("unarchive")
}
