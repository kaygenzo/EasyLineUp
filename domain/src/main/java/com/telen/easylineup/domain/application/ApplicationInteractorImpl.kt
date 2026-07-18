/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

internal class ApplicationInteractorImpl(
    private val lineupsInteractor: LineupsInteractor,
) : ApplicationInteractor {

    override fun lineups() = lineupsInteractor
}
