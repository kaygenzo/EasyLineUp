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
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlayerNumberOverlayDao {
    @Query("DELETE FROM playerNumberOverlay")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerNumberOverlay(item: RoomPlayerNumberOverlay): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerNumberOverlays(items: List<RoomPlayerNumberOverlay>)

    @Delete
    suspend fun deletePlayerNumberOverlay(item: RoomPlayerNumberOverlay)

    @Delete
    suspend fun deletePlayerNumberOverlays(items: List<RoomPlayerNumberOverlay>)

    @Update
    suspend fun updatePlayerNumberOverlay(item: RoomPlayerNumberOverlay)

    @Update
    suspend fun updatePlayerNumberOverlays(items: List<RoomPlayerNumberOverlay>)

    @Query("SELECT * FROM playerNumberOverlay WHERE lineupID=:lineupId")
    suspend fun getPlayerNumberOverlays(lineupId: Long): List<RoomPlayerNumberOverlay>

    @Query("SELECT * FROM playerNumberOverlay WHERE lineupID=:lineupId")
    fun observePlayerNumberOverlays(lineupId: Long): Flow<List<RoomPlayerNumberOverlay>>

    @Query("SELECT * from playerNumberOverlay WHERE hash = :hash")
    suspend fun getPlayerNumberOverlayByHash(hash: String): RoomPlayerNumberOverlay

    @Query("SELECT * from playerNumberOverlay WHERE playerID=:playerId AND lineupID=:lineupId")
    suspend fun getShirtNumberOverlay(playerId: Long, lineupId: Long): RoomPlayerNumberOverlay
}
