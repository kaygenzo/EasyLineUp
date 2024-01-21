/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
internal interface PlayerNumberOverlayDao {
    @Query("DELETE FROM playerNumberOverlay")
    fun deleteAll(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayerNumberOverlay(item: RoomPlayerNumberOverlay): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayerNumberOverlays(items: List<RoomPlayerNumberOverlay>): Completable

    @Delete
    fun deletePlayerNumberOverlay(item: RoomPlayerNumberOverlay): Completable

    @Delete
    fun deletePlayerNumberOverlays(items: List<RoomPlayerNumberOverlay>): Completable

    @Update
    fun updatePlayerNumberOverlay(item: RoomPlayerNumberOverlay): Completable

    @Update
    fun updatePlayerNumberOverlays(items: List<RoomPlayerNumberOverlay>): Completable

    @Query("SELECT * FROM playerNumberOverlay WHERE lineupID=:lineupId")
    fun getPlayerNumberOverlays(lineupId: Long): Single<List<RoomPlayerNumberOverlay>>

    @Query("SELECT * FROM playerNumberOverlay WHERE lineupID=:lineupId")
    fun observePlayerNumberOverlays(lineupId: Long): LiveData<List<RoomPlayerNumberOverlay>>

    @Query("SELECT * from playerNumberOverlay WHERE hash = :hash")
    fun getPlayerNumberOverlayByHash(hash: String): Single<RoomPlayerNumberOverlay>

    @Query("SELECT * from playerNumberOverlay WHERE playerID=:playerId AND lineupID=:lineupId")
    fun getShirtNumberOverlay(playerId: Long, lineupId: Long): Single<RoomPlayerNumberOverlay>
}
