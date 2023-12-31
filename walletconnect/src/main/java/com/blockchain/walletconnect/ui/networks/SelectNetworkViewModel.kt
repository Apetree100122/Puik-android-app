package com.blockchain.walletconnect.ui.networks

import androidx.lifecycle.viewModelScope
import com.blockchain.coincore.Coincore
import com.blockchain.commonarch.presentation.mvi_v2.ModelConfigArgs
import com.blockchain.commonarch.presentation.mvi_v2.MviViewModel
import com.blockchain.core.chains.EvmNetwork
import com.blockchain.core.chains.ethereum.EthDataManager
import com.blockchain.outcome.doOnFailure
import com.blockchain.outcome.doOnSuccess
import com.blockchain.utils.awaitOutcome
import info.blockchain.balance.CryptoCurrency
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectNetworkViewModel(
    private val coincore: Coincore,
    private val ethDataManager: EthDataManager
) : MviViewModel<
    SelectNetworkIntents,
    SelectNetworkViewState,
    SelectNetworkModelState,
    SelectNetworkNavigationEvent,
    ModelConfigArgs.NoArgs>(
    SelectNetworkModelState()
) {
    override fun viewCreated(args: ModelConfigArgs.NoArgs) {}

    override fun reduce(state: SelectNetworkModelState): SelectNetworkViewState {
        return with(state) {
            SelectNetworkViewState(
                networks = networks,
                selectedNetwork = selectedNetwork
            )
        }
    }

    override suspend fun handleIntent(modelState: SelectNetworkModelState, intent: SelectNetworkIntents) {
        when (intent) {
            is SelectNetworkIntents.LoadSupportedNetworks -> loadSupportedNetworks(intent.preSelectedChainId)
            is SelectNetworkIntents.LoadIconForNetworks -> loadIconsForNetworks(intent.networks, intent.selectedNetwork)
            is SelectNetworkIntents.SelectNetwork -> updateState {
                it.copy(
                    selectedNetwork = it.networks.findByChainId(intent.chainId)
                )
            }
        }
    }

    private suspend fun loadSupportedNetworks(chainIdToSelect: Int) = viewModelScope.launch {
        ethDataManager.supportedNetworks
            .awaitOutcome()
            .doOnSuccess { supportedNetworks ->
                val networks = supportedNetworks.map { evmNetwork -> evmNetwork.toNetworkInfo() }
                val selectedNetwork = networks.findByChainId(chainIdToSelect)
                updateState {
                    it.copy(
                        networks = networks,
                        selectedNetwork = selectedNetwork
                    )
                }
                onIntent(
                    SelectNetworkIntents.LoadIconForNetworks(
                        networks,
                        selectedNetwork
                    )
                )
            }
            .doOnFailure {
                Timber.e(it)
                updateState { state -> state.copy(networks = emptyList()) }
            }
    }

    private fun loadIconsForNetworks(networks: List<NetworkInfo>, selectedNetwork: NetworkInfo?) = updateState {
        it.copy(
            networks = networks.map { network ->
                network.copy(
                    logo = coincore[network.networkTicker]?.currency?.logo
                )
            },
            selectedNetwork = selectedNetwork?.copy(
                logo = coincore[selectedNetwork.networkTicker]?.currency?.logo
            )
        )
    }

    private fun EvmNetwork.toNetworkInfo() =
        NetworkInfo(
            networkTicker = if (networkTicker == CryptoCurrency.MATIC) {
                CryptoCurrency.MATIC_ON_POLYGON
            } else {
                networkTicker
            },
            name = networkName,
            chainId = chainId
        )

    private fun List<NetworkInfo>.findByChainId(chainId: Int) =
        firstOrNull { network ->
            network.chainId == chainId
        }
}
