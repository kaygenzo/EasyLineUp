package com.telen.easylineup.repository

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.telen.easylineup.domain.repository.*
import com.telen.easylineup.repository.adapters.impl.*
import com.telen.easylineup.repository.dao.AppDatabase
import com.telen.easylineup.repository.dao.DATABASE_NAME
import org.koin.dsl.module

object RepositoryModule {

    val repositoryModules = module {

        single {
            val builder = Room.databaseBuilder(get(), AppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(migration_1_2())
                    .addMigrations(migration_2_3())
                    .addMigrations(migration_3_4())
                    .addMigrations(migration_4_5())
                    .addMigrations(migration_5_6())
                    .addMigrations(migration_6_7())
                    .addMigrations(migration_7_8())
                    .addMigrations(migration_8_9())
            if(BuildConfig.usePrefilledDatabase) {
                builder.createFromAsset("demo_database")
                        .fallbackToDestructiveMigration()
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

        single<PlayerRepository> { PlayerRepositoryImpl(get()) }
        single<TeamRepository> { TeamRepositoryImpl(get()) }
        single<LineupRepository> { LineupRepositoryImpl(get()) }
        single<TournamentRepository> { TournamentRepositoryImpl(get()) }
        single<PlayerFieldPositionRepository> { PlayerFieldPositionRepositoryImpl(get()) }
    }

    private fun migration_1_2(): Migration {
        return object: Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Players ADD COLUMN positions INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration_2_3(): Migration {
        return object: Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Lineups ADD COLUMN mode INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Teams ADD COLUMN type INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration_3_4(): Migration {
        return object: Migration(3,4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Teams ADD COLUMN main INTEGER NOT NULL DEFAULT 1")
            }
        }
    }

    private fun migration_4_5(): Migration {
        return object: Migration(4,5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Lineups ADD COLUMN roaster TEXT DEFAULT null")
            }
        }
    }

    //delete columns x and y + add new column hash in each table
    private fun migration_5_6(): Migration {
        return object: Migration(5,6) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE IF NOT EXISTS playerFieldPosition_tmp (" +
//                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                        "`playerID` INTEGER NOT NULL, " +
//                        "`lineupID` INTEGER NOT NULL, " +
//                        "`position` INTEGER NOT NULL, " +
//                        "`order` INTEGER NOT NULL, " +
//                        "FOREIGN KEY(`playerID`) REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
//                        "FOREIGN KEY(`lineupID`) REFERENCES `lineups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
//                        ")")
//                // Copy the data
//                database.execSQL(
//                        "INSERT INTO playerFieldPosition_tmp (id, playerID, lineupID, position, order) " +
//                                "SELECT id, playerID, lineupID, position, order FROM playerFieldPosition")
//                // Remove the old table
//                database.execSQL("DROP TABLE playerFieldPosition")
//                // Change the table name to the correct one
//                database.execSQL("ALTER TABLE playerFieldPosition_tmp RENAME TO playerFieldPosition")

                database.execSQL("ALTER TABLE teams ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE players ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE lineups ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE tournaments ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE playerFieldPosition ADD COLUMN hash TEXT DEFAULT null")
            }
        }
    }

    private fun migration_6_7(): Migration {
        return object: Migration(6,7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE playerFieldPosition ADD COLUMN flags INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration_7_8(): Migration {
        return object: Migration(7,8) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("UPDATE playerFieldPosition set flags=1 where position=10")
                database.execSQL("""
                    update playerFieldPosition 
                    set flags=1 
                    where id in (
                        select playerFieldPosition.id 
                        from playerFieldPosition 
                        left join lineups on lineups.id=playerFieldPosition.lineupID 
                        left join teams on lineups.teamID=teams.id 
                        where position=1 and mode=1
                    )
                    """)
            }
        }
    }

    private fun migration_8_9(): Migration {
        return object: Migration(8,9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE players ADD COLUMN pitching INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE players ADD COLUMN batting INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

}