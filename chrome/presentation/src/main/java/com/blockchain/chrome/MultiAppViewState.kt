package com.blockchain.chrome

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.blockchain.commonarch.presentation.mvi_v2.ViewState
import com.blockchain.data.DataResource
import com.blockchain.walletmode.WalletMode

data class MultiAppViewState(
    val modeSwitcherOptions: List<WalletMode>,
    val selectedMode: WalletMode,
    val backgroundColors: ChromeBackgroundColors,
    val totalBalance: DataResource<String>,
    val shouldRevealBalance: Boolean,
    val bottomNavigationItems: List<ChromeBottomNavigationItem>
) : ViewState

sealed interface ChromeBottomNavigationItem {
    @get:StringRes
    val name: Int

    @get:DrawableRes
    val iconDefault: Int

    @get:DrawableRes
    val iconSelected: Int

    val route: String

    object Home : ChromeBottomNavigationItem {
        override val name: Int = R.string.chrome_navigation_home
        override val iconDefault: Int = R.drawable.ic_chrome_home_default
        override val iconSelected: Int = R.drawable.ic_chrome_home_selected
        override val route: String = "home"
    }

    object Trade : ChromeBottomNavigationItem {
        override val name: Int = R.string.chrome_navigation_trade
        override val iconDefault: Int = R.drawable.ic_chrome_trade_default
        override val iconSelected: Int = R.drawable.ic_chrome_trade_selected
        override val route: String = "trade"
    }

    object Card : ChromeBottomNavigationItem {
        override val name: Int = R.string.chrome_navigation_card
        override val iconDefault: Int = R.drawable.ic_chrome_card_default
        override val iconSelected: Int = R.drawable.ic_chrome_card_selected
        override val route: String = "card"
    }

    object Nft : ChromeBottomNavigationItem {
        override val name: Int = R.string.chrome_navigation_nft
        override val iconDefault: Int = R.drawable.ic_chrome_nft_default
        override val iconSelected: Int = R.drawable.ic_chrome_nft_selected
        override val route: String = "nft"
    }
}
