/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.ports.PhoneNumberValidator
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.IsEmailValid
import com.telen.easylineup.domain.usecases.IsValidPhoneNumber
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerTests {
    @Mock
    lateinit var playerDao: PlayerRepository

    @Mock
    lateinit var teamDao: TeamRepository
    lateinit var savePlayer: SavePlayer

    @Before
    fun init() {
        runBlocking {
        savePlayer = SavePlayer(
            playerDao,
            GetTeam(teamDao, testDispatcherProvider()),
            IsEmailValid(),
            IsValidPhoneNumber(
                object : PhoneNumberValidator {
                    override fun isGlobalPhoneNumber(phone: String) = true
                }
            ),
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
        Mockito.`when`(playerDao.updatePlayer(any())).thenReturn(Unit)
        Mockito.`when`(playerDao.insertPlayer(any())).thenReturn(1)
        Mockito.`when`(playerDao.getPlayerById(any())).thenReturn(player)
        Mockito.`when`(teamDao.getTeams())
            .thenReturn(listOf(Team(id = 1L, name = "Panthers", main = true)))
    }
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

    private suspend fun save(
        useCase: SavePlayer = savePlayer,
        email: String? = null,
        phone: String? = null
    ) = useCase(
        playerId = 0L,
        name = "  Test  ",
        shirtNumber = null,
        image = null,
        positions = 1,
        pitching = 0,
        batting = 0,
        email = email,
        phone = phone,
        sex = 0
    )

    @Test
    fun shouldRefuseAnInvalidEmailWithoutSaving() = runTest {
        val result = save(email = "not an email")

        assertTrue(result.exceptionOrNull() is InvalidEmailException)
        verify(playerDao, never()).insertPlayer(any())
    }

    @Test
    fun shouldRefuseAnInvalidPhoneWithoutSaving() = runTest {
        val rejectingPhones = SavePlayer(
            playerDao,
            GetTeam(teamDao, testDispatcherProvider()),
            IsEmailValid(),
            IsValidPhoneNumber(
                object : PhoneNumberValidator {
                    override fun isGlobalPhoneNumber(phone: String) = false
                }
            ),
            testDispatcherProvider()
        )

        val result = save(useCase = rejectingPhones, phone = "abc")

        assertTrue(result.exceptionOrNull() is InvalidPhoneException)
        verify(playerDao, never()).insertPlayer(any())
    }

    @Test
    fun shouldDefaultMissingNumbersToZeroAndTrimTheName() = runTest {
        val captor = argumentCaptor<Player>()

        save()

        verify(playerDao).insertPlayer(captor.capture())
        assertEquals("Test", captor.firstValue.name)
        assertEquals(0, captor.firstValue.shirtNumber)
        assertEquals(0L, captor.firstValue.licenseNumber)
    }

    @Test
    fun shouldStoreANullLicenseNumberAsZero() = runTest {
        val captor = argumentCaptor<Player>()

        savePlayer(
            playerId = 0L,
            name = "Test",
            shirtNumber = 1,
            licenseNumber = null,
            image = null,
            positions = 1,
            pitching = 0,
            batting = 0,
            email = null,
            phone = null,
            sex = 0
        )

        verify(playerDao).insertPlayer(captor.capture())
        assertEquals(0L, captor.firstValue.licenseNumber)
    }
}
