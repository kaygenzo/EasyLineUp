package com.telen.easylineup.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.domain.model.PlayerNumberOverlay
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

    @Query("SELECT * FROM playerNumberOverlay WHERE lineupID=:lineupID")
    fun observePlayerNumberOverlays(lineupID: Long): LiveData<List<RoomPlayerNumberOverlay>>

    @Query("SELECT * from playerNumberOverlay WHERE hash = :hash" )
    fun getPlayerNumberOverlayByHash(hash: String): Single<RoomPlayerNumberOverlay>

    @Query("SELECT * from playerNumberOverlay WHERE playerID=:playerID AND lineupID=:lineupID" )
    fun getShirtNumberOverlay(playerID: Long, lineupID: Long): Single<RoomPlayerNumberOverlay>
}