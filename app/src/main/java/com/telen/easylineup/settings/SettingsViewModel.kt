package com.telen.easylineup.settings

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.github.kaygenzo.bugreporter.api.BugReporter
import com.github.kaygenzo.bugreporter.api.ReportMethod
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.reporting.getReportMethods
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

sealed class Event
object DeleteAllDataEventSuccess: Event()
object DeleteAllDataEventFailure: Event()
data class ExportDataEventSuccess(val pathDirectory: String): Event()
object ExportDataEventFailure: Event()

class SettingsViewModel: ViewModel(), KoinComponent {

    companion object {
        private const val REQUEST_CODE_PERMISSION_OVERLAY = 0
    }

    private val domain: ApplicationInteractor by inject()
    private val bugReporter: BugReporter by inject()

    private val _event = PublishSubject.create<Event>()
    private val disposables = CompositeDisposable()

    fun clear() {
        disposables.clear()
    }

    fun deleteAllData() {
        val disposable = domain.data().deleteAllData()
                .andThen(Completable.timer(1000, TimeUnit.MILLISECONDS))
                .subscribe({
                    _event.onNext(DeleteAllDataEventSuccess)
                }, {
                    Timber.e(it)
                    _event.onNext(DeleteAllDataEventFailure)
                })
        disposables.add(disposable)
    }

    /**
     * @return The directory name where the file is exported
     */
    fun exportData(dirUri: Uri) {
        val disposable = domain.data().exportData(dirUri)
                .subscribe({
                    _event.onNext(ExportDataEventSuccess(it))
                }, {
                    Timber.e(it)
                    _event.onNext(ExportDataEventFailure)
                })
        disposables.add(disposable)
    }

    fun observeEvent(): Subject<Event> {
        return _event
    }

    fun onReportMethodsChosen(activity: Activity) {
        val methods = activity.getReportMethods()
        bugReporter.setReportMethods(methods)
        bugReporter.restart()
        val hasPermission = bugReporter.hasPermissionOverlay(activity)
        if (methods.contains(ReportMethod.FLOATING_BUTTON) && !hasPermission) {
            bugReporter.askOverlayPermission(activity, REQUEST_CODE_PERMISSION_OVERLAY)
        }
    }
}