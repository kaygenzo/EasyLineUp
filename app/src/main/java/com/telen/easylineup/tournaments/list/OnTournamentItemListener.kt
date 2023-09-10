package com.telen.easylineup.tournaments.list

import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament

interface OnTournamentItemListener {
    fun onHeaderClicked()
    fun onLineupClicked(lineup: Lineup)
    fun onDeleteTournamentClicked(tournament: Tournament)
    fun onStatisticsTournamentClicked(teamType: TeamType, tournament: Tournament)
    fun onEditLineupClicked(lineup: Lineup)
    fun onMapClicked(location: GeoLocation)
}