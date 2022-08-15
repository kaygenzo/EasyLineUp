package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.telen.easylineup.R
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.domain.model.StepConfiguration
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamCreationStep
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.team.createTeam.teamType.TeamTypeCardItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SetupViewModel : ViewModel(), KoinComponent {

    enum class StepError {
        NAME_EMPTY,
        UNKNOWN
    }

    private val domain: ApplicationInteractor by inject()
    private val _team = MutableLiveData<Team>()
    private val _step = MutableLiveData<Pair<StepConfiguration, StepConfiguration>>()

    private var currentTeam = Team(0, "", null, TeamType.UNKNOWN.id, true)
    private val disposables = CompositeDisposable()
    private var currentStep = StepConfiguration()
    var errors = PublishSubject.create<StepError>()

    fun observeTeamName(): LiveData<String> {
        return Transformations.map(_team) { it.name }
    }

    fun observeTeamType(): LiveData<Int> {
        return Transformations.map(_team) { it.type }
    }

    fun observeTeamImage(): LiveData<Uri?> {
        return Transformations.map(_team) {
            it.takeIf { it.image != null }?.let { Uri.parse(it.image) }
        }
    }

    fun observeStep(): LiveData<Pair<StepConfiguration, StepConfiguration>> {
        return _step
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

    private fun saveTeam(): Completable {
        return domain.teams().saveTeam(currentTeam)
    }

    fun nextButtonClicked() {
        disposables.clear()
        val saveDisposable =
            domain.teams().getTeamCreationNextStep(currentStep.nextStep.id, currentTeam)
                .subscribe({
                    if (it.nextStep == TeamCreationStep.FINISH) {
                        disposables.add(saveTeam().subscribe({
                            onNewStep(it)
                        }, {
                            errors.onNext(StepError.UNKNOWN)
                        }))
                    } else {
                        onNewStep(it)
                    }
                }, {
                    if (it is NameEmptyException) {
                        errors.onNext(StepError.NAME_EMPTY)
                    } else {
                        errors.onNext(StepError.UNKNOWN)
                    }
                })
        disposables.add(saveDisposable)
    }

    fun previousButtonClicked() {
        disposables.clear()
        val saveDisposable =
            domain.teams().getTeamCreationPreviousStep(currentStep.nextStep.id, currentTeam)
                .subscribe({
                    _step.postValue(Pair(currentStep, it))
                    currentStep = it
                }, {
                    errors.onNext(StepError.UNKNOWN)
                })
        disposables.add(saveDisposable)
    }

    fun onCancelClicked() {
        nextButtonClicked()
    }

    private fun onNewStep(new: StepConfiguration) {
        _step.postValue(Pair(currentStep, new))
        currentStep = new
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
                TeamType.BASEBALL -> {
                    TeamTypeCardItem(
                        type.id, type.title, R.drawable.image_baseball_ball_with_stroke,
                        R.drawable.image_baseball_ball, type.sportResId
                    )
                }
                TeamType.SOFTBALL -> {
                    TeamTypeCardItem(
                        type.id, type.title, R.drawable.image_softball_ball_with_stroke,
                        R.drawable.image_softball_ball, type.sportResId
                    )
                }
                TeamType.BASEBALL_5 -> {
                    TeamTypeCardItem(
                        type.id, type.title, R.drawable.image_baseball_ball_with_stroke,
                        R.drawable.image_baseball_ball, type.sportResId
                    )
                }
                else -> {
                    Timber.e("Unknown team type $type")
                    null
                }
            }
        }
    }
}