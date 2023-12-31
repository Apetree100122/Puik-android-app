package com.blockchain.core.user

import com.blockchain.api.services.AssetTag
import com.blockchain.api.services.WatchlistApiService
import com.blockchain.outcome.map
import com.blockchain.utils.rxCompletableOutcome
import com.blockchain.utils.rxSingleOutcome
import info.blockchain.balance.AssetCatalogue
import info.blockchain.balance.Currency
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Deprecated("use WatchlistService")
interface WatchlistDataManager {
    fun getWatchlist(): Single<Watchlist>
    fun addToWatchlist(asset: Currency, tags: List<AssetTag>): Single<WatchlistInfo>
    fun removeFromWatchList(asset: Currency, tags: List<AssetTag>): Completable
    fun isAssetInWatchlist(asset: Currency): Single<Boolean>
}

class WatchlistDataManagerImpl(
    private val watchlistService: WatchlistApiService,
    private val assetCatalogue: AssetCatalogue
) : WatchlistDataManager {

    override fun isAssetInWatchlist(asset: Currency): Single<Boolean> =
        getWatchlist().map {
            val existingAsset = it.assetMap[asset]
            existingAsset?.let { tags ->
                tags.firstOrNull { assetTag ->
                    assetTag == AssetTag.Favourite
                } != null
            } ?: return@map false
        }

    override fun getWatchlist(): Single<Watchlist> =
        rxSingleOutcome {
            watchlistService.getWatchlist()
                .map { response ->
                    val assetMap = mutableMapOf<Currency, List<AssetTag>>()
                    response.items.forEach { item ->
                        assetCatalogue.fromNetworkTicker(item.asset)?.let { currency ->
                            assetMap[currency] = item.tags.map { tagItem ->
                                AssetTag.fromString(tagItem.tag)
                            }
                        }
                    }
                    Watchlist(assetMap)
                }
        }

    override fun addToWatchlist(asset: Currency, tags: List<AssetTag>): Single<WatchlistInfo> =
        rxSingleOutcome {
            watchlistService.addToWatchlist(asset.networkTicker)
                .map { response ->
                    WatchlistInfo(
                        asset,
                        response.tags.map { tagItem ->
                            AssetTag.fromString(tagItem.tag)
                        }
                    )
                }
        }

    override fun removeFromWatchList(asset: Currency, tags: List<AssetTag>): Completable =
        rxCompletableOutcome {
            watchlistService.removeFromWatchlist(asset.networkTicker)
        }
}

data class Watchlist(
    val assetMap: Map<Currency, List<AssetTag>>
)

data class WatchlistInfo(
    val asset: Currency,
    val currentTags: List<AssetTag>
)
