package com.blockchain.logging.data.koin

import com.blockchain.koin.embraceLogger
import com.blockchain.logging.EventLogger
import com.blockchain.logging.ILogger
import com.blockchain.logging.MomentLogger
import com.blockchain.logging.NullLogger
import com.blockchain.logging.RemoteLogger
import com.blockchain.logging.data.BuildConfig
import com.blockchain.logging.data.CompoundRemoteLogger
import com.blockchain.logging.data.EmbraceMomentLogger
import com.blockchain.logging.data.EmbraceRemoteLogger
import com.blockchain.logging.data.FirebaseRemoteLogger
import com.blockchain.logging.data.InjectableLogging
import com.blockchain.logging.data.TimberLogger
import org.koin.dsl.bind
import org.koin.dsl.module

val loggingModule = module {
    single {
        CompoundRemoteLogger(
            listOf(
                FirebaseRemoteLogger(),
                get(embraceLogger)
            ),
            get()
        )
    }.bind(RemoteLogger::class)

    single {
        if (BuildConfig.DEBUG) {
            TimberLogger()
        } else {
            NullLogger
        }
    }.bind(ILogger::class)

    factory {
        InjectableLogging(get())
    }.bind(EventLogger::class)

    single<MomentLogger> {
        EmbraceMomentLogger
    }

    single(embraceLogger) {
        EmbraceRemoteLogger(get())
    }.bind(RemoteLogger::class)
}
