/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.repository.TournamentRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.UUID

/**
 * @property teamDao
 * @property playerDao
 * @property tournamentDao
 * @property lineupDao
 * @property playerFieldPositionsDao
 */
internal class CheckHashData(
    val teamDao: TeamRepository,
    val playerDao: PlayerRepository,
    val tournamentDao: TournamentRepository,
    val lineupDao: LineupRepository,
    val playerFieldPositionsDao: PlayerFieldPositionRepository
) : UseCase<CheckHashData.RequestValues, CheckHashData.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val result = intArrayOf(0, 0, 0, 0, 0)
        return updateTeams().flatMapCompletable {
            result[0] = it
            Completable.complete()
        }
            .andThen(updatePlayers().flatMapCompletable {
                result[1] = it
                Completable.complete()
            })
            .andThen(updateTournaments().flatMapCompletable {
                result[2] = it
                Completable.complete()
            })
            .andThen(updateLineups().flatMapCompletable {
                result[3] = it
                Completable.complete()
            })
            .andThen(updatePlayerFieldPositions().flatMapCompletable {
                result[4] = it
                Completable.complete()
            })
            .andThen(Single.just(ResponseValue(result)))
    }

    private fun updateTeams(): Single<Int> {
        return teamDao.getTeamsRx().flatMap { teams ->
            val toUpdate: MutableList<Team> = mutableListOf()
            teams.forEach { team ->
                if (team.hash.isNullOrBlank()) {
                    team.hash = UUID.randomUUID().toString()
                    toUpdate.add(team)
                }
            }

            teamDao.updateTeamsWithRowCount(toUpdate)
        }
    }

    private fun updatePlayers(): Single<Int> {
        return playerDao.getPlayers().flatMap { players ->
            val toUpdate: MutableList<Player> = mutableListOf()
            players.forEach { player ->
                if (player.hash.isNullOrBlank()) {
                    player.hash = UUID.randomUUID().toString()
                    toUpdate.add(player)
                }
            }

            playerDao.updatePlayersWithRowCount(toUpdate)
        }
    }

    private fun updateTournaments(): Single<Int> {
        return tournamentDao.getTournaments().flatMap { tournaments ->
            val toUpdate: MutableList<Tournament> = mutableListOf()
            tournaments.forEach { tournament ->
                if (tournament.hash.isNullOrBlank()) {
                    tournament.hash = UUID.randomUUID().toString()
                    toUpdate.add(tournament)
                }
            }

            tournamentDao.updateTournamentsWithRowCount(toUpdate)
        }
    }

    private fun updateLineups(): Single<Int> {
        return lineupDao.getLineups().flatMap { lineups ->
            val toUpdate: MutableList<Lineup> = mutableListOf()
            lineups.forEach { lineup ->
                if (lineup.hash.isNullOrBlank()) {
                    lineup.hash = UUID.randomUUID().toString()
                    toUpdate.add(lineup)
                }
            }

            lineupDao.updateLineupsWithRowCount(toUpdate)
        }
    }

    private fun updatePlayerFieldPositions(): Single<Int> {
        return playerFieldPositionsDao.getPlayerFieldPositions().flatMap { playerFieldPositions ->
            val toUpdate: MutableList<PlayerFieldPosition> = mutableListOf()
            playerFieldPositions.forEach { playerFieldPosition ->
                if (playerFieldPosition.hash.isNullOrBlank()) {
                    playerFieldPosition.hash = UUID.randomUUID().toString()
                    toUpdate.add(playerFieldPosition)
                }
            }

            playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(toUpdate)
        }
    }

    /**
     * @property updateResult
     */
    class ResponseValue(val updateResult: IntArray) : UseCase.ResponseValue
    class RequestValues : UseCase.RequestValues
}
