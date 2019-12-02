package com.telen.easylineup.team.createTeam

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.TeamType
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class NameEmptyException: Exception()

class SetupViewModel: ViewModel() {

    private val getTeamUseCase = GetTeam(App.database.teamDao(), App.prefs)

    enum class NextStep(val id: Int) {
        TEAM(0), TYPE(1), PLAYERS(2), FINISH(3);
    }

    enum class Error {
        NAME_EMPTY,
        UNKNOWN
    }

    enum class BottomBarState {
        NEXT_ENABLED,
        NEXT_DISABLED,
        NEXT_FINISH
    }

    private var teamName: String = ""
    private var teamImage: String? = null
    private var teamType: TeamType = TeamType.BASEBALL

    private var nameLiveData = MutableLiveData<String>()
    private var imageLiveData = MutableLiveData<String>()

    fun registerNameObserver(): LiveData<String> {
        return nameLiveData
    }

    fun registerImageObserver(): LiveData<String> {
        return imageLiveData
    }

    fun setTeamName(name: String) {
        this.teamName = name
        if(name.isNotEmpty())
            nameLiveData.value = name
    }

    fun setTeamImage(image: String?) {
        this.teamImage = image
        imageLiveData.value = image
    }

    private var saveDisposable: Disposable? = null

    var stepLiveData: MutableLiveData<NextStep> = MutableLiveData()
    var bottomBarLiveData: MutableLiveData<BottomBarState> = MutableLiveData()
    var errorLiveData: MutableLiveData<Error> = MutableLiveData()

    fun saveTeam(): Completable {
        bottomBarLiveData.value = BottomBarState.NEXT_DISABLED
        val name = teamName
        val image = teamImage

        return if(!TextUtils.isEmpty(name.trim())) {
            val teamID = App.prefs.getLong(Constants.PREF_CURRENT_TEAM_ID, 1)
            val team = Team(id = teamID, name = name, image = image, type = teamType.id)

            if(team.id == 0L) {
                App.database.teamDao().insertTeam(team).ignoreElement()
            }
            else {
                App.database.teamDao().updateTeam(team)
            }
        }
        else
            Completable.error(NameEmptyException())
    }

    fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map {
            it.team
        }
    }

    fun nextButtonClicked(currentStep: Int) {
        when(currentStep){
            NextStep.TEAM.id -> {
                dispose(saveDisposable)
                saveDisposable = saveTeam()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            stepLiveData.value = NextStep.TYPE
                            bottomBarLiveData.value = BottomBarState.NEXT_ENABLED
                        }, {
                            bottomBarLiveData.value = BottomBarState.NEXT_ENABLED
                            if(it is NameEmptyException) {
                                errorLiveData.value = Error.NAME_EMPTY
                            }
                            else {
                                errorLiveData.value = Error.UNKNOWN
                            }
                        })
            }
            NextStep.TYPE.id -> {
                dispose(saveDisposable)
                saveDisposable = saveTeam()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            stepLiveData.value = NextStep.PLAYERS
                            bottomBarLiveData.value = BottomBarState.NEXT_ENABLED
                            bottomBarLiveData.value = BottomBarState.NEXT_FINISH
                        }, {
                            bottomBarLiveData.value = BottomBarState.NEXT_ENABLED
                            errorLiveData.value = Error.UNKNOWN
                        })
            }
            NextStep.PLAYERS.id -> {
                stepLiveData.value = NextStep.FINISH
            }
            else -> {}
        }
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }

    fun backPressed() {

    }

    fun getTeamType(): LiveData<TeamType> {
        val liveDataType = MutableLiveData<TeamType>()
        liveDataType.value = teamType
        return liveDataType
    }

    fun setTeamType(position: Int) {
        TeamType.values().forEach {
            if(it.position == position) {
                teamType = it
            }
        }
    }

}