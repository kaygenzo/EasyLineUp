/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports.repository

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.ShirtNumberEntry
import kotlinx.coroutines.flow.Flow

interface PlayerNumberOverlayRepository {
    fun observePlayersNumberOverlay(lineupId: Long): Flow<List<PlayerNumberOverlay>>
    suspend fun getPlayersNumberOverlay(lineupId: Long): List<PlayerNumberOverlay>
    suspend fun deletePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>)
    suspend fun updatePlayerNumberOverlays(overlays: List<PlayerNumberOverlay>)
    suspend fun updatePlayerNumberOverlay(overlay: PlayerNumberOverlay)
    suspend fun insertPlayerNumberOverlays(overlays: List<PlayerNumberOverlay>)
    suspend fun insertPlayerNumberOverlay(overlay: PlayerNumberOverlay)
    suspend fun getShirtNumberFromNumberOverlays(teamId: Long, number: Int): List<ShirtNumberEntry>
    suspend fun getShirtNumberOverlay(playerId: Long, lineupId: Long): PlayerNumberOverlay?
    suspend fun getPlayerNumberOverlayByHash(hash: String): PlayerNumberOverlay?
}
