package com.telen.easylineup.battingOrder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.data.Team

class BattingOrderViewModel: ViewModel() {
    val team: LiveData<Team> = DatabaseMockProvider().retrieveTeam()
}