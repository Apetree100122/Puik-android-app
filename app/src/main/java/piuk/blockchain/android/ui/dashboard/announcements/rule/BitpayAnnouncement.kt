package piuk.blockchain.android.ui.dashboard.announcements.rule

import androidx.annotation.VisibleForTesting
import com.blockchain.preferences.WalletStatusPrefs
import com.blockchain.walletmode.WalletMode
import io.reactivex.rxjava3.core.Single
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.dashboard.announcements.AnnouncementHost
import piuk.blockchain.android.ui.dashboard.announcements.AnnouncementRule
import piuk.blockchain.android.ui.dashboard.announcements.DismissRecorder
import piuk.blockchain.android.ui.dashboard.announcements.DismissRule
import piuk.blockchain.android.ui.dashboard.announcements.StandardAnnouncementCard

class BitpayAnnouncement(
    dismissRecorder: DismissRecorder,
    val walletStatusPrefs: WalletStatusPrefs
) : AnnouncementRule(dismissRecorder) {

    override val associatedWalletModes: List<WalletMode>
        get() = listOf(WalletMode.NON_CUSTODIAL_ONLY)

    override val dismissKey = DISMISS_KEY

    override fun shouldShow(): Single<Boolean> {
        if (dismissEntry.isDismissed) {
            return Single.just(false)
        }

        return Single.just(!walletStatusPrefs.hasMadeBitPayTransaction)
    }

    override fun show(host: AnnouncementHost) {
        host.showAnnouncementCard(
            card = StandardAnnouncementCard(
                name = name,
                dismissRule = DismissRule.CardOneTime,
                dismissEntry = dismissEntry,
                bodyText = R.string.bitpay_announcement_body,
                iconImage = R.drawable.ic_bitpay_logo,
                dismissFunction = {
                    host.dismissAnnouncementCard()
                }
            )
        )
    }

    override val name = "bitpay"

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val DISMISS_KEY = "BitpayAnnouncement_DISMISSED"
    }
}
