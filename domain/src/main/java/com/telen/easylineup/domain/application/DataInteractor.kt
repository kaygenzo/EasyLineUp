package com.telen.easylineup.domain.application

import android.net.Uri
import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.export.ExportBase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.Subject

interface DataInteractor {
    fun getDashboardConfigurations(): LiveData<List<DashboardTile>>
    fun importData(root: ExportBase, updateIfExists: Boolean): Completable
    fun deleteAllData(): Completable
    fun exportData(dirUri: Uri): Single<String>
    fun generateMockedData(): Completable
    fun updateDashboardConfiguration(tiles: List<DashboardTile>): Completable
    fun observeErrors(): Subject<DomainErrors.Configuration>
}