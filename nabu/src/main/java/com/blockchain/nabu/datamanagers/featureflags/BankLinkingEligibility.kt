package com.blockchain.nabu.datamanagers.featureflags

import android.os.Build
import com.blockchain.featureflags.GatedFeature
import com.blockchain.featureflags.InternalFeatureFlagApi
import com.blockchain.nabu.models.data.BankPartner
import com.blockchain.remoteconfig.FeatureFlag
import io.reactivex.Single

interface BankLinkingEnabledProvider {
    fun supportedBankPartners(): Single<List<BankPartner>>
    fun bankLinkingEnabled(fiatCurrency: String): Single<Boolean>
}

class BankLinkingEnabledProviderImpl(
    private val obFF: FeatureFlag,
    private val internalFlags: InternalFeatureFlagApi
) : BankLinkingEnabledProvider {
    override fun supportedBankPartners(): Single<List<BankPartner>> =
        obFF.enabled.map { ob ->
            val supportedPartners = mutableListOf<BankPartner>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                supportedPartners.add(BankPartner.YODLEE)
            }
            if (ob && internalFlags.isFeatureEnabled(GatedFeature.OPEN_BANKING)) {
                supportedPartners.add(BankPartner.YAPILY)
            }
            supportedPartners
        }

    override fun bankLinkingEnabled(fiatCurrency: String): Single<Boolean> =
        if (fiatCurrency == "EUR" || fiatCurrency == "GBP") {
            obFF.enabled.map { obEnabled ->
                obEnabled && internalFlags.isFeatureEnabled(GatedFeature.OPEN_BANKING)
            }
        } else {
            Single.just(true)
        }
}