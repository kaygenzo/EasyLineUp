/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal class GetShirtNumberHistory(private val playersRepo: PlayerRepository) :
    UseCase<GetShirtNumberHistory.RequestValues, GetShirtNumberHistory.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val overlaysAdded: MutableList<ShirtNumberEntry> = mutableListOf()
        return playersRepo.getShirtNumberFromPlayers(requestValues.teamId, requestValues.number)
            .flatMapObservable { items ->
                Observable.fromIterable(items)
            }
            .flatMapSingle { shirtNumber ->
                playersRepo.getShirtNumberOverlay(shirtNumber.playerId, shirtNumber.lineupId)
                    .map {
                        val newItem = ShirtNumberEntry(
                            it.number, shirtNumber.playerName, it.playerId, shirtNumber.eventTime,
                            shirtNumber.createdAt, it.lineupId, shirtNumber.lineupName
                        )
                        overlaysAdded.add(newItem)
                        newItem
                    }
                    .onErrorResumeNext {
                        Single.just(shirtNumber)
                    }
            }
            .toList()
            .flatMap { items ->
                playersRepo.getShirtNumberFromNumberOverlays(
                    requestValues.teamId,
                    requestValues.number
                )
                    .map { overlays ->
                        overlays.forEach { overlay ->
                            val first =
                                overlaysAdded.find {
                                    it.playerId == overlay.playerId
                                            && it.lineupId == overlay.lineupId
                                }
                            first ?: items.add(overlay)
                        }
                        items.filter { it.number == requestValues.number }
                    }
            }
            .map {
                it.sortedByDescending { entry ->
                    entry.eventTime.takeIf { it > 0 } ?: let { entry.createdAt }
                }
            }
            .map { ResponseValue(it) }
    }

    /**
     * @property history
     */
    class ResponseValue(val history: List<ShirtNumberEntry>) : UseCase.ResponseValue

    /**
     * @property teamId
     * @property number
     */
    class RequestValues(val teamId: Long, val number: Int) : UseCase.RequestValues
}
