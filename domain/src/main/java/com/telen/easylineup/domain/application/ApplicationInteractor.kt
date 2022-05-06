package com.telen.easylineup.domain.application

interface ApplicationInteractor {
    fun players(): PlayersInteractor
    fun teams(): TeamsInteractor
    fun lineups(): LineupsInteractor
    fun tournaments(): TournamentsInteractor
    fun playerFieldPositions(): PlayerFieldPositionsInteractor
    fun data(): DataInteractor
}