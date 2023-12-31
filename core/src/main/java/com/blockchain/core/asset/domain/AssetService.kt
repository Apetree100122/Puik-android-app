package com.blockchain.core.asset.domain

import com.blockchain.api.services.DetailedAssetInformation
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.FiatCurrency
import kotlinx.coroutines.flow.Flow

interface AssetService {
    @Deprecated("not yet implemented", level = DeprecationLevel.ERROR)
    fun getAvailableFiatAssets(
        freshnessStrategy: FreshnessStrategy = FreshnessStrategy.Cached(forceRefresh = true)
    ): Flow<DataResource<List<FiatCurrency>>>

    fun getAssetInformation(
        asset: AssetInfo,
        freshnessStrategy: FreshnessStrategy = FreshnessStrategy.Cached(forceRefresh = true)
    ): Flow<DataResource<DetailedAssetInformation>>
}
