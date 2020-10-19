package com.telen.easylineup.domain.repository

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.ShirtNumberEntry
import io.reactivex.Completable
import io.reactivex.Single

interface PlayerRepository {
    fun insertPlayer(player: Player): Single<Long>
    fun insertPlayers(players: List<Player>): Completable
    fun deletePlayer(player: Player): Completable
    fun updatePlayer(player: Player): Completable
    fun updatePlayersWithRowCount(players: List<Player>): Single<Int>
    fun getPlayerByHash(hash: String): Single<Player>
    fun getPlayerById(playerID: Long): LiveData<Player>
    fun getPlayerByIdAsSingle(playerID: Long): Single<Player>
    fun getPlayers(teamID: Long): Single<List<Player>>
    fun getPlayers(): Single<List<Player>>
    fun getTeamPlayersAndMaybePositions(lineupID: Long): LiveData<List<PlayerWithPosition>>

    fun observePlayersNumberOverlay(lineupID: Long): LiveData<List<PlayerNumberOverlay>>
    fun getPlayersNumberOverlay(lineupID: Long): Single<List<PlayerNumberOverlay>>
    fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable
    fun createPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>): Completable
    fun createPlayerNumberOverlay(overlay: PlayerNumberOverlay): Completable
    fun getShirtNumberFromPlayers(number: Int): Single<List<ShirtNumberEntry>>
    fun getShirtNumberFromNumberOverlays(number: Int): Single<List<ShirtNumberEntry>>
    fun getShirtNumberOverlay(playerID: Long, lineupID: Long): Single<PlayerNumberOverlay>
    fun getPlayerNumberOverlayByHash(hash: String): Single<PlayerNumberOverlay>
}