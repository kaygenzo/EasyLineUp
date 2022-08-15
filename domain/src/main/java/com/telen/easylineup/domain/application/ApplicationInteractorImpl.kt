package com.telen.easylineup.domain.application

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class ApplicationInteractorImpl : ApplicationInteractor, KoinComponent {

    private val dataInteractor: DataInteractor by inject()
    private val lineupsInteractor: LineupsInteractor by inject()
    private val teamsInteractor: TeamsInteractor by inject()
    private val tournamentsInteractor: TournamentsInteractor by inject()
    private val playerFieldPositionsInteractor: PlayerFieldPositionsInteractor by inject()
    private val playerInteractor: PlayersInteractor by inject()

    override fun data() = dataInteractor
    override fun lineups() = lineupsInteractor
    override fun teams() = teamsInteractor
    override fun tournaments() = tournamentsInteractor
    override fun playerFieldPositions() = playerFieldPositionsInteractor
    override fun players() = playerInteractor
}