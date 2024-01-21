/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.telen.easylineup.repository.model.RoomLineup
import com.telen.easylineup.repository.model.RoomPlayer
import com.telen.easylineup.repository.model.RoomPlayerFieldPosition
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import com.telen.easylineup.repository.model.RoomTeam
import com.telen.easylineup.repository.model.RoomTile
import com.telen.easylineup.repository.model.RoomTournament

const val DATABASE_NAME = "easylineup_database"

@Database(
    entities = [RoomPlayer::class, RoomTeam::class, RoomLineup::class,
        RoomPlayerFieldPosition::class, RoomTournament::class, RoomTile::class,
        RoomPlayerNumberOverlay::class], version = 17, exportSchema = true
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun lineupDao(): LineupDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun playerFieldPositionsDao(): PlayerFieldPositionsDao
    abstract fun tilesDao(): TilesDao
    abstract fun playerNumberOverlaysDao(): PlayerNumberOverlayDao
}
