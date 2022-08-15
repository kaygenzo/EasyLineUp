package com.telen.easylineup.domain.application.impl

import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.PlayerFieldPositionsInteractor
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.usecases.DeletePlayerFieldPosition
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SavePlayerFieldPosition
import com.telen.easylineup.domain.usecases.SwitchPlayersPosition
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.KoinComponent
import org.koin.core.inject

internal class PlayerFieldPositionsInteractorImpl : PlayerFieldPositionsInteractor, KoinComponent {

    private val playerFieldPositionRepo: PlayerFieldPositionRepository by inject()
    private val getTeam: GetTeam by inject()
    private val savePlayerFieldPosition: SavePlayerFieldPosition by inject()
    private val deletePlayerFieldPosition: DeletePlayerFieldPosition by inject()
    private val switchPlayersPosition: SwitchPlayersPosition by inject()

    private val errors: PublishSubject<DomainErrors.PlayerFieldPositions> = PublishSubject.create()

    override fun insertPlayerFieldPositions(playerFieldPositions: List<PlayerFieldPosition>):
            Completable {
        return playerFieldPositionRepo.insertPlayerFieldPositions(playerFieldPositions)
    }

    override fun savePlayerFieldPosition(
        player: Player,
        position: FieldPosition,
        list: List<PlayerWithPosition>,
        lineupID: Long?,
        lineupMode: Int,
        strategy: TeamStrategy,
        batterSize: Int,
        extraBatterSize: Int
    ): Completable {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val requestValues = SavePlayerFieldPosition.RequestValues(
                    lineupID = lineupID,
                    player = player,
                    position = position,
                    players = list,
                    lineupMode = lineupMode,
                    teamType = it.type,
                    strategy = strategy,
                    batterSize = batterSize,
                    extraHittersSize = extraBatterSize
                )
                UseCaseHandler.execute(savePlayerFieldPosition, requestValues)
            }
            .ignoreElement()
            .doOnError {
                errors.onNext(DomainErrors.PlayerFieldPositions.SAVE_PLAYER_FIELD_POSITION_FAILED)
            }
    }

    override fun deletePlayerPosition(
        player: Player,
        position: FieldPosition,
        list: List<PlayerWithPosition>,
        lineupMode: Int,
        extraHitterSize: Int
    ): Completable {
        val requestValues = DeletePlayerFieldPosition.RequestValues(
            list,
            player,
            position,
            lineupMode,
            extraHitterSize
        )
        return UseCaseHandler.execute(deletePlayerFieldPosition, requestValues)
            .ignoreElement()
            .doOnError {
                errors.onNext(DomainErrors.PlayerFieldPositions.DELETE_PLAYER_FIELD_POSITION_FAILED)
            }
    }

    override fun switchPlayersPosition(
        p1: FieldPosition,
        p2: FieldPosition,
        list: List<PlayerWithPosition>,
        lineupMode: Int,
        strategy: TeamStrategy,
        extraHittersSize: Int
    ): Completable {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap {
                val request = SwitchPlayersPosition.RequestValues(
                    players = list,
                    position1 = p1,
                    position2 = p2,
                    teamType = it.type,
                    lineupMode = lineupMode,
                    strategy = strategy,
                    extraHittersSize = extraHittersSize
                )
                UseCaseHandler.execute(switchPlayersPosition, request)
            }
            .ignoreElement()
    }

    override fun observeErrors(): Subject<DomainErrors.PlayerFieldPositions> {
        return errors
    }
}