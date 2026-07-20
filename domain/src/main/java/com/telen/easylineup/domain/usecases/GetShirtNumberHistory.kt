/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class GetShirtNumberHistory(
    private val playersRepo: PlayerRepository,
    private val getTeam: GetTeam,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(number: Int): Result<List<ShirtNumberEntry>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val overlaysAdded: MutableList<ShirtNumberEntry> = mutableListOf()
            val team = getTeam().getOrThrow()
            val teamId = team.id

            val shirtNumbers = playersRepo.getShirtNumberFromPlayers(teamId, number)
            val items = shirtNumbers.map { shirtNumber ->
                try {
                    val overlay = playersRepo.getShirtNumberOverlay(
                        shirtNumber.playerId,
                        shirtNumber.lineupId
                    )
                    val newItem = ShirtNumberEntry(
                        overlay.number, shirtNumber.playerName, overlay.playerId,
                        shirtNumber.eventTime, shirtNumber.createdAt, overlay.lineupId,
                        shirtNumber.lineupName
                    )
                    overlaysAdded.add(newItem)
                    newItem
                } catch (c: CancellationException) {
                    throw c
                } catch (e: Exception) {
                    shirtNumber
                }
            }.toMutableList()

            val overlays = playersRepo.getShirtNumberFromNumberOverlays(teamId, number)
            overlays.forEach { overlay ->
                val first = overlaysAdded.find {
                    it.playerId == overlay.playerId && it.lineupId == overlay.lineupId
                }
                first ?: items.add(overlay)
            }

            items.filter { it.number == number }
                .sortedByDescending { entry -> entry.eventTime.takeIf { it > 0 } ?: entry.createdAt }
        }
    }
}
