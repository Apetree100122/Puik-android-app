package com.blockchain.core.asset.data

import com.blockchain.api.assetdiscovery.data.AssetInformationDto
import com.blockchain.api.services.DetailedAssetInformation
import com.blockchain.core.asset.data.dataresources.AssetInformationStore
import com.blockchain.core.asset.domain.AssetService
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import com.blockchain.data.FreshnessStrategy.Companion.withKey
import com.blockchain.store.mapData
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.FiatCurrency
import kotlinx.coroutines.flow.Flow

class AssetRepository(
    private val assetInformationStore: AssetInformationStore
) : AssetService {
    override fun getAvailableFiatAssets(
        freshnessStrategy: FreshnessStrategy
    ): Flow<DataResource<List<FiatCurrency>>> {
        TODO("Not yet implemented")
    }

    override fun getAssetInformation(
        asset: AssetInfo,
        freshnessStrategy: FreshnessStrategy
    ): Flow<DataResource<DetailedAssetInformation>> {
        return assetInformationStore
            .stream(
                freshnessStrategy.withKey(AssetInformationStore.Key(asset.networkTicker))
            )
            .mapData {
                it.toAssetInfo()
            }
    }

    private fun AssetInformationDto.toAssetInfo(): DetailedAssetInformation =
        DetailedAssetInformation(
            description = description.orEmpty(),
            website = website.orEmpty(),
            whitepaper = whitepaper.orEmpty()
        )
}
