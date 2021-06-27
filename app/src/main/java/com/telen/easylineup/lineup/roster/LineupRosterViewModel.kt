package com.telen.easylineup.lineup.roster

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.*
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject

class LineupRosterViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()
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
        return domain.getRoster(lineupID)
                .map { it.players }
    }

    fun saveOverlays(): Completable {
        return domain.saveOrUpdatePlayerNumberOverlays(rosterItems)
    }

    fun numberChanged(number: Int, item: RosterItem) {
        item.playerNumberOverlay?.let {
            it.number = number
        } ?: let {
            item.playerNumberOverlay = PlayerNumberOverlay(lineupID = lineupID, playerID = item.player.id, number = number)
        }
    }

    fun saveUpdatedRoster(newRosterStatus: List<RosterPlayerStatus>): Completable {
        return domain.updateRoster(lineupID, newRosterStatus)
    }

    fun observeLineup(): LiveData<Lineup> {
        return domain.observeLineupById(lineupID)
    }
}