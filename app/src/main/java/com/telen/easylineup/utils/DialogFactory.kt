package com.telen.easylineup.utils

import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog
import com.telen.easylineup.R
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DialogFactory {
    companion object {
        fun getWarningDialog(context: Context, title: String, content: String, task: Completable): SweetAlertDialog {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(content)
                    .setConfirmText(context.getString(android.R.string.yes))
                    .setCancelText(context.getString(android.R.string.cancel))
                    .setConfirmClickListener { sDialog ->
                        sDialog.setTitleText(R.string.dialog_delete_progress_message)
                                .showContentText(false)
                                .showCancelButton(false)
                                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE)

                        sDialog.setCancelable(false)

                        task.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    sDialog.setTitleText("")
                                            .hideConfirmButton()
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)

                                    Completable.timer(1000, TimeUnit.MILLISECONDS)
                                            .subscribe {
                                                sDialog.dismiss()
                                            }
                                }, { throwable ->
                                    Timber.e(throwable)
                                })
                    }
            return dialog
        }

        fun getSimpleDialog(context: Context, title: String): SweetAlertDialog {
            val dialog = SweetAlertDialog(context)
                    .setTitleText(title)
            return dialog
        }

        fun getErrorDialog(context: Context, title: String): SweetAlertDialog {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(title)
            return dialog
        }
    }
}