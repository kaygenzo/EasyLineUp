/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerExport
import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import com.telen.easylineup.domain.model.export.PlayerPositionExport
import com.telen.easylineup.domain.model.export.TeamExport
import com.telen.easylineup.domain.model.export.TournamentExport
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.ImportResult
import kotlinx.coroutines.runBlocking
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
internal class ImportDataTests {

    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerFieldPositionsDao: PlayerFieldPositionRepository

    lateinit var importData: ImportData

    private val teamHash = "team-hash"
    private val playerHash = "player-hash"
    private val tournamentHash = "tournament-hash"
    private val lineupHash = "lineup-hash"
    private val positionHash = "position-hash"
    private val overlayHash = "overlay-hash"

    private lateinit var exportBase: ExportBase

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        importData = ImportData(
            teamDao,
            playerDao,
            tournamentDao,
            lineupDao,
            playerFieldPositionsDao,
            testDispatcherProvider()
        )

        val playerExport = PlayerExport(
            id = playerHash,
            name = "toto",
            image = null,
            shirtNumber = 42,
            licenseNumber = "123",
            positions = 0,
            pitching = 0,
            batting = 0,
            email = "toto@mail.com",
            phone = "0102030405",
            sex = 0
        )

        val positionExport = PlayerPositionExport(
            id = positionHash,
            playerId = playerHash,
            position = 1,
            x = 0.1f,
            y = 0.2f,
            flags = 0,
            order = 1
        )

        val overlayExport = PlayerNumberOverlayExport(
            id = overlayHash,
            playerId = playerHash,
            number = 99
        )

        val lineupExport = LineupExport(
            id = lineupHash,
            name = "lineup",
            eventTime = 1000L,
            createdAt = 1000L,
            editedAt = 1000L,
            mode = 0,
            strategy = 0,
            extraHitters = 0,
            roster = listOf(playerHash),
            playerPositions = listOf(positionExport),
            playerNumberOverlays = listOf(overlayExport)
        )

        val tournamentExport = TournamentExport(
            id = tournamentHash,
            name = "champs",
            createdAt = 1000L,
            startTime = 1000L,
            endTime = 2000L,
            address = null,
            lineups = listOf(lineupExport)
        )

        val teamExport = TeamExport(
            id = teamHash,
            name = "team",
            image = null,
            type = 0,
            main = true,
            players = listOf(playerExport),
            tournaments = listOf(tournamentExport)
        )

        exportBase = ExportBase(teams = listOf(teamExport))
    }
    }

    private suspend fun mockAllNotFound() {
        Mockito.`when`(teamDao.getTeamByHash(teamHash)).thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerDao.getPlayerByHash(playerHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(tournamentDao.getTournamentByHash(tournamentHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(lineupDao.getLineupByHash(lineupHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerFieldPositionsDao.getPlayerFieldPositionByHash(positionHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerDao.getPlayerNumberOverlayByHash(overlayHash))
            .thenAnswer { throw NoSuchElementException() }
    }

    private suspend fun mockAllInsertsSucceed() {
        Mockito.`when`(teamDao.insertTeam(any())).thenReturn(1L)
        Mockito.`when`(playerDao.insertPlayer(any())).thenReturn(2L)
        Mockito.`when`(tournamentDao.insertTournament(any())).thenReturn(3L)
        Mockito.`when`(lineupDao.insertLineup(any())).thenReturn(4L)
        Mockito.`when`(playerFieldPositionsDao.insertPlayerFieldPosition(any()))
            .thenReturn(5L)
        Mockito.`when`(playerDao.createPlayerNumberOverlay(any())).thenReturn(Unit)
    }

    private suspend fun mockAllFound() {
        Mockito.`when`(teamDao.getTeamByHash(teamHash))
            .thenReturn(Team(id = 1L, name = "team", hash = teamHash))
        Mockito.`when`(playerDao.getPlayerByHash(playerHash))
            .thenReturn(
                
                    Player(
                        id = 2L,
                        teamId = 1L,
                        name = "toto",
                        shirtNumber = 42,
                        licenseNumber = 123L,
                        hash = playerHash
                    )
                
            )
        Mockito.`when`(tournamentDao.getTournamentByHash(tournamentHash))
            .thenReturn(
                
                    Tournament(
                        id = 3L,
                        name = "champs",
                        createdAt = 1000L,
                        startTime = 1000L,
                        endTime = 2000L,
                        hash = tournamentHash
                    )
                
            )
        Mockito.`when`(lineupDao.getLineupByHash(lineupHash))
            .thenReturn(
                
                    Lineup(id = 4L, teamId = 1L, tournamentId = 3L, hash = lineupHash)
                
            )
        Mockito.`when`(playerFieldPositionsDao.getPlayerFieldPositionByHash(positionHash))
            .thenReturn(
                
                    PlayerFieldPosition(id = 5L, playerId = 2L, lineupId = 4L, hash = positionHash)
                
            )
        Mockito.`when`(playerDao.getPlayerNumberOverlayByHash(overlayHash))
            .thenReturn(
                
                    PlayerNumberOverlay(id = 6L, lineupId = 4L, playerId = 2L, number = 99)
                
            )
    }

    private suspend fun mockAllUpdatesSucceed() {
        Mockito.`when`(teamDao.updateTeam(any())).thenReturn(Unit)
        Mockito.`when`(playerDao.updatePlayer(any())).thenReturn(Unit)
        Mockito.`when`(tournamentDao.updateTournament(any())).thenReturn(Unit)
        Mockito.`when`(lineupDao.updateLineup(any())).thenReturn(Unit)
        Mockito.`when`(playerFieldPositionsDao.updatePlayerFieldPosition(any()))
            .thenReturn(Unit)
        Mockito.`when`(playerDao.updatePlayerNumberOverlay(any())).thenReturn(Unit)
    }

    @Test
    fun shouldInsertEverythingWhenNothingExists() = runTest {
        mockAllNotFound()
        mockAllInsertsSucceed()

        val importResult = importData(exportBase, updateIfExists = false)

        assertTrue(importResult.isSuccess)
        val result = importResult.getOrNull()!!
        assertEquals(listOf(1, 1, 1, 1, 1, 1), result.inserted.toList())
        assertEquals(listOf(0, 0, 0, 0, 0, 0), result.updated.toList())

        Mockito.verify(teamDao).insertTeam(any())
        Mockito.verify(playerDao).insertPlayer(any())
        Mockito.verify(tournamentDao).insertTournament(any())
        Mockito.verify(lineupDao).insertLineup(any())
        Mockito.verify(playerFieldPositionsDao).insertPlayerFieldPosition(any())
        Mockito.verify(playerDao).createPlayerNumberOverlay(any())
    }

    @Test
    fun shouldMapEmailAndPhoneWhenInsertingPlayer() = runTest {
        mockAllNotFound()
        mockAllInsertsSucceed()

        importData(exportBase, updateIfExists = false)

        val captor = argumentCaptor<Player>()
        Mockito.verify(playerDao).insertPlayer(captor.capture())
        assertEquals("toto@mail.com", captor.firstValue.email)
        assertEquals("0102030405", captor.firstValue.phone)
    }

    @Test
    fun shouldBuildRosterFromInsertedPlayerIds() = runTest {
        mockAllNotFound()
        mockAllInsertsSucceed()

        importData(exportBase, updateIfExists = false)

        val captor = argumentCaptor<Lineup>()
        Mockito.verify(lineupDao).insertLineup(captor.capture())
        assertEquals("2", captor.firstValue.roster)
    }

    @Test
    fun shouldUpdateEverythingWhenAllExistAndUpdateIfExistsTrue() = runTest {
        mockAllFound()
        mockAllUpdatesSucceed()

        val importResult = importData(exportBase, updateIfExists = true)

        assertTrue(importResult.isSuccess)
        val result = importResult.getOrNull()!!
        assertEquals(listOf(0, 0, 0, 0, 0, 0), result.inserted.toList())
        assertEquals(listOf(1, 1, 1, 1, 1, 1), result.updated.toList())

        Mockito.verify(teamDao).updateTeam(any())
        Mockito.verify(playerDao).updatePlayer(any())
        Mockito.verify(tournamentDao).updateTournament(any())
        Mockito.verify(lineupDao).updateLineup(any())
        Mockito.verify(playerFieldPositionsDao).updatePlayerFieldPosition(any())
        Mockito.verify(playerDao).updatePlayerNumberOverlay(any())

        Mockito.verify(teamDao, Mockito.never()).insertTeam(any())
        Mockito.verify(playerDao, Mockito.never()).insertPlayer(any())
        Mockito.verify(tournamentDao, Mockito.never()).insertTournament(any())
        Mockito.verify(lineupDao, Mockito.never()).insertLineup(any())
        Mockito.verify(playerFieldPositionsDao, Mockito.never()).insertPlayerFieldPosition(any())
        Mockito.verify(playerDao, Mockito.never()).createPlayerNumberOverlay(any())
    }

    @Test
    fun shouldSkipEverythingWhenAllExistAndUpdateIfExistsFalse() = runTest {
        mockAllFound()

        val importResult = importData(exportBase, updateIfExists = false)

        assertTrue(importResult.isSuccess)
        val result = importResult.getOrNull()!!
        assertEquals(listOf(0, 0, 0, 0, 0, 0), result.inserted.toList())
        assertEquals(listOf(0, 0, 0, 0, 0, 0), result.updated.toList())

        Mockito.verify(teamDao, Mockito.never()).insertTeam(any())
        Mockito.verify(teamDao, Mockito.never()).updateTeam(any())
        Mockito.verify(playerDao, Mockito.never()).insertPlayer(any())
        Mockito.verify(playerDao, Mockito.never()).updatePlayer(any())
    }

    @Test
    fun shouldReturnAllZerosForEmptyExport() = runTest {
        val importResult = importData(ExportBase(teams = listOf()), updateIfExists = false)

        assertTrue(importResult.isSuccess)
        val result = importResult.getOrNull()!!
        assertEquals(listOf(0, 0, 0, 0, 0, 0), result.inserted.toList())
        assertEquals(listOf(0, 0, 0, 0, 0, 0), result.updated.toList())
    }

    @Test
    fun shouldDefaultLicenseNumberToZeroWhenNotParsable() = runTest {
        val teamExport = exportBase.teams.first()
        val brokenPlayer = teamExport.players.first().copy(licenseNumber = "not-a-number")
        val brokenExport = ExportBase(
            teams = listOf(teamExport.copy(players = listOf(brokenPlayer)))
        )

        Mockito.`when`(teamDao.getTeamByHash(teamHash)).thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerDao.getPlayerByHash(playerHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(tournamentDao.getTournamentByHash(any())).thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(lineupDao.getLineupByHash(any())).thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerFieldPositionsDao.getPlayerFieldPositionByHash(any()))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerDao.getPlayerNumberOverlayByHash(any()))
            .thenAnswer { throw NoSuchElementException() }
        mockAllInsertsSucceed()

        val importResult = importData(brokenExport, updateIfExists = false)

        assertTrue(importResult.isSuccess)
        val captor = argumentCaptor<Player>()
        Mockito.verify(playerDao).insertPlayer(captor.capture())
        assertEquals(0L, captor.firstValue.licenseNumber)
    }

    @Test
    fun shouldPropagateInsertErrors() = runTest {
        mockAllNotFound()
        val exception = Exception("db error")
        Mockito.`when`(teamDao.insertTeam(any())).thenAnswer { throw exception }

        val importResult = importData(exportBase, updateIfExists = false)

        assertEquals(exception.message, importResult.exceptionOrNull()?.message)
    }

    /**
     * Documents a potential issue: [teamDao.getTeamByHash] failing for any reason (not only
     * "not found") is silently interpreted as "team does not exist yet" because of the
     * catch-all try/catch, so an unrelated repository error still triggers an insert
     * attempt instead of propagating the original error.
     */
    @Test
    fun shouldAttemptInsertEvenWhenLookupFailsWithAnUnrelatedError() = runTest {
        Mockito.`when`(teamDao.getTeamByHash(teamHash))
            .thenAnswer { throw RuntimeException("connection lost") }
        Mockito.`when`(playerDao.getPlayerByHash(playerHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(tournamentDao.getTournamentByHash(tournamentHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(lineupDao.getLineupByHash(lineupHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerFieldPositionsDao.getPlayerFieldPositionByHash(positionHash))
            .thenAnswer { throw NoSuchElementException() }
        Mockito.`when`(playerDao.getPlayerNumberOverlayByHash(overlayHash))
            .thenAnswer { throw NoSuchElementException() }
        mockAllInsertsSucceed()

        val importResult = importData(exportBase, updateIfExists = false)

        assertTrue(importResult.isSuccess)
        Mockito.verify(teamDao).insertTeam(any())
    }
}
