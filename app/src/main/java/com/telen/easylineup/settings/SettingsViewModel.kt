package com.telen.easylineup.settings

import android.os.Environment
import android.webkit.URLUtil
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.CheckHashData
import com.telen.easylineup.domain.DeleteAllData
import com.telen.easylineup.domain.ExportData
import com.telen.easylineup.domain.ValidationCallback
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

data class ExportDataObject(val fallbackName: String)

class SettingsViewModel: ViewModel(), KoinComponent {

    private val deleteAllDataUseCase: DeleteAllData by inject()
    private val checkHashUseCase: CheckHashData by inject()
    private val exportDataUseCase: ExportData by inject()

    private val _exportDataObjectLiveData = MutableLiveData<ExportDataObject>()
    val exportDataObjectLiveData: LiveData<ExportDataObject> = _exportDataObjectLiveData

    fun deleteAllData(): Completable {
        return UseCaseHandler.execute(deleteAllDataUseCase, DeleteAllData.RequestValues()).ignoreElement()
    }

    fun exportDataTriggered() {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
        val dateTime = formatter.format(now)
        _exportDataObjectLiveData.value = ExportDataObject(dateTime)
    }

    /**
     * @return The directory name where the file is exported
     */
    fun exportDataOnExternalMemory(name: String, fallbackName: String): Single<String> {
        return UseCaseHandler.execute(checkHashUseCase, CheckHashData.RequestValues()).flatMapCompletable {
                    Timber.d("update result is ${it.updateResult}")
                    Completable.complete()
                }.andThen(UseCaseHandler.execute(exportDataUseCase, ExportData.RequestValues(object : ValidationCallback {
                    override fun isNetworkUrl(url: String?): Boolean {
                        return URLUtil.isNetworkUrl(url)
                    }

                    override fun isDigitsOnly(value: String): Boolean {
                        return value.isDigitsOnly()
                    }

                    override fun isBlank(value: String): Boolean {
                        return value.isBlank()
                    }
                })))
                .flatMap {
                    val storageDirectoryName = Constants.EXPORTS_DIRECTORY
                    val json = Gson().toJson(it.exportBase)

                    val rootDirectory = File(Environment.getExternalStorageDirectory().path
                            + "/" + storageDirectoryName)
                    if(!rootDirectory.exists())
                        rootDirectory.mkdirs()

                    val fileName = if(name.isNotBlank()) name else fallbackName
                    val file = File(rootDirectory.absolutePath + "/$fileName.elu")
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