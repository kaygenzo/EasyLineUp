package com.telen.easylineup.domain.usecases

import android.view.View
import com.telen.easylineup.domain.R
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.StepConfiguration
import com.telen.easylineup.domain.model.TeamCreationStep
import io.reactivex.rxjava3.core.Single

internal abstract class GetTeamCreationStep: UseCase<GetTeamCreationStep.RequestValues, GetTeamCreationStep.ResponseValue>() {
    class ResponseValue(val config: StepConfiguration): UseCase.ResponseValue

    class RequestValues(val currentStep: TeamCreationStep): UseCase.RequestValues
}

internal class GetTeamCreationNextStep: GetTeamCreationStep() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val responseValue: ResponseValue = when(requestValues.currentStep){
            TeamCreationStep.CANCEL -> {
                ResponseValue(StepConfiguration(TeamCreationStep.TEAM,
                    nextButtonEnabled = true, previousButtonEnabled = true,
                    nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                    nextButtonLabel = R.string.team_creation_label_next,
                    previousButtonLabel = R.string.team_creation_label_cancel))
            }
            TeamCreationStep.TEAM -> {
                ResponseValue(StepConfiguration(TeamCreationStep.TYPE,
                        nextButtonEnabled = true, previousButtonEnabled = true,
                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                        nextButtonLabel = R.string.team_creation_label_finish,
                        previousButtonLabel = R.string.team_creation_label_previous))
            }
            TeamCreationStep.TYPE -> {
                ResponseValue(StepConfiguration(TeamCreationStep.FINISH,
                        nextButtonEnabled = false, previousButtonEnabled = false,
                        nextButtonVisibility = View.INVISIBLE, previousButtonVisibility = View.INVISIBLE,
                        nextButtonLabel = 0, previousButtonLabel = 0))
            }
            else -> {
                return Single.error(IllegalArgumentException())
            }
        }

        return Single.just(responseValue)
    }
}

internal class GetTeamCreationPreviousStep: GetTeamCreationStep() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val responseValue: ResponseValue = when(requestValues.currentStep){
            TeamCreationStep.TEAM -> {
                ResponseValue(StepConfiguration(TeamCreationStep.CANCEL,
                        nextButtonEnabled = false, previousButtonEnabled = false,
                        nextButtonVisibility = View.INVISIBLE, previousButtonVisibility = View.INVISIBLE,
                        nextButtonLabel = 0, previousButtonLabel = 0))
            }
            TeamCreationStep.TYPE -> {
                ResponseValue(StepConfiguration(TeamCreationStep.TEAM,
                        nextButtonEnabled = true, previousButtonEnabled = true,
                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                        nextButtonLabel = R.string.team_creation_label_next,
                        previousButtonLabel = R.string.team_creation_label_cancel))
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