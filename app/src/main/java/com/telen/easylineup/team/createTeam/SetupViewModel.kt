/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.views.TeamTypeCardItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SetupViewModel : ViewModel(), KoinComponent {
    private val saveTeamUseCase: SaveTeam by inject()
    private val _team: MutableSharedFlow<Team> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    private var currentTeam = Team(0, "", null, TeamType.UNKNOWN.id, true)
    var errors: Subject<StepError> = PublishSubject.create()

    fun observeTeamName(): Flow<String> {
        return _team.map { it.name }
    }

    fun observeTeamType(): Flow<Int> {
        return _team.map { it.type }
    }

    fun observeTeamImage(): Flow<Uri?> {
        return _team.map {
            it.takeIf { it.image != null }?.let { Uri.parse(it.image) }
        }
    }

    fun setTeamName(name: String) {
        currentTeam.name = name
    }

    fun setTeamImage(image: String?) {
        currentTeam.image = image
    }

    fun setTeamType(position: Int) {
        TeamType.values().firstOrNull { it.position == position }?.let {
            currentTeam.type = it.id
        }
    }

    fun onSaveClicked(): Completable {
        return saveTeamUseCase(currentTeam).ignoreElement()
    }

    fun setTeam(team: Team?) {
        team?.let {
            currentTeam = it
        }
        _team.tryEmit(currentTeam)
    }

    fun getTeamTypeCardItems(): List<TeamTypeCardItem> {
        return TeamType.values().mapNotNull { type ->
            when (type) {
                TeamType.BASEBALL -> TeamTypeCardItem(
                    type.id, type.title, R.drawable.image_baseball_ball_with_stroke,
                    R.drawable.image_baseball_ball, type.sportResId
                )
                TeamType.SOFTBALL -> TeamTypeCardItem(
                    type.id, type.title, R.drawable.image_softball_ball_with_stroke,
                    R.drawable.image_softball_ball, type.sportResId
                )
                TeamType.BASEBALL_5 -> TeamTypeCardItem(
                    type.id, type.title, R.drawable.image_baseball_ball_with_stroke,
                    R.drawable.image_baseball_ball, type.sportResId
                )
                else -> {
                    Timber.e("Unknown team type $type")
                    null
                }
            }
        }
    }

    enum class StepError {
        NAME_EMPTY,
    }
}
