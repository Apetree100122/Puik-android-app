package piuk.blockchain.android.ui.kyc.invalidcountry

import com.blockchain.featureflag.FeatureFlag
import com.blockchain.metadata.MetadataRepository
import com.blockchain.metadata.save
import com.blockchain.nabu.datamanagers.NabuDataManager
import com.blockchain.nabu.metadata.NabuAccountCredentialsMetadata
import com.blockchain.nabu.metadata.NabuUserCredentialsMetadata
import com.blockchain.nabu.models.responses.tokenresponse.NabuOfflineTokenResponse
import com.blockchain.nabu.models.responses.tokenresponse.mapToNabuAccountMetadata
import com.blockchain.nabu.models.responses.tokenresponse.mapToNabuUserMetadata
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.zipWith
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import piuk.blockchain.android.ui.base.BasePresenter
import timber.log.Timber

@OptIn(InternalSerializationApi::class)
class KycInvalidCountryPresenter(
    private val nabuDataManager: NabuDataManager,
    private val metadataRepository: MetadataRepository,
    private val accountMetadataMigrationFF: FeatureFlag
) : BasePresenter<KycInvalidCountryView>() {

    override fun onViewReady() = Unit

    internal fun onNoThanks() {
        compositeDisposable +=
            recordCountryCode(false)
                .subscribe()
    }

    internal fun onNotifyMe() {
        compositeDisposable +=
            recordCountryCode(true)
                .subscribe()
    }

    private fun recordCountryCode(notifyMe: Boolean): Completable =
        createUserAndStoreInMetadata()
            .flatMapCompletable { (jwt, offlineToken) ->
                nabuDataManager.recordCountrySelection(
                    offlineToken,
                    jwt,
                    view.displayModel.countryCode,
                    view.displayModel.state,
                    notifyMe
                ).subscribeOn(Schedulers.io())
            }
            .doOnError { Timber.e(it) }
            // No need to notify users that this has failed
            .onErrorComplete()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { view.finishPage() }
            .doOnSubscribe { view.showProgressDialog() }
            .doOnTerminate { view.dismissProgressDialog() }

    private fun createUserAndStoreInMetadata(): Single<Pair<String, NabuOfflineTokenResponse>> =
        nabuDataManager.requestJwt()
            .zipWith(accountMetadataMigrationFF.enabled)
            .subscribeOn(Schedulers.io())
            .flatMap { (jwt, enabled) ->
                nabuDataManager.getAuthToken(jwt)
                    .subscribeOn(Schedulers.io())
                    .flatMap { tokenResponse ->
                        if (enabled) {
                            val nabuMetadata = tokenResponse.mapToNabuAccountMetadata()
                            metadataRepository.save(
                                nabuMetadata,
                                NabuAccountCredentialsMetadata.ACCOUNT_CREDENTIALS_METADATA_NODE
                            ).toSingle { jwt to tokenResponse }
                        } else {
                            val nabuMetadata = tokenResponse.mapToNabuUserMetadata()
                            metadataRepository.saveMetadata(
                                nabuMetadata,
                                NabuUserCredentialsMetadata::class.java,
                                NabuUserCredentialsMetadata::class.serializer(),
                                NabuUserCredentialsMetadata.USER_CREDENTIALS_METADATA_NODE
                            ).toSingle { jwt to tokenResponse }
                        }
                    }
            }

    internal fun onProgressCancelled() {
        // Clear outbound requests
        compositeDisposable.clear()
    }
}
