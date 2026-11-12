/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.ports.repository.PlayerNumberOverlayRepository
import com.telen.easylineup.domain.ports.repository.PlayerRepository
import com.telen.easylineup.domain.ports.repository.TeamRepository
import com.telen.easylineup.domain.ports.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.export.json.writeExportFile
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.inject
import com.telen.easylineup.utils.ImportFileReaderImpl
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * An `.elu` file is how a coach backs up or moves their whole season, so this runs the real chain
 * end to end - use cases, Gson wire format, a file on disk, real Room database - and compares
 * what comes back row by row. Every piece has its own mocked test elsewhere; only this one proves
 * they agree with each other, for instance that a position still points at the right player
 * once every id has been regenerated.
 *
 * Not covered: [com.telen.easylineup.utils.ExportFileWriterImpl], which needs a real folder
 * picked through the system's document picker. The file is written directly here instead.
 */
@RunWith(AndroidJUnit4::class)
class ExportImportRoundTripTest {
    @get:Rule
    val seedRule = SeedDatabaseRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val exportData: ExportData by inject()
    private val importData: ImportData by inject()
    private val deleteAllData: DeleteAllData by inject()
    private val teams: TeamRepository by inject()
    private val players: PlayerRepository by inject()
    private val tournaments: TournamentRepository by inject()
    private val lineups: LineupRepository by inject()
    private val positions: PlayerFieldPositionRepository by inject()
    private val overlays: PlayerNumberOverlayRepository by inject()

    /** Every field that must survive, keyed by hash since ids are regenerated on import. */
    private data class Snapshot(
        val teams: Set<List<Any?>>,
        val players: Set<List<Any?>>,
        val tournaments: Set<List<Any?>>,
        val lineups: Set<List<Any?>>,
        val positions: Set<List<Any?>>,
        val overlays: Set<List<Any?>>
    )

    private suspend fun snapshot(): Snapshot {
        val playerHashes = players.getPlayers().associate { it.id to it.hash }
        val allLineups = lineups.getLineups()
        val lineupHashes = allLineups.associate { it.id to it.hash }
        val tournamentHashes = tournaments.getTournaments().associate { it.id to it.hash }
        return Snapshot(
            teams = teams.getTeams().map { listOf(it.hash, it.name, it.type) }.toSet(),
            players = players.getPlayers().map {
                listOf(it.hash, it.name, it.shirtNumber, it.licenseNumber, it.positions, it.sex)
            }.toSet(),
            tournaments = tournaments.getTournaments().map {
                listOf(it.hash, it.name, it.startTime, it.endTime, it.address)
            }.toSet(),
            lineups = allLineups.map {
                listOf(
                    it.hash,
                    it.name,
                    tournamentHashes[it.tournamentId],
                    it.mode,
                    it.strategy,
                    it.extraHitters,
                    it.eventTimeInMillis
                )
            }.toSet(),
            positions = positions.getPlayerFieldPositions().map {
                listOf(
                    it.hash,
                    playerHashes[it.playerId],
                    lineupHashes[it.lineupId],
                    it.position,
                    it.x,
                    it.y,
                    it.order,
                    it.flags
                )
            }.toSet(),
            overlays = allLineups.flatMap { overlays.getPlayersNumberOverlay(it.id) }.map {
                listOf(it.hash, playerHashes[it.playerId], lineupHashes[it.lineupId], it.number)
            }.toSet()
        )
    }

    private suspend fun exportToFile(): File {
        val export = exportData().getOrThrow()
        return File(context.cacheDir, "round_trip.elu").apply {
            bufferedWriter().use { writeExportFile(export, it) }
        }
    }

    private fun read(file: File) = ImportFileReaderImpl(context).read(Uri.fromFile(file).toString())

    @Before
    fun addANumberOverlay() = runBlocking {
        val fixture = seedRule.fixture
        overlays.insertPlayerNumberOverlay(
            PlayerNumberOverlay(
                lineupId = fixture.lineupId,
                playerId = fixture.playerIds.first(),
                number = 42
            )
        )
    }

    @Test
    fun importingAnExportIntoAnEmptyDatabaseRestoresEveryRow() = runBlocking {
        val file = exportToFile()
        val before = snapshot()
        assertEquals(12, before.players.size)
        assertEquals(12, before.positions.size)
        assertEquals(1, before.overlays.size)

        deleteAllData().getOrThrow()
        assertTrue(teams.getTeams().isEmpty())
        importData(read(file), updateIfExists = false).getOrThrow()

        assertEquals(before, snapshot())
    }

    @Test
    fun importingTwiceDoesNotDuplicateAnything() = runBlocking {
        val file = exportToFile()
        val before = snapshot()

        val result = importData(read(file), updateIfExists = false).getOrThrow()

        assertEquals(before, snapshot())
        assertEquals(0, result.teams.inserted)
        assertEquals(0, result.players.inserted)
        assertEquals(0, result.lineups.inserted)
        assertEquals(0, result.playerPositions.inserted)
        assertEquals(0, result.playerNumberOverlays.inserted)
    }

    @Test
    fun importingTheSameFileAgainAfterARestoreDoesNotDuplicateAnything() = runBlocking {
        val file = exportToFile()
        val before = snapshot()
        deleteAllData().getOrThrow()
        importData(read(file), updateIfExists = false).getOrThrow()

        importData(read(file), updateIfExists = false).getOrThrow()

        assertEquals(before, snapshot())
    }

    @Test
    fun importingWithoutUpdateKeepsLocalChanges() = runBlocking {
        val file = exportToFile()
        val player = players.getPlayerById(seedRule.fixture.playerIds.first())
        players.updatePlayer(player.copy(name = "Renamed Locally"))

        importData(read(file), updateIfExists = false).getOrThrow()

        assertEquals("Renamed Locally", players.getPlayerById(player.id).name)
    }

    @Test
    fun importingWithUpdateOverwritesLocalChanges() = runBlocking {
        val file = exportToFile()
        val before = snapshot()
        val player = players.getPlayerById(seedRule.fixture.playerIds.first())
        players.updatePlayer(player.copy(name = "Renamed Locally"))

        val result = importData(read(file), updateIfExists = true).getOrThrow()

        assertEquals(before, snapshot())
        assertEquals(12, result.players.updated)
    }
}
