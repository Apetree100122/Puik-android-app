package com.blockchain.api.nftwaitlist.data.api

import com.blockchain.api.nftwaitlist.data.model.NftWaitlistDto
import com.blockchain.outcome.Outcome
import retrofit2.http.Body
import retrofit2.http.POST

internal interface NftWaitlistApi {
    @POST("explorer-gateway/features/subscribe")
    suspend fun joinNftWaitlist(
        @Body nftWaitlistDto: NftWaitlistDto
    ): Outcome<Exception, Unit>
}
