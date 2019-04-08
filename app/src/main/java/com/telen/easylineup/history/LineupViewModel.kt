package com.telen.easylineup.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.PlayerFieldPosition

class LineupViewModel: ViewModel() {
    val lineups = App.database.lineupDao().getAllLineup()

    fun getPlayerFieldPositionFor(lineup: Lineup): LiveData<List<PlayerFieldPosition>> {
        return App.database.lineupDao().getAllPlayerFieldPositionsForLineup(lineup.id)
    }
}