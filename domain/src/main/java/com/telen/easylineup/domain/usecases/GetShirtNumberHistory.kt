/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class GetShirtNumberHistory(
    private val playersRepo: PlayerRepository,
    private val getTeam: GetTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(number: Int): Single<List<ShirtNumberEntry>> {
        val overlaysAdded: MutableList<ShirtNumberEntry> = mutableListOf()
        return getTeam()
            .flatMap { team ->
                val teamId = team.id
                playersRepo.getShirtNumberFromPlayers(teamId, number)
                    .flatMapObservable { items ->
                        Observable.fromIterable(items)
                    }
                    .flatMapSingle { shirtNumber ->
                        playersRepo.getShirtNumberOverlay(shirtNumber.playerId, shirtNumber.lineupId)
                            .map {
                                val newItem = ShirtNumberEntry(
                                    it.number, shirtNumber.playerName, it.playerId,
                                    shirtNumber.eventTime, shirtNumber.createdAt, it.lineupId,
                                    shirtNumber.lineupName
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
                        playersRepo.getShirtNumberFromNumberOverlays(teamId, number)
                            .map { overlays ->
                                overlays.forEach { overlay ->
                                    val first =
                                        overlaysAdded.find {
                                            it.playerId == overlay.playerId
                                                    && it.lineupId == overlay.lineupId
                                        }
                                    first ?: items.add(overlay)
                                }
                                items.filter { it.number == number }
                            }
                    }
            }
            .map {
                it.sortedByDescending { entry ->
                    entry.eventTime.takeIf { it > 0 } ?: let { entry.createdAt }
                }
            }
            .subscribeOn(schedulersProvider.io())
    }
}
