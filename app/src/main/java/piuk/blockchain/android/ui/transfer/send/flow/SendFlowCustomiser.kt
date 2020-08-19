package piuk.blockchain.android.ui.transfer.send.flow

import android.content.res.Resources
import piuk.blockchain.android.R
import piuk.blockchain.android.coincore.AssetAction
import piuk.blockchain.android.ui.transfer.send.SendErrorState
import piuk.blockchain.android.ui.transfer.send.SendState

interface SendFlowCustomiser {
    fun selectSourceAddressTitle(state: SendState): String
    fun selectTargetAddressTitle(state: SendState): String
    fun enterAmountTitle(state: SendState): String
    fun confirmTitle(state: SendState): String
    fun confirmCtaText(state: SendState): String
    fun transactionProgressTitle(state: SendState): String
    fun transactionProgressMessage(state: SendState): String
    fun transactionCompleteTitle(state: SendState): String
    fun transactionCompleteMessage(state: SendState): String

    fun setDirectionIcon(state: SendState): Int

    // val targetAccountFilter(state: SendState): (SingleAccount) -> Boolean

    fun errorFlashMessage(state: SendState): String?
}

class SendFlowCustomiserImpl(
    private val resources: Resources
) : SendFlowCustomiser {
    override fun setDirectionIcon(state: SendState): Int {
        return when (state.action) {
            AssetAction.NewSend -> R.drawable.ic_tx_sent
            AssetAction.Deposit -> R.drawable.ic_tx_deposit_arrow
            // AssetAction.Swap -> resources.getString(R.string.common_swap)
            // AssertAction.Sell -> "Sell for"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun selectSourceAddressTitle(state: SendState): String = "Select Source Address"

    override fun selectTargetAddressTitle(state: SendState): String {
        return when (state.action) {
            AssetAction.NewSend -> resources.getString(R.string.common_send)
            AssetAction.Deposit -> resources.getString(R.string.common_deposit)
            // AssetAction.Swap -> resources.getString(R.string.common_swap)
            // AssertAction.Sell -> "Sell for"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun enterAmountTitle(state: SendState): String {
        return when (state.action) {
            AssetAction.NewSend -> resources.getString(
                R.string.send_enter_amount_title, state.sendingAccount.asset.displayTicker
            )
            AssetAction.Deposit -> resources.getString(R.string.tx_title_deposit,
                state.sendingAccount.asset.displayTicker)
            // AssetAction.Swap -> "Swap..."
            // AssertAction.Sell -> "Sell ${state.sendingAccount.asset.displayTicker}"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun confirmTitle(state: SendState): String {
        val amount = state.pendingTx?.amount?.toStringWithSymbol() ?: ""

        return when (state.action) {
            AssetAction.NewSend -> resources.getString(
                R.string.send_confirmation_title, amount
            )
            AssetAction.Deposit -> resources.getString(R.string.common_confirm)
            // AssetAction.Swap -> "Swap ${state.sendingAccount.asset.displayTicker}"
            // AssertAction.Sell -> "Checkout"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun confirmCtaText(state: SendState): String {
        val amount = state.pendingTx?.amount?.toStringWithSymbol() ?: ""

        return when (state.action) {
            AssetAction.NewSend -> resources.getString(
                R.string.send_confirmation_cta_button, amount
            )
            AssetAction.Deposit -> resources.getString(
                R.string.send_confirmation_deposit_cta_button)
            // AssetAction.Swap -> "Execute Trade"
            // AssertAction.Sell ->
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun transactionProgressTitle(state: SendState): String {
        val amount = state.pendingTx?.amount?.toStringWithSymbol() ?: ""

        return when (state.action) {
            AssetAction.NewSend -> resources.getString(
                R.string.send_progress_sending_title, amount
            )
            AssetAction.Deposit -> resources.getString(R.string.send_confirmation_progress_title,
                amount)
            // AssetAction.Swap -> "Execute Trade"
            // AssertAction.Sell -> "Selling ${state.pendingTx.amount.toStringWithSymbol()}"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun transactionProgressMessage(state: SendState): String {
        return when (state.action) {
            AssetAction.NewSend -> resources.getString(R.string.send_progress_sending_subtitle)
            AssetAction.Deposit -> resources.getString(R.string.send_confirmation_progress_message,
                state.sendingAccount.asset.name)
            // AssetAction.Swap -> ""
            // AssertAction.Sell -> "We're completing your sell now."
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun transactionCompleteTitle(state: SendState): String {
        val amount = state.pendingTx?.amount?.toStringWithSymbol() ?: ""

        return when (state.action) {
            AssetAction.NewSend -> resources.getString(
                R.string.send_progress_complete_title, amount
            )
            AssetAction.Deposit -> resources.getString(R.string.send_confirmation_success_title,
                amount)
            // AssetAction.Swap -> "Execute Trade"
            // AssertAction.Sell -> "${state.pendingTx.amount.toStringWithSymbol()} sold"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun transactionCompleteMessage(state: SendState): String {
        return when (state.action) {
            AssetAction.NewSend -> resources.getString(
                R.string.send_progress_complete_subtitle, state.sendingAccount.asset.name
            )
            AssetAction.Deposit -> resources.getString(R.string.send_confirmation_success_message,
                state.sendingAccount.asset.name)
            //  AssetAction.Swap -> "Execute Trade"
            // AssertAction.Sell -> "Your cash is now available in your GBP wallet"
            else -> throw IllegalArgumentException("Action not supported by Send Flow")
        }
    }

    override fun errorFlashMessage(state: SendState): String? {
        require(state.errorState != SendErrorState.NONE)

        return when (state.errorState) {
            SendErrorState.NONE -> null
            SendErrorState.INSUFFICIENT_FUNDS -> resources.getString(
                R.string.send_enter_amount_error_insufficient_funds,
                state.sendingAccount.asset.displayTicker
            )
            SendErrorState.INVALID_AMOUNT -> resources.getString(
                R.string.send_enter_amount_error_invalid_amount,
                state.sendingAccount.asset.displayTicker
            )
            SendErrorState.INVALID_ADDRESS -> resources.getString(
                R.string.send_error_not_valid_asset_address,
                state.sendingAccount.asset.displayTicker
            )
            SendErrorState.ADDRESS_IS_CONTRACT -> resources.getString(
                R.string.send_error_address_is_eth_contract)
            SendErrorState.INVALID_PASSWORD -> resources.getString(
                R.string.send_enter_invalid_password)
            SendErrorState.NOT_ENOUGH_GAS -> resources.getString(
                R.string.send_enter_insufficient_gas)
            SendErrorState.UNEXPECTED_ERROR -> resources.getString(
                R.string.send_enter_unexpected_error)
            SendErrorState.BELOW_MIN_LIMIT -> TODO()
            SendErrorState.ABOVE_MAX_LIMIT -> TODO()
        }
    }
}