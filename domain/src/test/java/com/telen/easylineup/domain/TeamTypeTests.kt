/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.getStrategies
import org.junit.Assert.assertArrayEquals
import org.junit.Test

internal class TeamTypeTests {

    @Test
    fun softballOffersStandardFiveManAndSlowpitch() {
        assertArrayEquals(
            arrayOf(TeamStrategy.STANDARD, TeamStrategy.FIVE_MAN_STANDARD, TeamStrategy.SLOWPITCH),
            TeamType.SOFTBALL.getStrategies()
        )
    }

    @Test
    fun baseball5OnlyOffersItsOwnDefaultStrategy() {
        assertArrayEquals(arrayOf(TeamStrategy.B5_DEFAULT), TeamType.BASEBALL_5.getStrategies())
    }

    @Test
    fun baseballOffersStandardAndFiveMan() {
        assertArrayEquals(
            arrayOf(TeamStrategy.STANDARD, TeamStrategy.FIVE_MAN_STANDARD),
            TeamType.BASEBALL.getStrategies()
        )
    }

    @Test
    fun unknownTeamTypeFallsBackToTheSameOptionsAsBaseball() {
        assertArrayEquals(
            arrayOf(TeamStrategy.STANDARD, TeamStrategy.FIVE_MAN_STANDARD),
            TeamType.UNKNOWN.getStrategies()
        )
    }
}
