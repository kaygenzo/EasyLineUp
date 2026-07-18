package com.telen.easylineup.domain.usecases

interface StringResourcesProvider {
    fun positionShortNames(teamType: Int): Array<String>
    fun unknownPlayerName(): String
    fun gamesPlayedLabel(): String
    fun strategyDisplayNames(teamType: Int): Array<String>?
}
