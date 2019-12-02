package com.telen.easylineup.repository.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.telen.easylineup.repository.model.*

@Database(entities = [Player::class, Team::class, Lineup::class, PlayerFieldPosition::class, Tournament::class], version = 3)
abstract class AppDatabase: RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun lineupDao(): LineupDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun playerFieldPositionsDao(): PlayerFieldPositionsDao
}