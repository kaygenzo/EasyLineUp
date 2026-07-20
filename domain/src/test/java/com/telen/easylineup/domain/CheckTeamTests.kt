/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class CheckTeamTests {
    val team = Team(1L, "A", null, 0, true, null)
    lateinit var checkTeam: CheckTeam

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        checkTeam = CheckTeam(testDispatcherProvider())
    }

    @Test
    fun shouldAcceptTeamWithNameNotEmpty() = runTest {
        val result = checkTeam(team)
        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldRejectTeamWithNameEmpty() = runTest {
        team.name = ""
        val result = checkTeam(team)
        assertTrue(result.exceptionOrNull() is NameEmptyException)
    }

    @Test
    fun shouldRejectTeamWithNameOnlyWhitespaces() = runTest {
        team.name = "    "
        val result = checkTeam(team)
        assertTrue(result.exceptionOrNull() is NameEmptyException)
    }
}
