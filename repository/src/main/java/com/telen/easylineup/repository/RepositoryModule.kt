/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.tiles.TileType
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TilesRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.repository.adapters.impl.LineupRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.PlayerFieldPositionRepositoryImpl
import com.telen.easylineup.repository.adapters.impl.PlayerRepositoryImpl
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
                .addMigrations(migration1To2())
                .addMigrations(migration2To3())
                .addMigrations(migration3To4())
                .addMigrations(migration4To5())
                .addMigrations(migration5To6())
                .addMigrations(migration6To7())
                .addMigrations(migration7To8())
                .addMigrations(migration8To9())
                .addMigrations(migration9To10())
                .addMigrations(migration10To11())
                .addMigrations(migration11To12())
                .addMigrations(migration12To13())
                .addMigrations(migration13To14())
                .addMigrations(migration14To15())
                .addMigrations(migration15To16())
                .addMigrations(migration16To17())
            if (BuildConfig.usePrefilledDatabase) {
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
        single {
            val database: AppDatabase = get()
            database.tilesDao()
        }
        single {
            val database: AppDatabase = get()
            database.playerNumberOverlaysDao()
        }

        single<PlayerRepository> { PlayerRepositoryImpl(get(), get()) }
        single<TeamRepository> { TeamRepositoryImpl(get()) }
        single<LineupRepository> { LineupRepositoryImpl(get()) }
        single<TournamentRepository> { TournamentRepositoryImpl(get()) }
        single<PlayerFieldPositionRepository> { PlayerFieldPositionRepositoryImpl(get()) }
        single<TilesRepository> { TileRepositoryImpl(get()) }
    }

    private fun migration1To2(): Migration {
        return object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database
                    .execSQL("ALTER TABLE Players ADD COLUMN positions INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration2To3(): Migration {
        return object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Lineups ADD COLUMN mode INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE Teams ADD COLUMN type INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration3To4(): Migration {
        return object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Teams ADD COLUMN main INTEGER NOT NULL DEFAULT 1")
            }
        }
    }

    private fun migration4To5(): Migration {
        return object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Lineups ADD COLUMN roaster TEXT DEFAULT null")
            }
        }
    }

    // delete columns x and y + add new column hash in each table
    private fun migration5To6(): Migration {
        return object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // database.execSQL("CREATE TABLE IF NOT EXISTS playerFieldPosition_tmp (" +
                // "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                // "`playerID` INTEGER NOT NULL, " +
                // "`lineupID` INTEGER NOT NULL, " +
                // "`position` INTEGER NOT NULL, " +
                // "`order` INTEGER NOT NULL, " +
                // "FOREIGN KEY(`playerID`) REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                // "FOREIGN KEY(`lineupID`) REFERENCES `lineups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                // ")")
                // // Copy the data
                // database.execSQL(
                // "INSERT INTO playerFieldPosition_tmp (id, playerID, lineupID, position, order) " +
                // "SELECT id, playerID, lineupID, position, order FROM playerFieldPosition")
                // // Remove the old table
                // database.execSQL("DROP TABLE playerFieldPosition")
                // // Change the table name to the correct one
                // database.execSQL("ALTER TABLE playerFieldPosition_tmp RENAME TO playerFieldPosition")

                database.execSQL("ALTER TABLE teams ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE players ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE lineups ADD COLUMN hash TEXT DEFAULT null")
                database.execSQL("ALTER TABLE tournaments ADD COLUMN hash TEXT DEFAULT null")
                database
                    .execSQL("ALTER TABLE playerFieldPosition ADD COLUMN hash TEXT DEFAULT null")
            }
        }
    }

    private fun migration6To7(): Migration {
        return object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE playerFieldPosition ADD COLUMN flags INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }

    private fun migration7To8(): Migration {
        return object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // database.execSQL("UPDATE playerFieldPosition set flags=1 where position=10")
                database.execSQL(
                    """
                    update playerFieldPosition
                    set flags=1
                    where id in (
                        select playerFieldPosition.id
                        from playerFieldPosition
                        left join lineups on lineups.id=playerFieldPosition.lineupID
                        left join teams on lineups.teamID=teams.id
                        where position=1 and mode=1
                    )
                    """
                )
            }
        }
    }

    private fun migration8To9(): Migration {
        return object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database
                    .execSQL("ALTER TABLE players ADD COLUMN pitching INTEGER NOT NULL DEFAULT 0")
                database
                    .execSQL("ALTER TABLE players ADD COLUMN batting INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration9To10(): Migration {
        return object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS tiles (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`position` INTEGER NOT NULL, " +
                            "`type` INTEGER NOT NULL, " +
                            "`enabled` INTEGER NOT NULL default 1)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tiles_position` ON tiles (`position`)"
                )
                database.execSQL(
                    "INSERT INTO tiles (id, position, type) VALUES (1, 0, ${TileType.BETA.type})")
                database.execSQL(
                    "INSERT INTO tiles (id, position, type) " +
                            "VALUES (2, 1, ${TileType.TEAM_SIZE.type})"
                )
                database.execSQL(
                    "INSERT INTO tiles (id, position, type) " +
                            "VALUES (3, 2, ${TileType.MOST_USED_PLAYER.type})"
                )
                database.execSQL(
                    "INSERT INTO tiles (id, position, type) " +
                            "VALUES (4, 3, ${TileType.LAST_LINEUP.type})"
                )
            }
        }
    }

    private fun migration10To11(): Migration {
        return object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS playerNumberOverlay (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`lineupID` INTEGER NOT NULL, " +
                            "`playerID` INTEGER NOT NULL, " +
                            "`number` INTEGER NOT NULL, " +
                            "`hash` TEXT, " +
                            "FOREIGN KEY(`playerID`) REFERENCES `players`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE , " +
                            "FOREIGN KEY(`lineupID`) REFERENCES `lineups`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_playerNumberOverlay_number` " +
                            "ON playerNumberOverlay (`number`)"
                )

                // update player indexes
                database.execSQL("DROP INDEX IF EXISTS index_players_name_licenseNumber")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_players_name_licenseNumber_teamID` " +
                            "ON players (`name`, `licenseNumber`, `teamID`)"
                )

                // update lineups indexes
                database.execSQL("DROP INDEX IF EXISTS index_lineups_name")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_lineups_name_teamID_tournamentID` " +
                            "ON lineups (`name`, `teamID`, `tournamentID`)"
                )

                // update playerFieldPosition indexes
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_playerFieldPosition_playerID_lineupID` " +
                            "ON playerFieldPosition (`playerID`, `lineupID`)"
                )
            }
        }
    }

    private fun migration11To12(): Migration {
        return object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "INSERT INTO tiles (id, position, type, enabled) " +
                            "VALUES (5, 4, ${TileType.LAST_PLAYER_NUMBER.type}, 1)"
                )
            }
        }
    }

    private fun migration12To13(): Migration {
        return object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database
                    .execSQL("ALTER TABLE lineups ADD COLUMN eventTime INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    private fun migration13To14(): Migration {
        return object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE players ADD COLUMN email TEXT DEFAULT null")
                database.execSQL("ALTER TABLE players ADD COLUMN phone TEXT DEFAULT null")
            }
        }
    }

    private fun migration14To15(): Migration {
        return object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE lineups ADD COLUMN " +
                            "strategy INTEGER NOT NULL DEFAULT ${TeamStrategy.STANDARD.id}"
                )
                database.execSQL(
                    "ALTER TABLE lineups ADD COLUMN extraHitters INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }

    private fun migration15To16(): Migration {
        return object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE players ADD COLUMN sex INTEGER NOT NULL DEFAULT ${Sex.UNKNOWN.id}"
                )
            }
        }
    }

    private fun migration16To17(): Migration {
        return object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE tournaments ADD COLUMN startTime INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE tournaments ADD COLUMN endTime INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL("ALTER TABLE tournaments ADD COLUMN address TEXT DEFAULT null")
            }
        }
    }
}
