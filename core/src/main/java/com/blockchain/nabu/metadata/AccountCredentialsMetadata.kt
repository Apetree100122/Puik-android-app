package com.blockchain.nabu.metadata

import com.blockchain.featureflag.FeatureFlag
import com.blockchain.metadata.MetadataRepository
import com.blockchain.metadata.load
import com.blockchain.metadata.save
import com.blockchain.nabu.models.responses.tokenresponse.NabuOfflineTokenResponse
import com.blockchain.nabu.models.responses.tokenresponse.mapToBlockchainCredentialsMetadata
import com.blockchain.nabu.models.responses.tokenresponse.mapToLegacyCredentials
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import piuk.blockchain.androidcore.utils.extensions.thenMaybe
import piuk.blockchain.androidcore.utils.extensions.thenSingle

class AccountCredentialsMetadata(
    private val metadataRepository: MetadataRepository,
    private val accountMetadataMigrationFF: FeatureFlag
) {
    fun load(): Maybe<CredentialMetadata> {
        return metadataRepository.load<BlockchainAccountCredentialsMetadata>(
            BlockchainAccountCredentialsMetadata.BLOCKCHAIN_CREDENTIALS_METADATA_NODE,
        ).switchIfEmpty(
            // Not found - haven't been created yet.
            Maybe.just(BlockchainAccountCredentialsMetadata.invalid())
        ).flatMap { blockchainMetadata ->
            if (blockchainMetadata.isValid()) {
                Maybe.just(blockchainMetadata)
            } else {
                metadataRepository.load<NabuLegacyCredentialsMetadata>(
                    NabuLegacyCredentialsMetadata.NABU_LEGACY_CREDENTIALS_METADATA_NODE
                ).flatMap { legacyMetadata ->
                    migrate(legacyMetadata, blockchainMetadata).thenMaybe {
                        Maybe.just(legacyMetadata)
                    }
                }
            }
        }
    }

    fun saveAndReturn(tokenResponse: NabuOfflineTokenResponse): Single<CredentialMetadata> {
        return accountMetadataMigrationFF.enabled.flatMap { enabled ->
            if (enabled) {
                saveAndReturnMetadata(tokenResponse)
            } else {
                saveAndReturnLegacyMetadata(tokenResponse)
            }
        }
    }

    private fun saveAndReturnMetadata(tokenResponse: NabuOfflineTokenResponse): Single<CredentialMetadata> {
        val metadata = tokenResponse.mapToBlockchainCredentialsMetadata()
        return metadataRepository.save(
            metadata,
            BlockchainAccountCredentialsMetadata.BLOCKCHAIN_CREDENTIALS_METADATA_NODE
        ).thenSingle {
            Single.just(metadata)
        }
    }

    private fun saveAndReturnLegacyMetadata(tokenResponse: NabuOfflineTokenResponse): Single<CredentialMetadata> {
        val metadata = tokenResponse.mapToLegacyCredentials()
        return metadataRepository.save(
            metadata,
            NabuLegacyCredentialsMetadata.NABU_LEGACY_CREDENTIALS_METADATA_NODE
        ).thenSingle {
            Single.just(metadata)
        }
    }

    private fun migrate(
        legacyMetadata: NabuLegacyCredentialsMetadata,
        blockchainMetadata: BlockchainAccountCredentialsMetadata
    ): Completable {
        return metadataRepository.save(
            BlockchainAccountCredentialsMetadata(
                userId = legacyMetadata.userId,
                exchangeLifetimeToken = blockchainMetadata.exchangeLifetimeToken?.takeIf { it.isNotEmpty() },
                lifetimeToken = legacyMetadata.lifetimeToken,
                exchangeUserId = blockchainMetadata.exchangeUserId?.takeIf { it.isNotEmpty() }
            ),
            BlockchainAccountCredentialsMetadata.BLOCKCHAIN_CREDENTIALS_METADATA_NODE
        )
    }
}
