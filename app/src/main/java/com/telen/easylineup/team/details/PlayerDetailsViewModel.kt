package com.telen.easylineup.team.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.data.PositionWithLineup

class PlayerDetailsViewModel: ViewModel() {
    fun getAllLineupsForPlayer(playerID: Long): LiveData<List<PositionWithLineup>> {
        return App.database.lineupDao().getAllPositionsForPlayer(playerID)
    }
}