/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.ShirtNumberEntry
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    suspend fun insertPlayer(player: Player): Long
    suspend fun insertPlayers(players: List<Player>)
    suspend fun deletePlayer(player: Player)
    suspend fun updatePlayer(player: Player)
    suspend fun updatePlayersWithRowCount(players: List<Player>): Int
    suspend fun getPlayerByHash(hash: String): Player
    fun getPlayerById(playerId: Long): Flow<Player>
    suspend fun getPlayerByIdAsSingle(playerId: Long): Player
    suspend fun getPlayersByTeamId(teamId: Long): List<Player>
    suspend fun getPlayers(): List<Player>
    fun observePlayers(teamId: Long): Flow<List<Player>>
    fun getTeamPlayersAndMaybePositions(lineupId: Long): Flow<List<PlayerWithPosition>>

    fun observePlayersNumberOverlay(lineupId: Long): Flow<List<PlayerNumberOverlay>>
    suspend fun getPlayersNumberOverlay(lineupId: Long): List<PlayerNumberOverlay>
    suspend fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>)
    suspend fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>)
    suspend fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay)
    suspend fun createPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>)
    suspend fun createPlayerNumberOverlay(overlay: PlayerNumberOverlay)
    suspend fun getShirtNumberFromPlayers(teamId: Long, number: Int): List<ShirtNumberEntry>
    suspend fun getShirtNumberFromNumberOverlays(teamId: Long, number: Int): List<ShirtNumberEntry>
    suspend fun getShirtNumberOverlay(playerId: Long, lineupId: Long): PlayerNumberOverlay
    suspend fun getPlayerNumberOverlayByHash(hash: String): PlayerNumberOverlay
}
