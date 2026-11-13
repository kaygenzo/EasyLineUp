/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.telen.easylineup.repository.model.RoomTile

@Dao
internal interface TilesDao {
    @Query("DELETE FROM tiles")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiles(tiles: List<RoomTile>)

    @Query("SELECT * FROM tiles ORDER BY position ASC")
    suspend fun getTiles(): List<RoomTile>
}
