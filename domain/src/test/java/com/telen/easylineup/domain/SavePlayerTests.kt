/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.PhoneNumberValidator
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerTests {
    @Mock
    lateinit var playerDao: PlayerRepository

    @Mock
    lateinit var teamDao: TeamRepository
    lateinit var savePlayer: SavePlayer

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayer = SavePlayer(
            playerDao,
            GetTeam(teamDao, testSchedulersProvider()),
            ValidatorUtilsMock(),
            testDispatcherProvider()
        )
        val player = Player(
            id = 1L,
            teamId = 1,
            name = "Test",
            image = null,
            shirtNumber = 1,
            licenseNumber = 1L,
            positions = 1,
            pitching = 1,
            batting = 3,
            hash = "hash",
            email = "p1@test.com",
            phone = "001"
        )
        Mockito.`when`(playerDao.updatePlayer(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerDao.insertPlayer(any())).thenReturn(Single.just(1))
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(any())).thenReturn(Single.just(player))
        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "Panthers", main = true))))
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() = runTest {
        val result = savePlayer(
            playerId = 1L,
            name = "",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            image = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        assertTrue(result.exceptionOrNull() is NameEmptyException)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsWhitespaces() = runTest {
        val result = savePlayer(
            playerId = 1L,
            name = "     ",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            image = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        assertTrue(result.exceptionOrNull() is NameEmptyException)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsNull() = runTest {
        val result = savePlayer(
            playerId = 1L,
            name = null,
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            image = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        assertTrue(result.exceptionOrNull() is NameEmptyException)
    }

    @Test
    fun shouldInsertEventIfShirtNumberIsNull() = runTest {
        val result = savePlayer(
            playerId = 0L,
            name = "Test",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = null,
            image = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        assertTrue(result.isSuccess)
        verify(playerDao).insertPlayer(any())
    }

    @Test
    fun shouldInsertIfNewPlayer() = runTest {
        val result = savePlayer(
            playerId = 0L,
            name = "Test",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            image = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        assertTrue(result.isSuccess)
        verify(playerDao).insertPlayer(any())
        verify(playerDao, never()).updatePlayer(any())
    }

    @Test
    fun shouldUpdateIfKnownPlayer() = runTest {
        val result = savePlayer(
            playerId = 1L,
            name = "Test",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            image = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        assertTrue(result.isSuccess)
        verify(playerDao).updatePlayer(any())
        verify(playerDao, never()).insertPlayer(any())
    }

    class ValidatorUtilsMock : ValidatorUtils(
        object : PhoneNumberValidator {
            override fun isGlobalPhoneNumber(phone: String) = true
        }
    ) {
        override fun isEmailValid(email: String?): Boolean {
            return true
        }

        override fun isValidPhoneNumber(phone: String?): Boolean {
            return true
        }
    }
}
