/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

internal class ApplicationInteractorImpl(
    private val lineupsInteractor: LineupsInteractor,
    private val tournamentsInteractor: TournamentsInteractor,
) : ApplicationInteractor {

    override fun lineups() = lineupsInteractor
    override fun tournaments() = tournamentsInteractor
}
