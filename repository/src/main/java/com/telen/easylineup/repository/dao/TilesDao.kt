/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.telen.easylineup.repository.model.RoomTile

@Dao
internal interface TilesDao {
    @Query("DELETE FROM tiles")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTile(tile: RoomTile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiles(tiles: List<RoomTile>)

    @Delete
    suspend fun deleteTile(tile: RoomTile)

    @Delete
    suspend fun deleteTiles(tiles: List<RoomTile>)

    @Update
    suspend fun updateTile(tile: RoomTile)

    @Update
    suspend fun updateTiles(tiles: List<RoomTile>)

    @Query("SELECT * FROM tiles ORDER BY position ASC")
    suspend fun getTiles(): List<RoomTile>
}
