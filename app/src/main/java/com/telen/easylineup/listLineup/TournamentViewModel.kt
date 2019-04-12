package com.telen.easylineup.listLineup

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App

class TournamentViewModel: ViewModel() {
    val tournaments = App.database.tournamentDao().getTournaments()
}