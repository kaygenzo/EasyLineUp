package com.telen.easylineup.domain.application.impl

import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.PlayerFieldPositionsInteractor
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.AssignPlayerFieldPosition
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import io.reactivex.rxjava3.core.Completable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class PlayerFieldPositionsInteractorImpl : PlayerFieldPositionsInteractor, KoinComponent {

    private val playerFieldPositionRepo: PlayerFieldPositionRepository by inject()
    private val getTeam: GetTeam by inject()
    private val savePlayerFieldPosition: AssignPlayerFieldPosition by inject()
    private val deletePlayerFieldPosition: DeletePlayerFieldPosition by inject()
    private val switchPlayersPosition: SwitchPlayersPosition by inject()

    override fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>):
            Completable {
        return playerFieldPositionRepo.insertPlayerFieldPositions(playerFieldPositions)
    }

    override fun savePlayerFieldPosition(
        player: Player,
        position: FieldPosition,
        lineup: Lineup,
        list: List<PlayerWithPosition>
    ): Completable {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val requestValues = AssignPlayerFieldPosition.RequestValues(
                    lineup = lineup,
                    player = player,
                    position = position,
                    players = list,
                    teamType = it.type
                )
                UseCaseHandler.execute(savePlayerFieldPosition, requestValues)
            }
            .ignoreElement()
    }

    override fun deletePlayerPosition(
        player: Player,
        list: List<PlayerWithPosition>,
        lineupMode: Int,
        extraHitterSize: Int
    ): Completable {
        val requestValues = DeletePlayerFieldPosition.RequestValues(
            list,
            player,
            lineupMode,
            extraHitterSize
        )
        return UseCaseHandler.execute(deletePlayerFieldPosition, requestValues).ignoreElement()
    }

    override fun switchPlayersPosition(
        p1: FieldPosition,
        p2: FieldPosition,
        list: List<PlayerWithPosition>,
        lineup: Lineup
    ): Completable {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val request = SwitchPlayersPosition.RequestValues(
                    players = list,
                    position1 = p1,
                    position2 = p2,
                    teamType = it.type,
                    lineup = lineup
                )
                UseCaseHandler.execute(switchPlayersPosition, request)
            }
            .ignoreElement()
    }
}