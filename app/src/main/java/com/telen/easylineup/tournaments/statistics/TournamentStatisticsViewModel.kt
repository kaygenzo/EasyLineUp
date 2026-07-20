/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetTournamentStatsForPositionTable
import com.telen.easylineup.utils.getStrategiesDisplayName
import io.github.kaygenzo.androidtable.api.CellConfiguration
import io.github.kaygenzo.androidtable.api.Highlight
import io.github.kaygenzo.androidtable.api.StyleConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class TournamentStatisticsViewModel : ViewModel(), KoinComponent {
    var job: Job? = null
    val topHeadersData: MutableSharedFlow<List<CellConfiguration>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val leftHeadersData: MutableSharedFlow<List<CellConfiguration>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val mainTableData: MutableSharedFlow<List<List<CellConfiguration>>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val columnHighlights: MutableSharedFlow<List<Highlight>> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private val getTournamentStatsForPositionTable: GetTournamentStatsForPositionTable by inject()
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
        val currentTournament = tournament ?: run {
            Timber.e(IllegalArgumentException())
            return
        }
        job = viewModelScope.launch {
            getTournamentStatsForPositionTable(currentTournament, strategy)
                .onSuccess {
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

                    this@TournamentStatisticsViewModel.topHeadersData.tryEmit(topHeaderDataList)
                    this@TournamentStatisticsViewModel.leftHeadersData.tryEmit(leftHeaderDataList)
                    this@TournamentStatisticsViewModel.mainTableData.tryEmit(mainDataList)
                    this@TournamentStatisticsViewModel.columnHighlights.tryEmit(columnHighlights)
                }
                .onFailure {
                    Timber.e(it)
                }
        }
    }

    fun onStrategyChosen(index: Int) {
        val strategies = teamType?.getStrategies() ?: arrayOf()
        if (index < strategies.size) {
            strategy = strategies[index]
        }
        getPlayersPositionForTournament()
    }

    fun clear() {
        job?.cancel()
    }

    fun getStrategiesNames(context: Context): Array<String>? {
        return teamType?.getStrategiesDisplayName(context)
    }
}
