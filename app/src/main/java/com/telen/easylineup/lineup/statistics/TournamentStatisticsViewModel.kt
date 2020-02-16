package com.telen.easylineup.lineup.statistics

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.GetTournamentStatsForPositionTable
import com.telen.easylineup.repository.model.Tournament
import com.telen.library.widget.tablemultiscroll.views.MultipleScrollTableView
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TournamentStatisticsViewModel: ViewModel(), KoinComponent {

    private val getTeamUseCase: GetTeam by inject()
    private val tableDataUseCase: GetTournamentStatsForPositionTable by inject()

    var disposable: Disposable? = null

    val topHeadersData = MutableLiveData<List<MultipleScrollTableView.CellConfiguration>>()
    val leftHeadersData = MutableLiveData<List<MultipleScrollTableView.CellConfiguration>>()
    val mainTableData = MutableLiveData<List<List<MultipleScrollTableView.CellConfiguration>>>()

    fun getPlayersPositionForTournament(context: Context, tournament: Tournament) {
        disposable = UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .flatMap { UseCaseHandler.execute(tableDataUseCase, GetTournamentStatsForPositionTable.RequestValues(tournament, it.team, context)) }
                .subscribe({
                    val leftHeaderDataList = mutableListOf<MultipleScrollTableView.CellConfiguration>()
                    it.leftHeader.forEach {
                        leftHeaderDataList.add(MultipleScrollTableView.CellConfiguration(it.first))
                    }

                    val topHeaderDataList = mutableListOf<MultipleScrollTableView.CellConfiguration>()
                    it.topHeader.forEach {
                        topHeaderDataList.add(MultipleScrollTableView.CellConfiguration(it.first, it.second))
                    }

                    val mainDataList = mutableListOf<List<MultipleScrollTableView.CellConfiguration>>()
                    it.mainTable.forEach {
                        val list = mutableListOf<MultipleScrollTableView.CellConfiguration>()
                        mainDataList.add(list)
                        it.forEach {
                            list.add(MultipleScrollTableView.CellConfiguration(it.first, it.second))
                        }
                    }

                    topHeadersData.value = topHeaderDataList
                    leftHeadersData.value = leftHeaderDataList
                    mainTableData.value = mainDataList

                }, {
                    Timber.e(it)
                })
    }

    fun clear() {
        disposable?.dispose()
    }
}