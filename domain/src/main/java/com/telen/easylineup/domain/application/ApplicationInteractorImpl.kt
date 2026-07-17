/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

internal class ApplicationInteractorImpl(
    private val dataInteractor: DataInteractor,
    private val lineupsInteractor: LineupsInteractor,
    private val tournamentsInteractor: TournamentsInteractor,
    private val playerFieldPositionsInteractor: PlayerFieldPositionsInteractor,
    private val playerInteractor: PlayersInteractor,
) : ApplicationInteractor {

    override fun data() = dataInteractor
    override fun lineups() = lineupsInteractor
    override fun tournaments() = tournamentsInteractor
    override fun playerFieldPositions() = playerFieldPositionsInteractor
    override fun players() = playerInteractor
}
