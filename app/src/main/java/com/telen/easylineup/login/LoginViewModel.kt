package com.telen.easylineup.login

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class LoginViewModel : ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    fun importData(dataPath: String, updateIfExists: Boolean): Completable {
        var input : BufferedReader? = null
        return try {
            input = BufferedReader(FileReader(File(dataPath)))
            val data = Gson().fromJson(input, ExportBase::class.java)
            domain.importData(data, updateIfExists)
        }
        catch (e: Exception) {
            Completable.error(e)
        }
        finally {
            input?.close()
        }
    }

    fun getMainTeam(): Single<Team> {
        return domain.getTeam()
    }
}