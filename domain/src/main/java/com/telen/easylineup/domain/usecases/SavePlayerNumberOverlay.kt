/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Single

internal class SavePlayerNumberOverlay(private val playerRepository: PlayerRepository) :
    UseCase<SavePlayerNumberOverlay.RequestValues, SavePlayerNumberOverlay.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.items)
            .flatMap {
                val toAdd: MutableList<PlayerNumberOverlay> = mutableListOf()
                val toDelete: MutableList<PlayerNumberOverlay> = mutableListOf()
                val toUpdate: MutableList<PlayerNumberOverlay> = mutableListOf()
                it.forEach { item ->
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
                    .andThen(playerRepository.updatePlayerNumberOverlays(toUpdate))
                    .andThen(playerRepository.createPlayerNumberOverlays(toAdd))
                    .andThen(Single.just(ResponseValue()))
            }
    }

    /**
     * @property items
     */
    class RequestValues(val items: List<RosterItem>) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
