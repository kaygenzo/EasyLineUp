/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayer
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.withContext

/**
 * Saves which players are on a lineup's roster. A player taken off the roster is no longer
 * part of that game, so they also leave the lineup itself: field, batting order and bench.
 * Removal goes through [DeletePlayerFieldPosition], the same rule as removing them by hand, so
 * a DP/Flex pair is unlinked and a batting substitute's slot passes to the next substitute.
 */
class UpdateLineupRoster(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository,
    private val deletePlayerFieldPosition: DeletePlayerFieldPosition,
    private val saveBattingOrderAndPositions: SaveBattingOrderAndPositions,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineupId: Long, roster: List<RosterPlayerStatus>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                transactionRunner.inTransaction {
                    val lineup = lineupRepository.getLineupById(lineupId)
                        .copy(roster = rosterToString(roster))
                    val removedIds = roster.filterNot { it.status }.map { it.player.id }.toSet()
                    val players = pfpRepository.getAllPlayersWithPositionsForLineup(lineupId)
                    val toRemove = players.filter { it.playerId in removedIds && it.isAssigned() }
                    if (toRemove.isEmpty()) {
                        lineupRepository.updateLineup(lineup)
                        return@inTransaction
                    }
                    toRemove.forEach { player ->
                        // A field player first goes back to the bench, then leaves it: two steps,
                        // exactly like removing them twice by hand.
                        repeat(2) {
                            if (!player.isAssigned()) return@repeat
                            deletePlayerFieldPosition(
                                players,
                                player.toPlayer(),
                                lineup.mode,
                                lineup.extraHitters
                            ).getOrThrow()
                        }
                    }
                    saveBattingOrderAndPositions(lineup, players).getOrThrow()
                }
            }
        }

    private fun rosterToString(list: List<RosterPlayerStatus>): String {
        val builder = StringBuilder()
        list.forEach {
            if (it.status) {
                if (builder.isNotEmpty()) {
                    builder.append(";")
                }
                builder.append(it.player.id)
            }
        }
        return builder.toString()
    }
}
