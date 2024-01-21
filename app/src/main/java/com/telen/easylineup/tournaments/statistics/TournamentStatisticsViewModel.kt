/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.R
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import io.github.kaygenzo.androidtable.api.CellConfiguration
import io.github.kaygenzo.androidtable.api.Highlight
import io.github.kaygenzo.androidtable.api.StyleConfiguration
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TournamentStatisticsViewModel : ViewModel(), KoinComponent {
    var disposable: Disposable? = null
    val topHeadersData: MutableLiveData<List<CellConfiguration>> = MutableLiveData()
    val leftHeadersData: MutableLiveData<List<CellConfiguration>> = MutableLiveData()
    val mainTableData: MutableLiveData<List<List<CellConfiguration>>> = MutableLiveData()
    val columnHighlights: MutableLiveData<List<Highlight>> = MutableLiveData()
    private val domain: ApplicationInteractor by inject()
    var strategy = TeamStrategy.STANDARD
    var tournament: Tournament? = null
    var teamType: TeamType? = null
        set(value) {
            field = value
            this.strategy = when (field) {
                TeamType.BASEBALL_5 -> TeamStrategy.B5_DEFAULT
                else -> TeamStrategy.STANDARD
            }
        }

    fun getPlayersPositionForTournament() {
        disposable = Single.create { emitter ->
            tournament?.let {
                emitter.onSuccess(it)
            } ?: run {
                emitter.onError(IllegalArgumentException())
            }
        }
            .flatMap { domain.tournaments().getPlayersPositionForTournament(it, strategy) }
            .subscribe({
                val leftHeaderDataList: MutableList<CellConfiguration> = mutableListOf()
                it.leftHeader.forEach {
                    leftHeaderDataList.add(CellConfiguration(it.first))
                }

                val topHeaderDataList: MutableList<CellConfiguration> = mutableListOf()
                it.topHeader.forEach {
                    topHeaderDataList.add(CellConfiguration(it.first, it.second))
                }

                val mainDataList: MutableList<List<CellConfiguration>> = mutableListOf()
                it.mainTable.forEach {
                    val list: MutableList<CellConfiguration> = mutableListOf()
                    mainDataList.add(list)
                    it.forEach {
                        list.add(CellConfiguration(it.first, it.second))
                    }
                }

                val columnHighlights: MutableList<Highlight> = mutableListOf()
                it.columnToHighlight.forEach {
                    columnHighlights.add(
                        it,
                        Highlight(
                            it,
                            StyleConfiguration(
                                cellDefaultBackgroundColor = R.color.grey,
                                cellDefaultTextColor = R.color.white
                            )
                        )
                    )
                }

                this.topHeadersData.value = topHeaderDataList
                this.leftHeadersData.value = leftHeaderDataList
                this.mainTableData.value = mainDataList
                this.columnHighlights.value = columnHighlights
            }, {
                Timber.e(it)
            })
    }

    fun onStrategyChosen(index: Int) {
        val strategies = teamType?.getStrategies() ?: arrayOf()
        if (index < strategies.size) {
            strategy = strategies[index]
        }
        getPlayersPositionForTournament()
    }

    fun clear() {
        disposable?.dispose()
    }

    fun getStrategiesNames(context: Context): Array<String>? {
        return teamType?.getStrategiesDisplayName(context)
    }
}
