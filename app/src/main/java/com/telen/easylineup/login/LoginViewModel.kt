package com.telen.easylineup.login

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.ImportData
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.export.ExportBase
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class LoginViewModel : ViewModel(), KoinComponent {

    private val mImporterUseCase: ImportData by inject()
    private val mGetTeamUseCase: GetTeam by inject()

    fun importData(dataPath: String, updateIfExists: Boolean): Completable {
        var input : BufferedReader? = null
        return try {
            input = BufferedReader(FileReader(File(dataPath)))
            val data = Gson().fromJson(input, ExportBase::class.java)
            UseCaseHandler.execute(mImporterUseCase, ImportData.RequestValues(data, updateIfExists)).flatMapCompletable {
                Timber.d("Inserted: ${it.inserted.contentToString()} Updated: ${it.updated.contentToString()}")
                Completable.complete()
            }
        }
        catch (e: Exception) {
            Completable.error(e)
        }
        finally {
            input?.close()
        }
    }

    fun getMainTeam(): Single<Team> {
        return UseCaseHandler.execute(mGetTeamUseCase, GetTeam.RequestValues()).map { it.team }
    }
}