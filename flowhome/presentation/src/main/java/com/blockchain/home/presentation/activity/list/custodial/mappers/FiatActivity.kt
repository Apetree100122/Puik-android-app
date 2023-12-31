package com.blockchain.home.presentation.activity.list.custodial.mappers

import androidx.annotation.DrawableRes
import com.blockchain.coincore.FiatActivitySummaryItem
import com.blockchain.coincore.NullCryptoAddress.asset
import com.blockchain.componentlib.utils.TextValue
import com.blockchain.home.presentation.R
import com.blockchain.home.presentation.activity.common.ActivityStackView
import com.blockchain.nabu.datamanagers.TransactionState
import com.blockchain.nabu.datamanagers.TransactionType
import com.blockchain.preferences.CurrencyPrefs
import com.blockchain.unifiedcryptowallet.domain.activity.model.ActivityTextColor
import com.blockchain.utils.toFormattedDate
import org.koin.java.KoinJavaComponent

@DrawableRes internal fun FiatActivitySummaryItem.iconSummary(): Int {
    return when (type) {
        TransactionType.DEPOSIT -> R.drawable.ic_activity_buy
        TransactionType.WITHDRAWAL -> R.drawable.ic_activity_sell
    }
}

internal fun FiatActivitySummaryItem.leadingTitle(): ActivityStackView {
    return ActivityStackView.Text(
        value = TextValue.IntResValue(
            value = when (type) {
                TransactionType.DEPOSIT -> R.string.tx_title_deposited
                TransactionType.WITHDRAWAL -> R.string.tx_title_withdrawn
            },
            args = listOf(asset.displayTicker)
        ),
        style = basicTitleStyle
    )
}

internal fun FiatActivitySummaryItem.leadingSubtitle(): ActivityStackView {
    val color: ActivityTextColor = when (state) {
        TransactionState.FAILED -> ActivityTextColor.Error
        else -> ActivityTextColor.Muted
    }

    return ActivityStackView.Text(
        value = when (state) {
            TransactionState.COMPLETED,
            TransactionState.PENDING -> TextValue.StringValue(date.toFormattedDate())
            TransactionState.FAILED -> TextValue.IntResValue(R.string.activity_state_failed)
        },
        style = basicSubtitleStyle.copy(color = color)
    )
}

private fun FiatActivitySummaryItem.trailingStrikethrough() = when (state) {
    TransactionState.FAILED -> true
    else -> false
}

internal fun FiatActivitySummaryItem.trailingTitle(): ActivityStackView {
    val color: ActivityTextColor = when (state) {
        TransactionState.COMPLETED -> ActivityTextColor.Title
        else -> ActivityTextColor.Muted
    }

    return ActivityStackView.Text(
        value = TextValue.StringValue(value.toStringWithSymbol()),
        style = basicTitleStyle.copy(color = color, strikethrough = trailingStrikethrough())
    )
}

internal fun FiatActivitySummaryItem.trailingSubtitle(): ActivityStackView? {
    return KoinJavaComponent.getKoin().get<CurrencyPrefs>().selectedFiatCurrency.let { selectedFiat ->
        if (currency != selectedFiat) {
            return ActivityStackView.Text(
                value = TextValue.StringValue(fiatValue(selectedFiat).toStringWithSymbol()),
                style = basicSubtitleStyle.copy(strikethrough = trailingStrikethrough())
            )
        } else {
            null
        }
    }
}
