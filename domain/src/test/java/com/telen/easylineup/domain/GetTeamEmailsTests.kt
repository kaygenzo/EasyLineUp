/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetPlayers
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTeamEmails
import io.reactivex.rxjava3.core.Single
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
internal class GetTeamEmailsTests {
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getTeamEmails: GetTeamEmails

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getTeamEmails = GetTeamEmails(
            GetPlayers(playerDao, GetTeam(teamDao, testSchedulersProvider()), testDispatcherProvider()),
            testDispatcherProvider()
        )
        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "Panthers", main = true))))
    }

    @Test
    fun shouldFilterOutPlayersWithoutEmail() = runTest {
        val withEmail = Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L, email = "a@mail.com")
        val withoutEmail = Player(id = 2L, teamId = 1L, name = "Titi", shirtNumber = 2, licenseNumber = 2L, email = null)
        Mockito.`when`(playerDao.getPlayersByTeamId(1L))
            .thenReturn(Single.just(listOf(withEmail, withoutEmail)))

        val result = getTeamEmails()

        assertTrue(result.isSuccess)
        assertEquals(listOf("a@mail.com"), result.getOrNull())
    }
}
