package com.telen.easylineup.lineup.roster

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LineupRosterViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    var lineupID: Long = 0
    private val rosterItems = mutableListOf<RosterItem>()

    fun setRosterItems(items: List<RosterItem>) {
        rosterItems.clear()
        rosterItems.addAll(items)
    }

    fun getRosterItems() = rosterItems

    fun getCurrentRoster(): Single<List<RosterItem>> {
        return getRoster()
                .map { it.filter { it.status } }
                .map {
                    val items = mutableListOf<RosterItem>()
                    it.forEach {
                        items.add(RosterItem(it.player, it.playerNumberOverlay))
                    }
                    items
                }
    }

    fun getRoster(): Single<List<RosterPlayerStatus>> {
        return domain.lineups().getRoster(lineupID)
                .map { it.players }
    }

    fun saveOverlays(): Completable {
        return domain.players().saveOrUpdatePlayerNumberOverlays(rosterItems)
    }

    fun numberChanged(number: Int, item: RosterItem) {
        item.playerNumberOverlay?.let {
            it.number = number
        } ?: let {
            item.playerNumberOverlay = PlayerNumberOverlay(lineupID = lineupID, playerID = item.player.id, number = number)
        }
    }

    fun saveUpdatedRoster(newRosterStatus: List<RosterPlayerStatus>): Completable {
        return domain.lineups().updateRoster(lineupID, newRosterStatus)
    }

    fun observeLineup(): LiveData<Lineup> {
        return domain.lineups().observeLineupById(lineupID)
    }
}