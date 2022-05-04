package com.blockchain.core.user

import com.blockchain.api.services.ContactPreference
import com.blockchain.api.services.ContactPreferenceUpdate
import com.blockchain.api.services.LatestTermsAndConditions
import com.blockchain.api.services.NabuUserService
import com.blockchain.auth.AuthHeaderProvider
import com.blockchain.core.common.caching.TimedCacheRequest
import com.blockchain.nabu.models.responses.nabu.KycTiers
import com.blockchain.nabu.service.TierService
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface NabuUserDataManager {

    fun tiers(): Single<KycTiers>

    fun saveUserInitialLocation(countryIsoCode: String, stateIsoCode: String?): Completable

    fun getLatestTermsAndConditions(): Single<LatestTermsAndConditions>

    fun signLatestTermsAndConditions(): Completable

    fun getContactPreferences(): Single<List<ContactPreference>>

    fun updateContactPreferences(updates: List<ContactPreferenceUpdate>): Completable
}

class NabuUserDataManagerImpl(
    private val nabuUserService: NabuUserService,
    private val authenticator: AuthHeaderProvider,
    private val tierService: TierService
) : NabuUserDataManager {

    private val refresh: () -> Single<KycTiers> = {
        tierService.tiers()
    }

    private val cacheRequest: TimedCacheRequest<KycTiers> by lazy {
        TimedCacheRequest(
            cacheLifetimeSeconds = TIERS_CACHING_LIFETIME_SECS,
            refreshFn = refresh
        )
    }

    override fun tiers(): Single<KycTiers> = cacheRequest.getCachedSingle()

    override fun saveUserInitialLocation(countryIsoCode: String, stateIsoCode: String?): Completable =
        authenticator.getAuthHeader().map {
            nabuUserService.saveUserInitialLocation(
                it,
                countryIsoCode,
                stateIsoCode
            )
        }.flatMapCompletable { it }

    override fun getLatestTermsAndConditions(): Single<LatestTermsAndConditions> =
        authenticator.getAuthHeader().flatMap {
            nabuUserService.getLatestTermsAndConditions(it)
        }

    override fun signLatestTermsAndConditions(): Completable =
        authenticator.getAuthHeader().flatMapCompletable {
            nabuUserService.signLatestTermsAndConditions(it)
        }

    override fun getContactPreferences(): Single<List<ContactPreference>> =
        authenticator.getAuthHeader().flatMap {
            nabuUserService.getContactPreferences(it)
        }

    override fun updateContactPreferences(updates: List<ContactPreferenceUpdate>) =
        authenticator.getAuthHeader().flatMapCompletable {
            nabuUserService.updateContactPreferences(it, updates)
        }
}

private const val TIERS_CACHING_LIFETIME_SECS = 100L
