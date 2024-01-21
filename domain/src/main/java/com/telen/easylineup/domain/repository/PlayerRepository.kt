/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.ShirtNumberEntry
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface PlayerRepository {
    fun insertPlayer(player: Player): Single<Long>
    fun insertPlayers(players: List<Player>): Completable
    fun deletePlayer(player: Player): Completable
    fun updatePlayer(player: Player): Completable
    fun updatePlayersWithRowCount(players: List<Player>): Single<Int>
    fun getPlayerByHash(hash: String): Single<Player>
    fun getPlayerById(playerId: Long): LiveData<Player>
    fun getPlayerByIdAsSingle(playerId: Long): Single<Player>
    fun getPlayersByTeamId(teamId: Long): Single<List<Player>>
    fun getPlayers(): Single<List<Player>>
    fun observePlayers(teamId: Long): LiveData<List<Player>>
    fun getTeamPlayersAndMaybePositions(lineupId: Long): LiveData<List<PlayerWithPosition>>

    fun observePlayersNumberOverlay(lineupId: Long): LiveData<List<PlayerNumberOverlay>>
    fun getPlayersNumberOverlay(lineupId: Long): Single<List<PlayerNumberOverlay>>
    fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable
    fun createPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun createPlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable
    fun getShirtNumberFromPlayers(teamId: Long, number: Int): Single<List<ShirtNumberEntry>>
    fun getShirtNumberFromNumberOverlays(teamId: Long, number: Int): Single<List<ShirtNumberEntry>>
    fun getShirtNumberOverlay(playerId: Long, lineupId: Long): Single<PlayerNumberOverlay>
    fun getPlayerNumberOverlayByHash(hash: String): Single<PlayerNumberOverlay>
}
