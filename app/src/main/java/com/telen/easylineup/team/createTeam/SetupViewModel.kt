/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.telen.easylineup.R
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.views.TeamTypeCardItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SetupViewModel : ViewModel(), KoinComponent {
    private val domain: ApplicationInteractor by inject()
    private val _team: MutableLiveData<Team> = MutableLiveData()
    private var currentTeam = Team(0, "", null, TeamType.UNKNOWN.id, true)
    var errors: Subject<StepError> = PublishSubject.create()

    fun observeTeamName(): LiveData<String> {
        return _team.map { it.name }
    }

    fun observeTeamType(): LiveData<Int> {
        return _team.map { it.type }
    }

    fun observeTeamImage(): LiveData<Uri?> {
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
        return domain.teams().saveTeam(currentTeam)
    }

    fun setTeam(team: Team?) {
        team?.let {
            currentTeam = it
        }
        _team.postValue(currentTeam)
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
