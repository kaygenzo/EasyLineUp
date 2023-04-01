package com.telen.easylineup.lineup.edition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.domain.model.RosterPlayerStatus
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LineupEditionViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    var lineupID: Long = 0
    private val rosterItems = mutableListOf<RosterItem>()
    private val _rosterItemsLiveData = MutableLiveData<List<RosterItem>>()

    fun observeRosterItems(): LiveData<List<RosterItem>> {
        return _rosterItemsLiveData
    }

    fun loadRoster(): Completable {
        return getRoster()
            .map { it.filter { it.status } }
            .map { it.map { RosterItem(it.player, it.playerNumberOverlay) } }
            .doOnSuccess {
                this.rosterItems.run {
                    clear()
                    addAll(it)
                    _rosterItemsLiveData.postValue(this)
                }
            }
            .ignoreElement()
    }

    fun getRoster(): Single<List<RosterPlayerStatus>> {
        return domain.lineups().getRoster(lineupID).map { it.players }
    }

    fun saveOverlays(): Completable {
        return domain.players().saveOrUpdatePlayerNumberOverlays(rosterItems)
    }

    fun numberChanged(number: Int, item: RosterItem) {
        item.playerNumberOverlay?.let {
            it.number = number
        } ?: let {
            item.playerNumberOverlay =
                PlayerNumberOverlay(lineupID = lineupID, playerID = item.player.id, number = number)
        }
    }

    fun saveUpdatedRoster(newRosterStatus: List<RosterPlayerStatus>): Completable {
        return Completable.create {
            this.rosterItems.run {
                clear()
                addAll(newRosterStatus.filter { it.status }
                    .map { RosterItem(it.player, it.playerNumberOverlay) })
                _rosterItemsLiveData.postValue(this)
            }
        }
    }
}