package com.blockchain.chrome.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.blockchain.chrome.composable.MultiAppChrome
import com.blockchain.commonarch.presentation.mvi_v2.compose.composable
import com.blockchain.commonarch.presentation.mvi_v2.compose.navigate
import com.blockchain.home.presentation.navigation.AssetActionsNavigation
import com.blockchain.home.presentation.navigation.HomeDestination
import com.blockchain.home.presentation.navigation.homeGraph

@Composable
fun MultiAppNavHost(
    navController: NavHostController,
    assetActionsNavigation: AssetActionsNavigation,
) {
    NavHost(
        navController = navController,
        startDestination = ChromeDestination.Main.route
    ) {
        // main chrome
        chrome(navController, assetActionsNavigation)

        // home screens
        homeGraph()
    }
}

private fun NavGraphBuilder.chrome(navController: NavHostController, assetActionsNavigation: AssetActionsNavigation) {
    composable(navigationEvent = ChromeDestination.Main) {
        MultiAppChrome(
            assetActionsNavigation = assetActionsNavigation,
            openCryptoAssets = {
                navController.navigate(HomeDestination.CryptoAssets)
            },
            openActivity = {
                navController.navigate(HomeDestination.Activity)
            }
        )
    }
}
