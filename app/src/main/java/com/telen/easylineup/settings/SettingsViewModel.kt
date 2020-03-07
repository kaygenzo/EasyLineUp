package com.telen.easylineup.settings

import androidx.lifecycle.ViewModel
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.DeleteAllData
import io.reactivex.Completable
import org.koin.core.KoinComponent
import org.koin.core.inject

class SettingsViewModel: ViewModel(), KoinComponent {
    private val deleteAllDataUseCase: DeleteAllData by inject()

    fun deleteAllData(): Completable {
        return UseCaseHandler.execute(deleteAllDataUseCase, DeleteAllData.RequestValues()).ignoreElement()
    }
}