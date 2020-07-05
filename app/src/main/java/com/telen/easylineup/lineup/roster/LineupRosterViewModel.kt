package com.telen.easylineup.lineup.roster

import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.TeamRosterSummary
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class LineupRosterViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()
    var lineupID: Long = 0
    private val rosterItems = mutableListOf<RosterItem>()

    fun setRosterItems(items: List<RosterItem>) {
        rosterItems.clear()
        rosterItems.addAll(items)
    }

    fun getRosterItems() = rosterItems

    fun getRoster(): Single<List<RosterItem>> {
        return domain.getRoster(lineupID)
                .map { it.players.filter { it.status == true } }
                .map {
                    val items = mutableListOf<RosterItem>()
                    it.forEach {
                        items.add(RosterItem(it.player, it.playerNumberOverlay))
                    }
                    items
                }
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
}