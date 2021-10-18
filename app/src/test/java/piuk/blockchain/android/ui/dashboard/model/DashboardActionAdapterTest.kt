package piuk.blockchain.android.ui.dashboard.model

import com.blockchain.android.testutils.rxInit
import com.blockchain.featureflags.InternalFeatureFlagApi
import com.blockchain.nabu.datamanagers.CustodialWalletManager
import com.blockchain.nabu.datamanagers.custodialwalletimpl.PaymentMethodType
import com.blockchain.nabu.models.data.BankPartner
import com.blockchain.nabu.models.data.LinkBankTransfer
import com.blockchain.nabu.models.data.YodleeAttributes
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.rxjava3.core.Single

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.blockchain.coincore.AssetAction
import com.blockchain.coincore.FiatAccount
import com.blockchain.coincore.fiat.LinkedBankAccount
import com.blockchain.coincore.fiat.LinkedBanksFactory
import piuk.blockchain.android.ui.settings.LinkablePaymentMethods

class DashboardActionAdapterTest {

    private lateinit var actionAdapter: DashboardActionAdapter
    private val linkedBanksFactory: LinkedBanksFactory = mock()
    private val custodialWalletManager: CustodialWalletManager = mock()
    private val model: DashboardModel = mock()
    private val targetFiatAccount: FiatAccount = mock {
        on { fiatCurrency }.thenReturn("USD")
    }

    private val internalFlags: InternalFeatureFlagApi = mock()

    @get:Rule
    val rx = rxInit {
        ioTrampoline()
        computationTrampoline()
        mainTrampoline()
    }

    @Before
    fun setUp() {
        actionAdapter = DashboardActionAdapter(
            coincore = mock(),
            payloadManager = mock(),
            custodialWalletManager = custodialWalletManager,
            linkedBanksFactory = linkedBanksFactory,
            crashLogger = mock(),
            analytics = mock(),
            simpleBuyPrefs = mock(),
            featureFlag = mock(),
            currencyPrefs = mock(),
            userIdentity = mock(),
            exchangeRates = mock()
        )
    }

    @Test
    fun `for both available methods with no available bank transfer banks, chooser should be triggered`() {
        whenever(linkedBanksFactory.eligibleBankPaymentMethods(any())).thenReturn(
            Single.just(
                setOf(PaymentMethodType.BANK_TRANSFER, PaymentMethodType.BANK_ACCOUNT)
            )
        )
        whenever(linkedBanksFactory.getNonWireTransferBanks()).thenReturn(
            Single.just(
                emptyList()
            )
        )

        whenever(internalFlags.isFeatureEnabled(any())).thenReturn(true)

        actionAdapter.getBankDepositFlow(
            model = model,
            targetAccount = targetFiatAccount,
            action = AssetAction.FiatDeposit,
            shouldLaunchBankLinkTransfer = false
        )

        verify(model).process(
            DashboardIntent.ShowLinkablePaymentMethodsSheet(
                targetFiatAccount,
                LinkablePaymentMethodsForAction.LinkablePaymentMethodsForDeposit(
                    LinkablePaymentMethods(
                        "USD",
                        listOf(
                            PaymentMethodType.BANK_TRANSFER, PaymentMethodType.BANK_ACCOUNT
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `for only bank transfer available with no available bank transfer banks, bank link should launched`() {
        whenever(linkedBanksFactory.eligibleBankPaymentMethods(any())).thenReturn(
            Single.just(
                setOf(PaymentMethodType.BANK_TRANSFER)
            )
        )
        whenever(linkedBanksFactory.getNonWireTransferBanks()).thenReturn(
            Single.just(
                emptyList()
            )
        )
        whenever(custodialWalletManager.linkToABank("USD")).thenReturn(
            Single.just(
                LinkBankTransfer(
                    "123", BankPartner.YODLEE, YodleeAttributes("", "", "")
                )
            )
        )
        whenever(internalFlags.isFeatureEnabled(any())).thenReturn(true)

        actionAdapter.getBankDepositFlow(
            model = model,
            targetAccount = targetFiatAccount,
            action = AssetAction.FiatDeposit,
            shouldLaunchBankLinkTransfer = false
        )

        verify(model).process(
            DashboardIntent.LaunchBankLinkFlow(
                LinkBankTransfer(
                    "123", BankPartner.YODLEE, YodleeAttributes("", "", "")
                ),
                targetFiatAccount,
                AssetAction.FiatDeposit
            )
        )
    }

    @Test
    fun `for only funds with no available bank transfer banks, wire transfer should launched`() {
        whenever(linkedBanksFactory.eligibleBankPaymentMethods(any())).thenReturn(
            Single.just(
                setOf(PaymentMethodType.BANK_ACCOUNT)
            )
        )
        whenever(linkedBanksFactory.getNonWireTransferBanks()).thenReturn(
            Single.just(
                emptyList()
            )
        )

        actionAdapter.getBankDepositFlow(
            model = model,
            targetAccount = targetFiatAccount,
            action = AssetAction.FiatDeposit,
            shouldLaunchBankLinkTransfer = false
        )

        verify(model).process(
            DashboardIntent.ShowBankLinkingSheet(targetFiatAccount)
        )
    }

    @Test
    fun `with 1 available bank transfer, flow should be launched and wire transfer should get ignored`() {
        val linkedBankAccount: LinkedBankAccount = mock {
            on { currency }.thenReturn("USD")
        }
        whenever(linkedBanksFactory.eligibleBankPaymentMethods(any())).thenReturn(
            Single.just(
                setOf(PaymentMethodType.BANK_ACCOUNT, PaymentMethodType.BANK_TRANSFER)
            )
        )
        whenever(linkedBanksFactory.getNonWireTransferBanks()).thenReturn(
            Single.just(
                listOf(
                    linkedBankAccount
                )
            )
        )

        whenever(internalFlags.isFeatureEnabled(any())).thenReturn(true)

        actionAdapter.getBankDepositFlow(
            model = model,
            targetAccount = targetFiatAccount,
            action = AssetAction.FiatDeposit,
            shouldLaunchBankLinkTransfer = false
        )

        verify(model).process(any<DashboardIntent.UpdateLaunchDialogFlow>())
    }

    @Test
    fun `if linked bank should launched then wire transfer should get ignored and link bank should be launched`() {
        whenever(linkedBanksFactory.eligibleBankPaymentMethods(any())).thenReturn(
            Single.just(
                setOf(PaymentMethodType.BANK_ACCOUNT, PaymentMethodType.BANK_TRANSFER)
            )
        )
        whenever(linkedBanksFactory.getNonWireTransferBanks()).thenReturn(
            Single.just(
                emptyList()
            )
        )

        whenever(custodialWalletManager.linkToABank("USD")).thenReturn(
            Single.just(
                LinkBankTransfer(
                    "123", BankPartner.YODLEE, YodleeAttributes("", "", "")
                )
            )
        )

        whenever(internalFlags.isFeatureEnabled(any())).thenReturn(true)

        actionAdapter.getBankDepositFlow(
            model = model,
            targetAccount = targetFiatAccount,
            action = AssetAction.FiatDeposit,
            shouldLaunchBankLinkTransfer = true
        )

        verify(model).process(
            DashboardIntent.LaunchBankLinkFlow(
                LinkBankTransfer(
                    "123", BankPartner.YODLEE, YodleeAttributes("", "", "")
                ),
                targetFiatAccount,
                AssetAction.FiatDeposit
            )
        )
    }
}