package com.telen.easylineup.settings

import android.os.Environment
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.CheckHashData
import com.telen.easylineup.domain.DeleteAllData
import com.telen.easylineup.domain.ExportData
import com.telen.easylineup.repository.model.Constants
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel: ViewModel(), KoinComponent {

    private val deleteAllDataUseCase: DeleteAllData by inject()
    private val checkHashUseCase: CheckHashData by inject()
    private val exportDataUseCase: ExportData by inject()

    fun deleteAllData(): Completable {
        return UseCaseHandler.execute(deleteAllDataUseCase, DeleteAllData.RequestValues()).ignoreElement()
    }

    /**
     * @return The directory name where the file is exported
     */
    fun exportDataOnExternalMemory(): Single<String> {
        return UseCaseHandler.execute(checkHashUseCase, CheckHashData.RequestValues()).flatMapCompletable {
                    Timber.d("update result is ${it.updateResult}")
                    Completable.complete()
                }.andThen(UseCaseHandler.execute(exportDataUseCase, ExportData.RequestValues()))
                .flatMap {
                    val storageDirectoryName = "${Constants.BASE_ROOT_DIRECTORY}/exports"
                    val json = Gson().toJson(it.exportBase)

                    val rootDirectory = File(Environment.getExternalStorageDirectory().path
                            + "/" + storageDirectoryName)
                    if(!rootDirectory.exists())
                        rootDirectory.mkdirs()

                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
                    val dateTime = formatter.format(Calendar.getInstance().time)
                    val file = File(rootDirectory.absolutePath + "/$dateTime.elu")
                    if(!file.exists())
                        file.createNewFile()

                    var out: BufferedWriter? = null
                    try {
                        out = BufferedWriter(FileWriter(file.absolutePath, false))
                        out.write(json)
                        out.flush()
                    }
                    catch (e: Exception) {
                        Timber.e(e)
                    }
                    finally {
                        out?.close()
                    }
                    Single.just(storageDirectoryName)
                }
    }
}