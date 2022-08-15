package com.telen.easylineup.domain.application.impl

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.DataInteractor
import com.telen.easylineup.domain.mock.DatabaseMockProvider
import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

internal class DataInteractorImpl(private val context: Context) : DataInteractor,
    ValidationCallback, KoinComponent {

    private val getTeam: GetTeam by inject()
    private val getDashboardTiles: GetDashboardTiles by inject()
    private val updateTiles: SaveDashboardTiles by inject()
    private val createTiles: CreateDashboardTiles by inject()
    private val deleteAllData: DeleteAllData by inject()
    private val checkHash: CheckHashData by inject()
    private val mImporter: ImportData by inject()
    private val exportData: ExportData by inject()

    private val errors: PublishSubject<DomainErrors.Configuration> = PublishSubject.create()
    private val disposables = CompositeDisposable()

    override fun getDashboardConfigurations(): LiveData<List<DashboardTile>> {
        val resultLiveData = MutableLiveData<List<DashboardTile>>()
        val disposable = UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .flatMap { team ->
                val getDashboardUseCase =
                    UseCaseHandler.execute(getDashboardTiles, GetDashboardTiles.RequestValues(team))
                getDashboardUseCase.onErrorResumeNext {
                    if (it is NoSuchElementException) {
                        UseCaseHandler.execute(createTiles, CreateDashboardTiles.RequestValues())
                            .ignoreElement()
                            .andThen(getDashboardUseCase)
                    } else {
                        Single.error(it)
                    }
                }
                    .map { it.tiles }
            }
            .subscribe({
                resultLiveData.postValue(it)
            }, {
                errors.onNext(DomainErrors.Configuration.CANNOT_RETRIEVE_DASHBOARD)
            })
        disposables.add(disposable)
        return resultLiveData
    }

    override fun importData(root: ExportBase, updateIfExists: Boolean): Completable {
        return UseCaseHandler.execute(mImporter, ImportData.RequestValues(root, updateIfExists))
            .ignoreElement()
    }

    override fun deleteAllData(): Completable {
        return UseCaseHandler.execute(deleteAllData, DeleteAllData.RequestValues())
            .ignoreElement()
    }

    override fun exportData(dirUri: Uri): Single<String> {
        return UseCaseHandler.execute(checkHash, CheckHashData.RequestValues())
            .ignoreElement()
            .andThen(UseCaseHandler.execute(exportData, ExportData.RequestValues(this)))
            .flatMap {
                //TODO move into a dedicated class
                val now = Calendar.getInstance().time
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)
                val dateTime = formatter.format(now)
                val json = Gson().toJson(it.exportBase)
                val dir = DocumentFile.fromTreeUri(context, dirUri)
                val filename = "$dateTime.elu"
                val file = dir?.createFile("application/octet-stream", filename)
                file?.let {
                    context.contentResolver.openOutputStream(it.uri)?.let { os ->
                        Single.defer {
                            os.use { stream ->
                                stream.bufferedWriter().use { writer ->
                                    writer.write(json)
                                    writer.flush()
                                    Single.just(filename)
                                }
                            }
                        }
                    } ?: let {
                        errors.onNext(DomainErrors.Configuration.CANNOT_EXPORT_DATA)
                        Single.error(Exception("Cannot write in file"))
                    }
                } ?: let {
                    errors.onNext(DomainErrors.Configuration.CANNOT_EXPORT_DATA)
                    Single.error(Exception("Cannot open directory"))
                }
            }
    }

    override fun generateMockedData(): Completable {
        return DatabaseMockProvider().createMockDatabase(context)
    }

    override fun updateDashboardConfiguration(tiles: List<DashboardTile>): Completable {
        return UseCaseHandler.execute(updateTiles, SaveDashboardTiles.RequestValues(tiles))
            .ignoreElement()
    }

    override fun observeErrors(): Subject<DomainErrors.Configuration> {
        return errors
    }

    override fun isNetworkUrl(url: String?): Boolean {
        return URLUtil.isNetworkUrl(url)
    }

    override fun isDigitsOnly(value: String): Boolean {
        return value.isDigitsOnly()
    }

    override fun isBlank(value: String): Boolean {
        return value.isBlank()
    }
}