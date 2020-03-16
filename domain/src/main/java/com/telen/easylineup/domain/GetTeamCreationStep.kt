package com.telen.easylineup.domain

import android.view.View
import androidx.annotation.StringRes
import io.reactivex.Single

enum class TeamCreationStep(val id: Int) {
    TEAM(0), TYPE(1), FINISH(2), CANCEL(3);

    companion object {
        fun getStepById(id: Int): TeamCreationStep? {
            return values().firstOrNull { it.id == id }
        }
    }
}

abstract class GetTeamCreationStep: UseCase<GetTeamCreationStep.RequestValues, GetTeamCreationStep.ResponseValue>() {
    class ResponseValue(val nextStep: TeamCreationStep, val nextButtonEnabled: Boolean, val previousButtonEnabled: Boolean,
                        val nextButtonVisibility: Int, val previousButtonVisibility: Int, @StringRes val nextButtonLabel: Int,
                        @StringRes val previousButtonLabel: Int): UseCase.ResponseValue

    class RequestValues(val currentStep: TeamCreationStep): UseCase.RequestValues
}

class GetTeamCreationNextStep: GetTeamCreationStep() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val responseValue: ResponseValue = when(requestValues.currentStep){
            TeamCreationStep.TEAM -> {
                ResponseValue(TeamCreationStep.TYPE,
                        nextButtonEnabled = true, previousButtonEnabled = true,
                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                        nextButtonLabel = R.string.team_creation_label_finish,
                        previousButtonLabel = R.string.team_creation_label_previous)
            }
            TeamCreationStep.TYPE -> {
                ResponseValue(TeamCreationStep.FINISH,
                        nextButtonEnabled = false, previousButtonEnabled = false,
                        nextButtonVisibility = View.INVISIBLE, previousButtonVisibility = View.INVISIBLE,
                        nextButtonLabel = 0, previousButtonLabel = 0)
            }
            else -> {
                return Single.error(IllegalArgumentException())
            }
        }

        return Single.just(responseValue)
    }
}

class GetTeamCreationPreviousStep: GetTeamCreationStep() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val responseValue: ResponseValue = when(requestValues.currentStep){
            TeamCreationStep.TEAM -> {
                ResponseValue(TeamCreationStep.CANCEL,
                        nextButtonEnabled = false, previousButtonEnabled = false,
                        nextButtonVisibility = View.INVISIBLE, previousButtonVisibility = View.INVISIBLE,
                        nextButtonLabel = 0, previousButtonLabel = 0)
            }
            TeamCreationStep.TYPE -> {
                ResponseValue(TeamCreationStep.TEAM,
                        nextButtonEnabled = true, previousButtonEnabled = true,
                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                        nextButtonLabel = R.string.team_creation_label_next,
                        previousButtonLabel = R.string.team_creation_label_cancel)
            }
//            TeamCreationStep.PLAYERS -> {
//                ResponseValue(TeamCreationStep.TYPE,
//                        nextButtonEnabled = true, previousButtonEnabled = true,
//                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
//                        nextButtonLabel = R.string.team_creation_label_next,
//                        previousButtonLabel = R.string.team_creation_label_previous)
//            }
            else -> {
                return Single.error(IllegalArgumentException())
            }
        }

        return Single.just(responseValue)
    }
}