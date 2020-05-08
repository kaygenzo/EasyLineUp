package com.telen.easylineup.repository.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.telen.easylineup.repository.model.*

@Database(entities = [RoomPlayer::class, RoomTeam::class, RoomLineup::class, RoomPlayerFieldPosition::class, RoomTournament::class], version = 8)
internal abstract class AppDatabase: RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun lineupDao(): LineupDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun playerFieldPositionsDao(): PlayerFieldPositionsDao
}