package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.RoomPlayerNumberOverlay
import io.reactivex.Completable
import io.reactivex.Single

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

    @Query("SELECT * FROM playerNumberOverlay WHERE lineupID=:lineupID")
    fun getPlayerNumberOverlays(lineupID: Long): Single<List<RoomPlayerNumberOverlay>>
}