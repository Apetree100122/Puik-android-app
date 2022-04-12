package com.blockchain.store_persisters_sqldelight

import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import store.StorePersisterDataQueries

val storePersistersSqlDelightModule = module {

    single<Database> {
        Database(AndroidSqliteDriver(Database.Schema, androidContext(), "store_persister.db"))
    }

    single<StorePersisterDataQueries> {
        get<Database>().storePersisterDataQueries
    }

}