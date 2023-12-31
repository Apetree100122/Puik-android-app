package piuk.blockchain.android.ui.dashboard.announcements.rule

import com.blockchain.preferences.WalletStatusPrefs
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.ui.dashboard.announcements.DismissRecorder

class TransferCryptoAnnouncementTest {

    private val dismissRecorder: DismissRecorder = mock()
    private val dismissEntry: DismissRecorder.DismissEntry = mock()
    private val walletStatusPrefs: WalletStatusPrefs = mock()

    private lateinit var subject: TransferCryptoAnnouncement

    @Before
    fun setUp() {
        whenever(dismissRecorder[TransferCryptoAnnouncement.DISMISS_KEY]).thenReturn(dismissEntry)
        whenever(dismissEntry.prefsKey).thenReturn(TransferCryptoAnnouncement.DISMISS_KEY)

        subject =
            TransferCryptoAnnouncement(
                dismissRecorder = dismissRecorder,
                walletStatusPrefs = walletStatusPrefs
            )
    }

    @Test
    fun `should not show, when already shown`() {
        whenever(dismissEntry.isDismissed).thenReturn(true)

        subject.shouldShow()
            .test()
            .assertValue { !it }
            .assertValueCount(1)
            .assertComplete()
    }

    @Test
    fun `should show, when not already shown, wallet is unfunded`() {
        whenever(dismissEntry.isDismissed).thenReturn(false)
        whenever(walletStatusPrefs.isWalletFunded).thenReturn(true)

        whenever(walletStatusPrefs.isWalletFunded).thenReturn(false)

        subject.shouldShow()
            .test()
            .assertValue { it }
            .assertValueCount(1)
            .assertComplete()
    }

    @Test
    fun `should not show, when not already shown, wallet is funded`() {
        whenever(dismissEntry.isDismissed).thenReturn(false)
        whenever(walletStatusPrefs.isWalletFunded).thenReturn(true)

        whenever(walletStatusPrefs.isWalletFunded).thenReturn(true)

        subject.shouldShow()
            .test()
            .assertValue { !it }
            .assertValueCount(1)
            .assertComplete()
    }
}
