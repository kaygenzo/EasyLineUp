package com.telen.easylineup.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.application.ApplicationPort
import io.reactivex.Completable
import io.reactivex.Single
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

data class ExportDataObject(val fallbackName: String)

class SettingsViewModel: ViewModel(), KoinComponent {

    private val domain: ApplicationPort by inject()

    private val _exportDataObjectLiveData = MutableLiveData<ExportDataObject>()
    val exportDataObjectLiveData: LiveData<ExportDataObject> = _exportDataObjectLiveData

    fun deleteAllData(): Completable {
        return domain.deleteAllData()
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
        return domain.exportDataOnExternalMemory(name, fallbackName)
    }
}