package com.blockchain.core.chains.erc20.data.store

import com.blockchain.api.ethereum.evm.BalancesResponse
import com.blockchain.data.DataResource
import com.blockchain.data.KeyedFreshnessStrategy
import com.blockchain.storedatasource.KeyedFlushableDataSource
import kotlinx.coroutines.flow.Flow

interface Erc20L2DataSource : KeyedFlushableDataSource<String> {
    fun streamData(
        request: KeyedFreshnessStrategy<Erc20L2Store.Key>
    ): Flow<DataResource<BalancesResponse>>
}
