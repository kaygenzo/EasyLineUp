package com.telen.easylineup

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.data.Team

class HomeViewModel: ViewModel() {

    fun registerTeamUpdates(): LiveData<Team> {
        return Transformations.map(App.database.teamDao().getTeams()) {
            it.size
            it.first()
        }
    }
}