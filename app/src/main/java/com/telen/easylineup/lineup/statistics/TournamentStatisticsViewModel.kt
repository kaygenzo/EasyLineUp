package com.telen.easylineup.lineup.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.R
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.Tournament
import com.telen.library.widget.tablemultiscroll.views.CellConfiguration
import com.telen.library.widget.tablemultiscroll.views.Highlight
import com.telen.library.widget.tablemultiscroll.views.StyleConfiguration
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TournamentStatisticsViewModel: ViewModel(), KoinComponent {

    var disposable: Disposable? = null

    val topHeadersData = MutableLiveData<List<CellConfiguration>>()
    val leftHeadersData = MutableLiveData<List<CellConfiguration>>()
    val mainTableData = MutableLiveData<List<List<CellConfiguration>>>()
    val columnHighlights = MutableLiveData<List<Highlight>>()

    private val domain: ApplicationPort by inject()

    fun getPlayersPositionForTournament(tournament: Tournament) {
        disposable = domain.getPlayersPositionForTournament(tournament)
                .subscribe({
                    val leftHeaderDataList = mutableListOf<CellConfiguration>()
                    it.leftHeader.forEach {
                        leftHeaderDataList.add(CellConfiguration(it.first))
                    }

                    val topHeaderDataList = mutableListOf<CellConfiguration>()
                    it.topHeader.forEach {
                        topHeaderDataList.add(CellConfiguration(it.first, it.second))
                    }

                    val mainDataList = mutableListOf<List<CellConfiguration>>()
                    it.mainTable.forEach {
                        val list = mutableListOf<CellConfiguration>()
                        mainDataList.add(list)
                        it.forEach {
                            list.add(CellConfiguration(it.first, it.second))
                        }
                    }

                    val columnHighlights = mutableListOf<Highlight>()
                    it.columnToHighlight.forEach {
                        columnHighlights.add(it, Highlight(it, StyleConfiguration( cellDefaultBackgroundColor = R.color.list_empty_text, cellDefaultTextColor = R.color.white )))
                    }

                    this.topHeadersData.value = topHeaderDataList
                    this.leftHeadersData.value = leftHeaderDataList
                    this.mainTableData.value = mainDataList
                    this.columnHighlights.value = columnHighlights

                }, {
                    Timber.e(it)
                })
    }

    fun clear() {
        disposable?.dispose()
    }
}