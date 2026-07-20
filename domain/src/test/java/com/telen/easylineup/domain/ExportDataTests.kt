/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.toLineupExport
import com.telen.easylineup.domain.model.toPlayerExport
import com.telen.easylineup.domain.model.toPlayerFieldPositionsExport
import com.telen.easylineup.domain.model.toPlayerNumberOverlayExport
import com.telen.easylineup.domain.model.toTeamExport
import com.telen.easylineup.domain.model.toTournamentExport
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.CheckHashData
import com.telen.easylineup.domain.usecases.ExportData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class ExportDataTests {
    private val extraHitters = 0
    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerPositionsDao: PlayerFieldPositionRepository
    private lateinit var export: ExportData

    // team 1
    private lateinit var team1: Team
    private lateinit var player1: Player
    private lateinit var tournament1: Tournament
    private lateinit var lineup1: Lineup
    private lateinit var playerPosition1: PlayerFieldPosition
    private lateinit var playerNumberOverlay1: PlayerNumberOverlay

    // team 2
    private lateinit var team2: Team
    private lateinit var player2: Player
    private lateinit var tournament2: Tournament
    private lateinit var lineup2: Lineup
    private lateinit var playerPosition2: PlayerFieldPosition

    @Before
    fun init() {
        runBlocking {
        MockitoAnnotations.initMocks(this)
        val checkHashData = CheckHashData(
            teamDao, playerDao, tournamentDao, lineupDao, playerPositionsDao,
            testDispatcherProvider()
        )
        export = ExportData(
            checkHashData, teamDao, playerDao, tournamentDao, lineupDao, playerPositionsDao,
            testDispatcherProvider()
        )

        // team 1
        team1 = Team(1L, "A", null, 0, true, "I")
        player1 = Player(1L, 1L, "A", 1, 1L, null, 1, 0, 0, "p1@test.com", "001", 0, "A")
        lineup1 = Lineup(1L, "A", 1L, 1L, 0, TeamStrategy.STANDARD.id, extraHitters, 3L, 1L, 1L, "1", "E")
        tournament1 = Tournament(1L, "A", 1L, 2L, 3L, null, "C")
        playerPosition1 = PlayerFieldPosition(1L, 1L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE, "G")
        playerNumberOverlay1 = PlayerNumberOverlay(1L, 1L, 1L, 42, "I")

        // team 2
        team2 = Team(2L, "B", null, 0, false, "J")
        player2 = Player(2L, 2L, "B", 2, 2L, null, 1, 0, 0, "p2@test.com", "002", 0, "B")
        lineup2 = Lineup(2L, "B", 2L, 2L, 0, TeamStrategy.STANDARD.id, extraHitters, 3L, 1L, 1L, "2", "F")
        tournament2 = Tournament(2L, "B", 2L, 3L, 4L, null, "D")
        playerPosition2 = PlayerFieldPosition(2L, 2L, 2L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE, "H")

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(listOf(team1, team2))

        Mockito.`when`(playerDao.getPlayersByTeamId(1L)).thenReturn(listOf(player1))
        Mockito.`when`(playerDao.getPlayersByTeamId(2L)).thenReturn(listOf(player2))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(listOf(tournament1, tournament2))

        Mockito.`when`(lineupDao.getLineupsForTournamentRx(1L, 1L))
            .thenReturn(listOf(lineup1))
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(2L, 2L))
            .thenReturn(listOf(lineup2))

        Mockito.`when`(lineupDao.getLineupsForTournamentRx(1L, 2L))
            .thenReturn(listOf())
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(2L, 1L))
            .thenReturn(listOf())

        Mockito.`when`(playerPositionsDao.getAllPlayerFieldPositionsForLineup(1L))
            .thenReturn(listOf(playerPosition1))
        Mockito.`when`(playerPositionsDao.getAllPlayerFieldPositionsForLineup(2L))
            .thenReturn(listOf(playerPosition2))

        Mockito.`when`(playerDao.getPlayersNumberOverlay(1L))
            .thenReturn(listOf(playerNumberOverlay1))
        Mockito.`when`(playerDao.getPlayersNumberOverlay(2L))
            .thenReturn(listOf())

        // CheckHashData runs first - every fixture already has a non-blank hash, so it finds
        // nothing to update; these repository calls just need a value to not NPE.
        Mockito.`when`(playerDao.getPlayers()).thenReturn(listOf(player1, player2))
        Mockito.`when`(playerDao.updatePlayersWithRowCount(any())).thenReturn(0)
        Mockito.`when`(teamDao.updateTeamsWithRowCount(any())).thenReturn(0)
        Mockito.`when`(tournamentDao.updateTournamentsWithRowCount(any())).thenReturn(0)
        Mockito.`when`(lineupDao.getLineups()).thenReturn(listOf(lineup1, lineup2))
        Mockito.`when`(lineupDao.updateLineupsWithRowCount(any())).thenReturn(0)
        Mockito.`when`(playerPositionsDao.getPlayerFieldPositions())
            .thenReturn(listOf(playerPosition1, playerPosition2))
        Mockito.`when`(playerPositionsDao.updatePlayerFieldPositionsWithRowCount(any()))
            .thenReturn(0)
    }
    }

    @Test
    fun shouldExportAllData() = runTest {
        val result = export()
        Assert.assertTrue(result.isSuccess)

        val root = ExportBase(listOf(
            team1.toTeamExport(
                listOf(player1).map { it.toPlayerExport() },
                listOf(tournament1).map { it.toTournamentExport(
                    listOf(lineup1).map {
                        it.toLineupExport(
                            listOf(playerPosition1)
                                .map { it.toPlayerFieldPositionsExport(player1.hash) }
                                .toMutableList(),
                            listOf(playerNumberOverlay1)
                                .map { it.toPlayerNumberOverlayExport(player1.hash) }
                                .toMutableList(),
                            listOf("A")
                        )
                    }
                )
                }
            ),
            team2.toTeamExport(
                listOf(player2).map { it.toPlayerExport() },
                listOf(tournament2).map { it.toTournamentExport(
                    listOf(lineup2).map {
                        it.toLineupExport(
                            listOf(playerPosition2)
                                .map { it.toPlayerFieldPositionsExport(player2.hash) }
                                .toMutableList(),
                            listOf(),
                            listOf("B")
                        )
                    }
                )
                }
            )
        ))

        Assert.assertEquals(root, result.getOrNull())
    }

    @Test
    fun shouldNotExportTournament() = runTest {
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(1L, 1L)).thenReturn(listOf(lineup1))
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(2L, 2L)).thenReturn(listOf())

        val result = export()
        Assert.assertTrue(result.isSuccess)

        val root = ExportBase(listOf(
            team1.toTeamExport(
                listOf(player1).map { it.toPlayerExport() },
                listOf(tournament1).map { it.toTournamentExport(
                    listOf(lineup1).map {
                        it.toLineupExport(
                            listOf(playerPosition1)
                                .map { it.toPlayerFieldPositionsExport(player1.hash) }
                                .toMutableList(),
                            listOf(playerNumberOverlay1)
                                .map { it.toPlayerNumberOverlayExport(player1.hash) }
                                .toMutableList(),
                            listOf("A")
                        )
                    }
                )
                }
            ),
            team2.toTeamExport(
                listOf(player2).map { it.toPlayerExport() },
                listOf()
            )
        ))

        Assert.assertEquals(root, result.getOrNull())
    }

    @Test
    fun shouldNotExportNonWebUri() = runTest {
        team1.image = "file:///test.png"
        team2.image = ""
        player1.image = "http://test.com"
        player2.image = "https://test.com"

        val result = export()
        Assert.assertTrue(result.isSuccess)

        val exported = result.getOrNull()!!
        Assert.assertEquals(null, exported.teams[0].image)
        Assert.assertEquals(null, exported.teams[1].image)
        Assert.assertEquals(player1.image, exported.teams[0].players[0].image)
        Assert.assertEquals(player2.image, exported.teams[1].players[0].image)
    }

    @Test
    fun shouldExportAllDataWithMorePlayersOfSameTeamSameLineup() = runTest {
        val player3 = Player(3L, 1L, "A", 3, 3L, null, 1, 0, 0, "p3@test.com", "003", 0, "hash3")
        val player4 = Player(4L, 1L, "B", 4, 4L, null, 1, 0, 0, "p4@test.com", "004", 0, "hash4")
        lineup1.roster = "1;3;4"

        val playerPosition3 = PlayerFieldPosition(3L, 3L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE, "hash3")
        val playerPosition4 = PlayerFieldPosition(4L, 4L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE, "hash4")

        Mockito.`when`(playerDao.getPlayersByTeamId(1L)).thenReturn(listOf(player1, player3, player4))
        Mockito.`when`(playerPositionsDao.getAllPlayerFieldPositionsForLineup(1L))
            .thenReturn(listOf(playerPosition1, playerPosition3, playerPosition4))

        val result = export()
        Assert.assertTrue(result.isSuccess)

        val root = ExportBase(listOf(
            team1.toTeamExport(
                listOf(player1, player3, player4).map { it.toPlayerExport() },
                listOf(tournament1).map { it.toTournamentExport(
                    listOf(lineup1).map {
                        it.toLineupExport(
                            listOf(playerPosition1, playerPosition3, playerPosition4)
                                .map {
                                    when (it.playerId) {
                                        player1.id -> it.toPlayerFieldPositionsExport(player1.hash)
                                        player3.id -> it.toPlayerFieldPositionsExport(player3.hash)
                                        player4.id -> it.toPlayerFieldPositionsExport(player4.hash)
                                        else -> throw Exception("Not planned into test cases")
                                    }
                                }
                                .toMutableList(),
                            listOf(playerNumberOverlay1)
                                .map { it.toPlayerNumberOverlayExport(player1.hash) }
                                .toMutableList(),
                            listOf("A", "hash3", "hash4")
                        )
                    }
                )
                }
            ),
            team2.toTeamExport(
                listOf(player2).map { it.toPlayerExport() },
                listOf(tournament2).map { it.toTournamentExport(
                    listOf(lineup2).map {
                        it.toLineupExport(
                            listOf(playerPosition2)
                                .map { it.toPlayerFieldPositionsExport(player2.hash) }
                                .toMutableList(),
                            listOf(),
                            listOf("B")
                        )
                    }
                )
                }
            )
        ))

        Assert.assertEquals(root, result.getOrNull())
    }
}
