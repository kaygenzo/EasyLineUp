package com.telen.easylineup.domain

import android.content.SharedPreferences
import android.view.View
import androidx.annotation.StringRes
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Constants
import io.reactivex.Single
import java.lang.IllegalArgumentException

class GetTeamCreationNextStep: UseCase<GetTeamCreationNextStep.RequestValues, GetTeamCreationNextStep.ResponseValue>() {

    enum class TeamCreationStep(val id: Int) {
        TEAM(0), TYPE(1), PLAYERS(2), FINISH(3);

        companion object {
            fun getStepById(id: Int): TeamCreationStep? {
                return values().firstOrNull { it.id == id }
            }
        }
    }

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val responseValue: ResponseValue = when(requestValues.currentStep){
//            TeamCreationStep.START -> {
//                ResponseValue(TeamCreationStep.TEAM,
//                        nextButtonEnabled = true, previousButtonEnabled = false,
//                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.INVISIBLE,
//                        nextButtonLabel = 0, previousButtonLabel = 0)
//            }
            TeamCreationStep.TEAM -> {
                ResponseValue(TeamCreationStep.TYPE,
                        nextButtonEnabled = true, previousButtonEnabled = true,
                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                        nextButtonLabel = R.string.team_creation_label_next, previousButtonLabel = 0)
            }
            TeamCreationStep.TYPE -> {
                ResponseValue(TeamCreationStep.PLAYERS,
                        nextButtonEnabled = true, previousButtonEnabled = true,
                        nextButtonVisibility = View.VISIBLE, previousButtonVisibility = View.VISIBLE,
                        nextButtonLabel = R.string.team_creation_label_finish, previousButtonLabel = 0)
            }
            TeamCreationStep.PLAYERS -> {
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

    class ResponseValue(val nextStep: TeamCreationStep, val nextButtonEnabled: Boolean, val previousButtonEnabled: Boolean,
                        val nextButtonVisibility: Int, val previousButtonVisibility: Int, @StringRes val nextButtonLabel: Int,
                        @StringRes val previousButtonLabel: Int): UseCase.ResponseValue

    class RequestValues(val currentStep: TeamCreationStep): UseCase.RequestValues
}