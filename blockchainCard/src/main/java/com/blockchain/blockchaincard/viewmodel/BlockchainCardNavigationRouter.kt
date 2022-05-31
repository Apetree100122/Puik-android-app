package com.blockchain.blockchaincard.viewmodel

import androidx.navigation.NavHostController
import com.blockchain.blockchaincard.domain.models.BlockchainCard
import com.blockchain.blockchaincard.ui.BlockchainCardFragment
import com.blockchain.commonarch.presentation.base.BlockchainActivity
import com.blockchain.commonarch.presentation.mvi_v2.NavigationEvent
import com.blockchain.commonarch.presentation.mvi_v2.compose.ComposeNavigationDestination
import com.blockchain.commonarch.presentation.mvi_v2.compose.ComposeNavigationRouter
import com.blockchain.extensions.exhaustive

class BlockchainCardNavigationRouter(override val navController: NavHostController) :
    ComposeNavigationRouter<BlockchainCardNavigationEvent> {

    override fun route(navigationEvent: BlockchainCardNavigationEvent) {
        var destination: BlockchainCardDestination = BlockchainCardDestination.NoDestination

        @Suppress("IMPLICIT_CAST_TO_ANY")
        when (navigationEvent) {
            is BlockchainCardNavigationEvent.OrderOrLinkCard -> {
                destination = BlockchainCardDestination.OrderOrLinkCardDestination
            }

            is BlockchainCardNavigationEvent.SelectCardForOrder -> {
                destination = BlockchainCardDestination.SelectCardForOrderDestination
            }

            is BlockchainCardNavigationEvent.SeeProductDetails -> {
                destination = BlockchainCardDestination.SeeProductDetailsDestination
            }

            is BlockchainCardNavigationEvent.HideBottomSheet -> {
                navController.popBackStack()
            }

            is BlockchainCardNavigationEvent.CreateCardInProgress -> {
                destination = BlockchainCardDestination.CreateCardInProgressDestination
            }

            is BlockchainCardNavigationEvent.CreateCardSuccess -> {
                navController.popBackStack(BlockchainCardDestination.OrderOrLinkCardDestination.route, true)
                destination = BlockchainCardDestination.CreateCardSuccessDestination
            }

            is BlockchainCardNavigationEvent.CreateCardFailed -> {
                navController.popBackStack(BlockchainCardDestination.SelectCardForOrderDestination.route, false)
                destination = BlockchainCardDestination.CreateCardFailedDestination
            }

            is BlockchainCardNavigationEvent.ManageCard -> {
                /*
                 Since we are navigating into the manage card screen which uses a different view model we must replace
                 the current fragment with a new instance of BlockchainCardFragment that uses the correct args
                */

                val fragmentManager = (navController.context as? BlockchainActivity)?.supportFragmentManager
                val fragmentOld = fragmentManager?.findFragmentByTag(BlockchainCardFragment::class.simpleName)

                fragmentOld?.let {
                    replaceCurrentFragment(
                        containerViewId = fragmentOld.id,
                        fragment = BlockchainCardFragment.newInstance(navigationEvent.card),
                        addToBackStack = false
                    )
                }
            }

            is BlockchainCardNavigationEvent.ManageCardDetails -> {
                destination = BlockchainCardDestination.ManageCardDetailsDestination
            }

            is BlockchainCardNavigationEvent.ChoosePaymentMethod -> {
                destination = BlockchainCardDestination.ChoosePaymentMethodDestination
            }

            is BlockchainCardNavigationEvent.CardDeleted -> {
                finishHostFragment()
            }
        }.exhaustive

        if (destination !is BlockchainCardDestination.NoDestination)
            navController.navigate(destination.route)
    }
}

sealed class BlockchainCardNavigationEvent : NavigationEvent {

    // Order Card
    object OrderOrLinkCard : BlockchainCardNavigationEvent()

    object CreateCardInProgress : BlockchainCardNavigationEvent()

    object CreateCardSuccess : BlockchainCardNavigationEvent()

    object CreateCardFailed : BlockchainCardNavigationEvent()

    object SelectCardForOrder : BlockchainCardNavigationEvent()

    object HideBottomSheet : BlockchainCardNavigationEvent()

    object SeeProductDetails : BlockchainCardNavigationEvent()

    // Manage Card
    data class ManageCard(val card: BlockchainCard) : BlockchainCardNavigationEvent()

    object ManageCardDetails : BlockchainCardNavigationEvent()

    object ChoosePaymentMethod : BlockchainCardNavigationEvent()

    object CardDeleted : BlockchainCardNavigationEvent()
}

sealed class BlockchainCardDestination(override val route: String) : ComposeNavigationDestination {

    object NoDestination : BlockchainCardDestination(route = "")

    object OrderOrLinkCardDestination : BlockchainCardDestination(route = "order_or_link_card")

    object CreateCardInProgressDestination : BlockchainCardDestination(route = "create_card_in_progress")

    object CreateCardSuccessDestination : BlockchainCardDestination(route = "create_card_success")

    object CreateCardFailedDestination : BlockchainCardDestination(route = "create_card_failed")

    object SelectCardForOrderDestination : BlockchainCardDestination(route = "select_card_for_order")

    object SeeProductDetailsDestination : BlockchainCardDestination(route = "product_details")

    object ManageCardDestination : BlockchainCardDestination(route = "manage_card")

    object ManageCardDetailsDestination : BlockchainCardDestination(route = "manage_card_details")

    object ChoosePaymentMethodDestination : BlockchainCardDestination(route = "choose_payment_method")
}
