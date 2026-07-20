/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.PlayerRepository
import kotlinx.coroutines.withContext

class SavePlayerNumberOverlay(
    private val playerRepository: PlayerRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(items: List<RosterItem>): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val toAdd: MutableList<PlayerNumberOverlay> = mutableListOf()
            val toDelete: MutableList<PlayerNumberOverlay> = mutableListOf()
            val toUpdate: MutableList<PlayerNumberOverlay> = mutableListOf()
            items.forEach { item ->
                item.playerNumberOverlay?.let { overlay ->
                    if (item.player.shirtNumber == overlay.number) {
                        if (overlay.id > 0L) {
                            toDelete.add(overlay)
                        } else {
                            // nothing to do
                        }
                    } else {
                        if (overlay.id > 0L) {
                            toUpdate.add(overlay)
                        } else {
                            toAdd.add(overlay)
                        }
                    }
                }
            }
            playerRepository.deletePlayerNumberOverlays(toDelete)
            playerRepository.updatePlayerNumberOverlays(toUpdate)
            playerRepository.createPlayerNumberOverlays(toAdd)
        }
    }
}
