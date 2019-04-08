package com.telen.easylineup.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Player::class, Team::class, Lineup::class, PlayerFieldPosition::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun lineupDao(): LineupDao
}