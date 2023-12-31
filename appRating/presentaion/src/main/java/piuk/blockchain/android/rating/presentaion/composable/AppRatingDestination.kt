package piuk.blockchain.android.rating.presentaion.composable

import com.blockchain.commonarch.presentation.mvi_v2.compose.ComposeNavigationDestination

sealed class AppRatingDestination(override val route: String) : ComposeNavigationDestination {
    object Stars : AppRatingDestination("Stars")
    object Feedback : AppRatingDestination("Feedback")
}
