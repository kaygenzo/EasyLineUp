/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.supportsDesignatedPlayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class TeamStrategyTests {
    @Test
    fun baseball5HasNoDesignatedPlayer() {
        assertFalse(TeamStrategy.B5_DEFAULT.supportsDesignatedPlayer())
    }

    @Test
    fun everyOtherStrategySupportsADesignatedPlayer() {
        listOf(
            TeamStrategy.STANDARD,
            TeamStrategy.SLOWPITCH,
            TeamStrategy.FIVE_MAN_STANDARD
        ).forEach { assertTrue(it.name, it.supportsDesignatedPlayer()) }
    }
}
