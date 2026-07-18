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
import com.telen.easylineup.domain.usecases.GetTeamPhones
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTeamPhonesTests {
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getTeamPhones: GetTeamPhones

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getTeamPhones = GetTeamPhones(GetPlayers(playerDao, GetTeam(teamDao)))
        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "Panthers", main = true))))
    }

    @Test
    fun shouldFilterOutPlayersWithoutPhone() {
        val withPhone = Player(id = 1L, teamId = 1L, name = "Toto", shirtNumber = 1, licenseNumber = 1L, phone = "0102030405")
        val withoutPhone = Player(id = 2L, teamId = 1L, name = "Titi", shirtNumber = 2, licenseNumber = 2L, phone = null)
        Mockito.`when`(playerDao.getPlayersByTeamId(1L))
            .thenReturn(Single.just(listOf(withPhone, withoutPhone)))

        val observer = TestObserver<GetTeamPhones.ResponseValue>()
        getTeamPhones.executeUseCase(GetTeamPhones.RequestValues()).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf("0102030405"), observer.values().first().phones)
    }
}
