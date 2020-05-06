package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.*
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

internal class CheckHashData(val teamDao: TeamRepository, val playerDao: PlayerRepository, val tournamentDao: TournamentRepository, val lineupDao: LineupRepository,
                val playerFieldPositionsDao: PlayerFieldPositionRepository): UseCase<CheckHashData.RequestValues, CheckHashData.ResponseValue>() {

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
            val toUpdate = mutableListOf<Team>()
            teams.forEach { team ->
                if(team.hash.isNullOrBlank()) {
                    team.hash = UUID.randomUUID().toString()
                    toUpdate.add(team)
                }
            }

            teamDao.updateTeamsWithRowCount(toUpdate)
        }
    }

    private fun updatePlayers(): Single<Int> {
        return playerDao.getPlayers().flatMap { players ->
            val toUpdate = mutableListOf<Player>()
            players.forEach { player ->
                if(player.hash.isNullOrBlank()) {
                    player.hash = UUID.randomUUID().toString()
                    toUpdate.add(player)
                }
            }

            playerDao.updatePlayersWithRowCount(toUpdate)
        }
    }

    private fun updateTournaments(): Single<Int> {
        return tournamentDao.getTournaments().flatMap { tournaments ->
            val toUpdate = mutableListOf<Tournament>()
            tournaments.forEach { tournament ->
                if(tournament.hash.isNullOrBlank()) {
                    tournament.hash = UUID.randomUUID().toString()
                    toUpdate.add(tournament)
                }
            }

            tournamentDao.updateTournamentsWithRowCount(toUpdate)
        }
    }

    private fun updateLineups(): Single<Int> {
        return lineupDao.getLineups().flatMap { lineups ->
            val toUpdate = mutableListOf<Lineup>()
            lineups.forEach { lineup ->
                if(lineup.hash.isNullOrBlank()) {
                    lineup.hash = UUID.randomUUID().toString()
                    toUpdate.add(lineup)
                }
            }

            lineupDao.updateLineupsWithRowCount(toUpdate)
        }
    }

    private fun updatePlayerFieldPositions(): Single<Int> {
        return playerFieldPositionsDao.getPlayerFieldPositions().flatMap { playerFieldPositions ->
            val toUpdate = mutableListOf<PlayerFieldPosition>()
            playerFieldPositions.forEach { playerFieldPosition ->
                if(playerFieldPosition.hash.isNullOrBlank()) {
                    playerFieldPosition.hash = UUID.randomUUID().toString()
                    toUpdate.add(playerFieldPosition)
                }
            }

            playerFieldPositionsDao.updatePlayerFieldPositionsWithRowCount(toUpdate)
        }
    }

    class ResponseValue(val updateResult: IntArray): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}