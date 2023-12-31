package com.blockchain.coincore.impl

import com.blockchain.coincore.MultipleCurrenciesAccountGroup
import com.blockchain.coincore.SingleAccountList
import com.blockchain.wallet.DefaultLabels
import info.blockchain.balance.Currency

class AllWalletsAccount internal constructor(
    override val accounts: SingleAccountList,
    private val labels: DefaultLabels,
    override val baseCurrency: Currency
) : MultipleCurrenciesAccountGroup {

    override val label: String
        get() = labels.getAllWalletLabel()
}

class AllCustodialWalletsAccount internal constructor(
    override val accounts: SingleAccountList,
    private val labels: DefaultLabels,
    override val baseCurrency: Currency
) : MultipleCurrenciesAccountGroup {
    override val label: String
        get() = labels.getAllCustodialWalletsLabel()
}

class AllNonCustodialWalletsAccount internal constructor(
    override val accounts: SingleAccountList,
    private val labels: DefaultLabels,
    override val baseCurrency: Currency
) : MultipleCurrenciesAccountGroup {
    override val label: String
        get() = labels.getAllNonCustodialWalletsLabel()
}
