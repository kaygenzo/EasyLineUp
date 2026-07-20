/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.InsertTeam
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class InsertTeamTests {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var insertTeam: InsertTeam

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        insertTeam = InsertTeam(teamDao, testDispatcherProvider())
    }

    @Test
    fun shouldDelegateToRepository() = runTest {
        val team = Team(id = 0L, name = "Panthers")
        Mockito.`when`(teamDao.insertTeam(team)).thenReturn(5L)

        val result = insertTeam(team)

        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull())
    }
}
