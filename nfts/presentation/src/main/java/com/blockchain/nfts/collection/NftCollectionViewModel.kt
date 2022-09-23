package com.blockchain.nfts.collection

import androidx.lifecycle.viewModelScope
import com.blockchain.commonarch.presentation.mvi_v2.ModelConfigArgs
import com.blockchain.commonarch.presentation.mvi_v2.MviViewModel
import com.blockchain.data.DataResource
import com.blockchain.nfts.OPENSEA_URL
import com.blockchain.nfts.collection.navigation.NftCollectionNavigationEvent
import com.blockchain.nfts.domain.service.NftService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NftCollectionViewModel(
    private val nftService: NftService
) : MviViewModel<NftCollectionIntent,
    NftCollectionViewState,
    NftCollectionModelState,
    NftCollectionNavigationEvent,
    ModelConfigArgs.NoArgs>(
    initialState = NftCollectionModelState()
) {
    override fun viewCreated(args: ModelConfigArgs.NoArgs) {
        loadNftCollection()
    }

    override fun reduce(state: NftCollectionModelState): NftCollectionViewState = state.run {
        NftCollectionViewState(
            collection = collection
        )
    }

    override suspend fun handleIntent(modelState: NftCollectionModelState, intent: NftCollectionIntent) {
        when(intent){
            NftCollectionIntent.ExternalShop -> {
                navigate(NftCollectionNavigationEvent.ShopExternal(OPENSEA_URL))
            }
        }
    }

    private fun loadNftCollection() {
        viewModelScope.launch {
            nftService.getNftCollectionForAddress(address = "0xD3799B05bf81F05358fac9e09760Ba35876002b8")
                .collectLatest { dataResource ->
                    updateState {
                        it.copy(
                            collection = if (dataResource is DataResource.Loading && it.collection is DataResource.Data) {
                                // if data is present already - don't show loading
                                it.collection
                            } else {
                                dataResource
                            }
                        )
                    }
                }
        }
    }
}
