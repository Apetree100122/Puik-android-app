package com.blockchain.serializers

import com.blockchain.koin.kotlinJsonAssetTicker
import info.blockchain.serializers.AssetInfoKSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.koin.dsl.module

val jsonSerializers = module {

    single(kotlinJsonAssetTicker) {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            serializersModule = SerializersModule {
                contextual(BigDecimalSerializer)
                contextual(BigIntSerializer)
                contextual(IsoDateSerializer)
                contextual(KZonedDateTimeSerializer)
                contextual(AssetInfoKSerializer(assetCatalogue = get()))
            }
        }
    }
}
