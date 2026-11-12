/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository

import androidx.room.Room
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TilesRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.repository.adapters.impl.LineupRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.PlayerFieldPositionRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.PlayerNumberOverlayRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.PlayerRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.RoomTransactionRunner
import com.telen.easylineup.repository.adapters.impl.TeamRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.TileRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.TournamentRepositoryImpl
import com.telen.easylineup.repository.dao.AppDatabase
import com.telen.easylineup.repository.dao.DATABASE_NAME
import org.koin.dsl.module

object RepositoryModule {
    val repositoryModules = module {
        single {
            val builder = Room.databaseBuilder(get(), AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(*Migrations.all)
            if (BuildConfig.usePrefilledDatabase) {
                builder.createFromAsset("demo_database")
                    .fallbackToDestructiveMigration(dropAllTables = true)
            }
            builder.build()
        }

        single {
            val database: AppDatabase = get()
            database.playerDao()
        }
        single {
            val database: AppDatabase = get()
            database.tournamentDao()
        }
        single {
            val database: AppDatabase = get()
            database.teamDao()
        }
        single {
            val database: AppDatabase = get()
            database.lineupDao()
        }
        single {
            val database: AppDatabase = get()
            database.playerFieldPositionsDao()
        }
        single {
            val database: AppDatabase = get()
            database.tilesDao()
        }
        single {
            val database: AppDatabase = get()
            database.playerNumberOverlaysDao()
        }

        single<PlayerRepository> { PlayerRepositoryImpl(get()) }
        single<PlayerNumberOverlayRepository> { PlayerNumberOverlayRepositoryImpl(get(), get()) }
        single<TeamRepository> { TeamRepositoryImpl(get()) }
        single<LineupRepository> { LineupRepositoryImpl(get()) }
        single<TournamentRepository> { TournamentRepositoryImpl(get()) }
        single<PlayerFieldPositionRepository> { PlayerFieldPositionRepositoryImpl(get()) }
        single<TilesRepository> { TileRepositoryImpl(get()) }
        single<TransactionRunner> { RoomTransactionRunner(get()) }
    }
}
