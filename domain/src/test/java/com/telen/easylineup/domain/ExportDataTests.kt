package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.repository.*
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.ValidationCallback
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
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

    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerPositionsDao: PlayerFieldPositionRepository

    @Mock lateinit var validator: ValidationCallback

    private lateinit var mExport: ExportData

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

    private val extraHitters = 0

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mExport = ExportData(teamDao, playerDao, tournamentDao, lineupDao, playerPositionsDao)

        //team 1
        team1 = Team(1L, "A", null, 0, true, "I")
        player1 = Player(1L, 1L, "A", 1, 1L, null, 1, 0, 0, "p1@test.com", "001","A")
        lineup1 = Lineup(1L, "A", 1L, 1L, 0, TeamStrategy.STANDARD.id, extraHitters, 3L, 1L, 1L, "1", "E")
        tournament1 = Tournament(1L, "A", 1L, "C")
        playerPosition1 = PlayerFieldPosition(1L, 1L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE,"G")
        playerNumberOverlay1 = PlayerNumberOverlay(1L, 1L, 1L, 42, "I")

        // team 2
        team2 = Team(2L, "B", null, 0, false, "J")
        player2 = Player(2L, 2L, "B", 2, 2L, null, 1, 0, 0, "p2@test.com", "002", "B")
        lineup2 = Lineup(2L, "B", 2L, 2L, 0, TeamStrategy.STANDARD.id, extraHitters, 3L, 1L, 1L, "2", "F")
        tournament2 = Tournament(2L, "B", 2L, "D")
        playerPosition2 = PlayerFieldPosition(2L, 2L, 2L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE,"H")

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team1, team2)))

        Mockito.`when`(playerDao.getPlayers(1L)).thenReturn(Single.just(listOf(player1)))
        Mockito.`when`(playerDao.getPlayers(2L)).thenReturn(Single.just(listOf(player2)))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(Single.just(listOf(tournament1, tournament2)))

        Mockito.`when`(lineupDao.getLineupsForTournamentRx(1L, 1L))
                .thenReturn(Single.just(listOf(lineup1)))
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(2L, 2L))
                .thenReturn(Single.just(listOf(lineup2)))

        Mockito.`when`(lineupDao.getLineupsForTournamentRx(1L, 2L))
                .thenReturn(Single.just(listOf()))
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(2L, 1L))
                .thenReturn(Single.just(listOf()))

        Mockito.`when`(playerPositionsDao.getAllPlayerFieldPositionsForLineup(1L))
                .thenReturn(Single.just(listOf(playerPosition1)))
        Mockito.`when`(playerPositionsDao.getAllPlayerFieldPositionsForLineup(2L))
                .thenReturn(Single.just(listOf(playerPosition2)))

        Mockito.`when`(playerDao.getPlayersNumberOverlay(1L))
                .thenReturn(Single.just(listOf(playerNumberOverlay1)))
        Mockito.`when`(playerDao.getPlayersNumberOverlay(2L))
                .thenReturn(Single.just(listOf()))

        Mockito.`when`(validator.isDigitsOnly(any())).thenReturn(true)
        Mockito.`when`(validator.isBlank(any())).thenReturn(false)
    }

    @Test
    fun shouldExportAllData() {

        val observer = TestObserver<ExportData.ResponseValue>()
        mExport.executeUseCase(ExportData.RequestValues(validator))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

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
                        ) }
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
                        ) }
                )
        ))

        Assert.assertEquals(root, observer.values().first().exportBase)
    }

    @Test
    fun shouldNotExportTournament() {

        Mockito.`when`(lineupDao.getLineupsForTournamentRx(1L, 1L)).thenReturn(Single.just(listOf(lineup1)))
        Mockito.`when`(lineupDao.getLineupsForTournamentRx(2L, 2L)).thenReturn(Single.just(listOf()))

        val observer = TestObserver<ExportData.ResponseValue>()
        mExport.executeUseCase(ExportData.RequestValues(validator))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

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
                        ) }
                ),
                team2.toTeamExport(
                        listOf(player2).map { it.toPlayerExport() },
                        listOf()
                )
        ))

        Assert.assertEquals(root, observer.values().first().exportBase)
    }

    @Test
    fun shouldNotExportNonWebUri() {

        team1.image = "file:///test.png"
        team2.image = ""
        player1.image = "http://test.com"
        player2.image = "https://test.com"

        Mockito.`when`(validator.isNetworkUrl(team1.image)).thenReturn(false)
        Mockito.`when`(validator.isNetworkUrl(team2.image)).thenReturn(false)
        Mockito.`when`(validator.isNetworkUrl(player1.image)).thenReturn(true)
        Mockito.`when`(validator.isNetworkUrl(player2.image)).thenReturn(true)

        val observer = TestObserver<ExportData.ResponseValue>()
        mExport.executeUseCase(ExportData.RequestValues(validator))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        Assert.assertEquals(null, observer.values().first().exportBase.teams[0].image)
        Assert.assertEquals(null, observer.values().first().exportBase.teams[1].image)
        Assert.assertEquals(player1.image, observer.values().first().exportBase.teams[0].players[0].image)
        Assert.assertEquals(player2.image, observer.values().first().exportBase.teams[1].players[0].image)
    }

    @Test
    fun shouldExportAllDataWithMorePlayersOfSameTeamSameLineup() {

        val player3 = Player(3L, 1L, "A", 3, 3L, null, 1, 0, 0, "p3@test.com", "003", "hash3")
        val player4 = Player(4L, 1L, "B", 4, 4L, null, 1, 0, 0, "p4@test.com", "004", "hash4")
        lineup1.roster = "1;3;4"

        val playerPosition3 = PlayerFieldPosition(3L, 3L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE,"hash3")
        val playerPosition4 = PlayerFieldPosition(4L, 4L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE,"hash4")

        Mockito.`when`(playerDao.getPlayers(1L)).thenReturn(Single.just(listOf(player1, player3, player4)))
        Mockito.`when`(playerPositionsDao.getAllPlayerFieldPositionsForLineup(1L))
                .thenReturn(Single.just(listOf(playerPosition1, playerPosition3, playerPosition4)))

        val observer = TestObserver<ExportData.ResponseValue>()
        mExport.executeUseCase(ExportData.RequestValues(validator))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()

        val root = ExportBase(listOf(
                team1.toTeamExport(
                        listOf(player1, player3, player4).map { it.toPlayerExport() },
                        listOf(tournament1).map { it.toTournamentExport(
                                listOf(lineup1).map {
                                    it.toLineupExport(
                                            listOf(playerPosition1, playerPosition3, playerPosition4)
                                                    .map {
                                                        when(it.playerId) {
                                                            player1.id -> {
                                                                it.toPlayerFieldPositionsExport(player1.hash)
                                                            }
                                                            player3.id -> {
                                                                it.toPlayerFieldPositionsExport(player3.hash)
                                                            }
                                                            player4.id -> {
                                                                it.toPlayerFieldPositionsExport(player4.hash)
                                                            }
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
                        ) }
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
                        ) }
                )
        ))

        Assert.assertEquals(root, observer.values().first().exportBase)
    }
}