package piuk.blockchain.android.ui.settings.account

import com.blockchain.blockchaincard.domain.models.BlockchainCard
import com.blockchain.blockchaincard.domain.models.BlockchainCardProduct
import com.blockchain.commonarch.presentation.mvi.MviState
import com.blockchain.domain.referral.model.ReferralInfo
import info.blockchain.balance.FiatCurrency

data class AccountState(
    val viewToLaunch: ViewToLaunch = ViewToLaunch.None,
    val accountInformation: AccountInformation? = null,
    val errorState: AccountError = AccountError.NONE,
    val exchangeLinkingState: ExchangeLinkingState = ExchangeLinkingState.UNKNOWN,
    val blockchainCardOrderState: BlockchainCardOrderState = BlockchainCardOrderState.NotEligible,
    val referralInfo: ReferralInfo = ReferralInfo.NotAvailable,
    val featureFlagSet: FeatureFlagSet = FeatureFlagSet(blockchainCardFF = false, dustBalancesFF = false)
) : MviState

sealed class ViewToLaunch {
    object None : ViewToLaunch()
    class DisplayCurrencySelection(
        val selectedCurrency: FiatCurrency,
        val currencyList: List<FiatCurrency>
    ) : ViewToLaunch()
    class TradingCurrencySelection(
        val selectedCurrency: FiatCurrency,
        val currencyList: List<FiatCurrency>
    ) : ViewToLaunch()
}

enum class ExchangeLinkingState {
    UNKNOWN,
    NOT_LINKED,
    LINKED
}

sealed class BlockchainCardOrderState {
    object NotEligible : BlockchainCardOrderState()
    data class Eligible(val cardProducts: List<BlockchainCardProduct>) : BlockchainCardOrderState()
    data class Ordered(
        val cardProducts: List<BlockchainCardProduct>,
        val cards: List<BlockchainCard>,
        val defaultCard: BlockchainCard? = null
    ) : BlockchainCardOrderState()
}

data class AccountInformation(
    val walletId: String,
    val displayCurrency: FiatCurrency,
    val tradingCurrency: FiatCurrency,
    val isChartVibrationEnabled: Boolean
)

enum class AccountError {
    NONE,
    ACCOUNT_INFO_FAIL,
    FIAT_LIST_FAIL,
    ACCOUNT_FIAT_UPDATE_FAIL,
    BLOCKCHAIN_CARD_LOAD_FAIL
}

data class FeatureFlagSet(
    val blockchainCardFF: Boolean,
    val dustBalancesFF: Boolean
)
