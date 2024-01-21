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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
internal interface TilesDao {
    @Query("DELETE FROM tiles")
    fun deleteAll(): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTile(tile: RoomTile): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTiles(tiles: List<RoomTile>): Completable

    @Delete
    fun deleteTile(tile: RoomTile): Completable

    @Delete
    fun deleteTiles(tiles: List<RoomTile>): Completable

    @Update
    fun updateTile(tile: RoomTile): Completable

    @Update
    fun updateTiles(tiles: List<RoomTile>): Completable

    @Query("SELECT * FROM tiles ORDER BY position ASC")
    fun getTiles(): Single<List<RoomTile>>
}
