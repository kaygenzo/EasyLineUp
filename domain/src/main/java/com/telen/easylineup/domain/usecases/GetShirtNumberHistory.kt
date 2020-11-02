package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.Observable
import io.reactivex.Single

internal class GetShirtNumberHistory(private val playersRepo: PlayerRepository): UseCase<GetShirtNumberHistory.RequestValues, GetShirtNumberHistory.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val overlaysAdded = mutableListOf<ShirtNumberEntry>()
        return playersRepo.getShirtNumberFromPlayers(requestValues.teamID, requestValues.number)
                .flatMapObservable { items ->
                    Observable.fromIterable(items)
                }
                .flatMapSingle { shirtNumber ->
                    playersRepo.getShirtNumberOverlay(shirtNumber.playerID, shirtNumber.lineupID)
                            .map {
                                val newItem = ShirtNumberEntry(it.number, shirtNumber.playerName, it.playerID, shirtNumber.eventTime,
                                        shirtNumber.createdAt, it.lineupID, shirtNumber.lineupName)
                                overlaysAdded.add(newItem)
                                newItem
                            }
                            .onErrorResumeNext {
                                Single.just(shirtNumber)
                            }
                }
                .toList()
                .flatMap { items ->
                    playersRepo.getShirtNumberFromNumberOverlays(requestValues.teamID, requestValues.number)
                            .map { overlays ->
                                overlays.forEach { overlay ->
                                    val first = overlaysAdded.find { it.playerID == overlay.playerID && it.lineupID == overlay.lineupID }
                                    if(first == null) {
                                        items.add(overlay)
                                    }
                                }
                                items.filter { it.number == requestValues.number }
                            }
                }
                .map {
                    it.sortedByDescending {entry -> entry.eventTime.takeIf { it > 0 } ?: let { entry.createdAt } }
                }
                .map { ResponseValue(it) }
    }

    class ResponseValue(val history: List<ShirtNumberEntry>): UseCase.ResponseValue
    class RequestValues(val teamID: Long, val number: Int): UseCase.RequestValues
}