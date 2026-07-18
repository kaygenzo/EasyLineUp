/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

interface ApplicationInteractor {
    fun lineups(): LineupsInteractor
    fun tournaments(): TournamentsInteractor
    fun playerFieldPositions(): PlayerFieldPositionsInteractor
}
