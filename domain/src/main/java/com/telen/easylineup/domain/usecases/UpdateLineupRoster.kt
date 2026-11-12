/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.ports.Clock
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.withContext

/**
 * Saves which players are on a lineup's roster, straight to the database - for screens that
 * edit the roster on its own, outside a lineup edit session. A player taken off the roster is
 * no longer part of that game, so they also leave the lineup itself: field, batting order and
 * bench. The rule itself is [ApplyRosterToLineup]'s, shared with the lineup editor, which
 * applies it to its unsaved draft instead.
 */
class UpdateLineupRoster(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository,
    private val applyRosterToLineup: ApplyRosterToLineup,
    private val saveBattingOrderAndPositions: SaveBattingOrderAndPositions,
    private val transactionRunner: TransactionRunner,
    private val clock: Clock,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineupId: Long, roster: List<RosterPlayerStatus>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                transactionRunner.inTransaction {
                    val players = pfpRepository.getAllPlayersWithPositionsForLineup(lineupId)
                    val removedIds = roster.filterNot { it.status }.map { it.player.id }.toSet()
                    val removesAPlayer =
                        players.any { it.playerId in removedIds && it.isAssigned() }
                    val draft = applyRosterToLineup(
                        lineupRepository.getLineupById(lineupId),
                        players,
                        roster
                    ).getOrThrow()
                    if (removesAPlayer) {
                        saveBattingOrderAndPositions(draft.lineup, draft.players).getOrThrow()
                    } else {
                        lineupRepository.updateLineup(
                            draft.lineup.copy(editedTimeInMillis = clock.nowMillis())
                        )
                    }
                }
            }
        }
}
