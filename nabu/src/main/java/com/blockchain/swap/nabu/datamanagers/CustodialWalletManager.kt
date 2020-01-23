package com.blockchain.swap.nabu.datamanagers

import com.blockchain.swap.nabu.models.simplebuy.BuyLimits
import com.blockchain.swap.nabu.models.simplebuy.SimpleBuyPair
import com.blockchain.swap.nabu.models.simplebuy.SimpleBuyPairs
import com.blockchain.swap.nabu.models.tokenresponse.NabuOfflineTokenResponse
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

// inject an instance of this to provide simple buy and custodial balance/transfer services.
// In the short-term, use aa instance which provides mock data - for development and testing.
// Once the UI and business logic are all working, we can then have NabuDataManager - or something similar -
// implement this, and use koin.bind to have that instance injected instead to provide live data

interface CustodialWalletManager {

    fun getSupportedBuyCurrencies(
        offlineToken: NabuOfflineTokenResponse
    ): Single<SimpleBuyPairs>

    fun getBalanceForAsset(
        offlineToken: NabuOfflineTokenResponse,
        crypto: CryptoCurrency
    ): Single<CryptoValue>
}

// Provide mock data for development and testing etc
internal class MockCustodialWalletManager : CustodialWalletManager {

    override fun getSupportedBuyCurrencies(
        offlineToken: NabuOfflineTokenResponse
    ): Single<SimpleBuyPairs> =
        Single.just(
            SimpleBuyPairs(
                listOf(
                    SimpleBuyPair(pair = "BTC-USD", buyLimits = BuyLimits(100, 5024558)),
                    SimpleBuyPair(pair = "BTC-EUR", buyLimits = BuyLimits(1006, 10000)),
                    SimpleBuyPair(pair = "ETH-EUR", buyLimits = BuyLimits(1005, 10000)),
                    SimpleBuyPair(pair = "BCH-EUR", buyLimits = BuyLimits(1001, 10000))
                )
            )
        )

    override fun getBalanceForAsset(
        offlineToken: NabuOfflineTokenResponse,
        crypto: CryptoCurrency
    ): Single<CryptoValue> =
        when (crypto) {
            CryptoCurrency.BTC -> Single.just(CryptoValue.ZeroBtc)
            CryptoCurrency.ETHER -> Single.just(CryptoValue.ZeroEth)
            CryptoCurrency.BCH -> Single.just(CryptoValue.ZeroBch)
            CryptoCurrency.XLM -> Single.just(CryptoValue.ZeroXlm)
            CryptoCurrency.PAX -> Single.just(CryptoValue.ZeroPax)
            CryptoCurrency.STX -> Single.just(CryptoValue.ZeroStx)
        }
}