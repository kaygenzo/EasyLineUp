package com.telen.easylineup.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.data.Team

class TeamViewModel: ViewModel() {
    val team: LiveData<Team> = DatabaseMockProvider().retrieveTeam()
}