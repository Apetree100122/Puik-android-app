package com.blockchain.unifiedcryptowallet.data.balances

import com.blockchain.api.selfcustody.AccountInfo
import com.blockchain.api.selfcustody.CommonResponse
import com.blockchain.api.selfcustody.PubKeyInfo
import com.blockchain.api.selfcustody.SubscriptionInfo
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import com.blockchain.data.FreshnessStrategy.Companion.withKey
import com.blockchain.logging.RemoteLogger
import com.blockchain.outcome.getOrThrow
import com.blockchain.preferences.CurrencyPrefs
import com.blockchain.store.firstOutcome
import com.blockchain.store.flatMapData
import com.blockchain.store.mapData
import com.blockchain.unifiedcryptowallet.domain.balances.NetworkAccountsService
import com.blockchain.unifiedcryptowallet.domain.balances.NetworkBalance
import com.blockchain.unifiedcryptowallet.domain.balances.UnifiedBalanceNotFoundException
import com.blockchain.unifiedcryptowallet.domain.balances.UnifiedBalancesService
import com.blockchain.unifiedcryptowallet.domain.wallet.NetworkWallet
import com.blockchain.unifiedcryptowallet.domain.wallet.PublicKey
import info.blockchain.balance.AssetCatalogue
import info.blockchain.balance.ExchangeRate
import info.blockchain.balance.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

internal class UnifiedBalancesRepository(
    private val networkAccountsService: NetworkAccountsService,
    private val unifiedBalancesSubscribeStore: UnifiedBalancesSubscribeStore,
    private val unifiedBalancesStore: UnifiedBalancesStore,
    private val assetCatalogue: AssetCatalogue,
    private val remoteLogger: RemoteLogger,
    private val currencyPrefs: CurrencyPrefs,
) : UnifiedBalancesService {
    /**
     * Specify those to get the balance of a specific Wallet.
     */
    override fun balances(wallet: NetworkWallet?): Flow<DataResource<List<NetworkBalance>>> {
        return networkAccountsService.allNetworkWallets()
            .flatMapConcat { networkWallets ->
                flow {
                    val pubKeys = networkWallets.filterNot { it.isImported }.associateWith {
                        it.publicKey()
                    }
                    subscribe(pubKeys)
                    emitAll(
                        unifiedBalancesStore.stream(FreshnessStrategy.Cached(true)).mapData { response ->
                            response.balances.filter {
                                if (wallet == null) true
                                else it.currency == wallet.currency.networkTicker && it.account.index == wallet.index &&
                                    it.account.name == wallet.label
                            }.mapNotNull {
                                if (it.price == null) return@mapNotNull null
                                val cc = assetCatalogue.fromNetworkTicker(it.currency)
                                NetworkBalance(
                                    currency = cc ?: return@mapNotNull null,
                                    balance = it.balance?.amount?.let { amount ->
                                        Money.fromMinor(cc, amount)
                                    } ?: return@mapNotNull null,
                                    unconfirmedBalance = it.pending?.amount?.let { amount ->
                                        Money.fromMinor(cc, amount)
                                    } ?: return@mapNotNull null,
                                    exchangeRate = ExchangeRate(
                                        from = cc,
                                        to = currencyPrefs.selectedFiatCurrency,
                                        rate = it.price
                                    )
                                )
                            }
                        }
                    )
                }
            }
    }

    override fun balanceForWallet(
        wallet: NetworkWallet
    ): Flow<DataResource<NetworkBalance>> {
        return balances(wallet).flatMapData {
            it.firstOrNull()?.let { balance ->
                flowOf(DataResource.Data(balance))
            } ?: flowOf(
                DataResource.Error(
                    UnifiedBalanceNotFoundException(
                        currency = wallet.currency.networkTicker,
                        name = wallet.label,
                        index = wallet.index
                    )
                )
            )
        }
    }

    private suspend fun subscribe(networkAccountsPubKeys: Map<NetworkWallet, List<PublicKey>>): CommonResponse {

        val subscriptions = networkAccountsPubKeys.keys.map {
            check(networkAccountsPubKeys[it] != null)
            SubscriptionInfo(
                it.currency.networkTicker,
                AccountInfo(
                    index = it.index,
                    name = it.label
                ),
                pubKeys = networkAccountsPubKeys[it]!!.map { pubKey ->
                    PubKeyInfo(
                        pubKey = pubKey.address,
                        style = pubKey.style,
                        descriptor = pubKey.descriptor
                    )
                }
            )
        }
        return unifiedBalancesSubscribeStore.stream(FreshnessStrategy.Cached(false).withKey(subscriptions))
            .firstOutcome()
            .getOrThrow()
    }
}
