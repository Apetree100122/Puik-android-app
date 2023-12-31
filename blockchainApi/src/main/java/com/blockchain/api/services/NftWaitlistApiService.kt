package com.blockchain.api.services

import com.blockchain.api.nftwaitlist.data.api.NftWaitlistApi
import com.blockchain.api.nftwaitlist.data.model.NftWaitlistDto
import com.blockchain.outcome.Outcome

class NftWaitlistApiService internal constructor(
    private val nftWaitlistApi: NftWaitlistApi
) {
    suspend fun joinNftWaitlist(nftWaitlistDto: NftWaitlistDto): Outcome<Exception, Unit> =
        nftWaitlistApi.joinNftWaitlist(nftWaitlistDto = nftWaitlistDto)
}
