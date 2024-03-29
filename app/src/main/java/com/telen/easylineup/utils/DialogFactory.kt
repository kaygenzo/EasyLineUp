/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telen.easylineup.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

import java.util.concurrent.TimeUnit

class DialogFactory {
    companion object {
        private val defaultClickListener = DialogInterface.OnClickListener { dialog, which -> }

        fun getDiscardDialog(
            context: Context,
            confirmClick: DialogInterface.OnClickListener?
        ): Dialog {
            return getWarningDialog(
                context,
                title = R.string.discard_change_title,
                message = R.string.discard_change_message,
                confirmClick = confirmClick
            )
        }

        fun getWarningDialog(
            context: Context,
            @StringRes title: Int, titleArgs: Array<Any> = arrayOf(),
            @StringRes message: Int, messageArgs: Array<Any> = arrayOf(),
            @StringRes confirmText: Int = android.R.string.ok,
            confirmClick: DialogInterface.OnClickListener? = null,
            cancelClick: DialogInterface.OnClickListener? = null
        ): Dialog {
            return getDialog(
                context = context,
                title = title,
                titleArgs = titleArgs,
                content = message,
                contentArgs = messageArgs,
                resIcon = R.drawable.ic_warning_orange_24dp,
                confirmClick = confirmClick,
                cancelClick = cancelClick ?: defaultClickListener,
                resConfirmText = confirmText
            )
        }

        fun getWarningTaskDialog(
            context: Context,
            @StringRes title: Int = 0,
            titleArgs: Array<Any> = arrayOf(),
            @StringRes message: Int = 0,
            messageArgs: Array<Any> = arrayOf(),
            task: Completable,
            @StringRes messageLoading: Int = R.string.dialog_delete_progress_message
        ): Dialog {
            return getWarningDialog(
                context = context,
                title = title,
                titleArgs = titleArgs,
                message = message,
                messageArgs = messageArgs,
                confirmClick = DialogInterface.OnClickListener { dialog, which ->

                    dialog.dismiss()

                    // val loadingDialog = getLoadingDialog(context, messageLoading)
                    // loadingDialog.show()

                    task.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Completable.timer(1_000, TimeUnit.MILLISECONDS)
                                .subscribe {
                                    // loadingDialog.dismiss()
                                    dialog.dismiss()
                                }
                        }, { throwable ->
                            Timber.e(throwable)
                        })
                }

            )
        }

        fun getSuccessDialog(
            context: Context,
            @StringRes title: Int = 0, titleArgs: Array<Any> = arrayOf(),
            @StringRes message: Int = 0, messageArgs: Array<Any> = arrayOf()
        ): Dialog {
            return getDialog(
                context = context,
                title = title,
                titleArgs = titleArgs,
                content = message,
                contentArgs = messageArgs,
                resIcon = R.drawable.ic_check_green_24dp,
                resCancelText = 0
            )
        }

        fun getSimpleDialog(
            context: Context,
            @StringRes title: Int = 0,
            @StringRes message: Int = 0,
            view: View? = null,
            confirmClick: DialogInterface.OnClickListener? = null,
            cancelClick: DialogInterface.OnClickListener? = null,
            @StringRes confirmText: Int = android.R.string.ok,
            @StringRes cancelText: Int = android.R.string.cancel,
            cancelable: Boolean = true
        ): Dialog {
            return getDialog(
                context = context,
                title = title,
                content = message,
                confirmClick = confirmClick,
                cancelClick = cancelClick ?: defaultClickListener,
                resConfirmText = confirmText,
                customView = view,
                cancelable = cancelable,
                resCancelText = cancelText
            )
        }

        fun getErrorDialog(
            context: Context,
            @StringRes title: Int,
            @StringRes message: Int,
            confirmClick: DialogInterface.OnClickListener? = null
        ): Dialog {
            return getDialog(
                context = context,
                title = title,
                content = message,
                resIcon = R.drawable.ic_warning_red_24dp,
                resCancelText = 0,
                confirmClick = confirmClick
            )
        }

        private fun getDialog(
            context: Context,
            @StringRes title: Int = 0,
            titleArgs: Array<Any> = arrayOf(),
            @StringRes content: Int = 0,
            contentArgs: Array<Any> = arrayOf(),
            @DrawableRes resIcon: Int = 0,
            @StringRes resConfirmText: Int = android.R.string.ok,
            @StringRes resCancelText: Int = android.R.string.cancel,
            confirmClick: DialogInterface.OnClickListener? = null,
            cancelClick: DialogInterface.OnClickListener? = null,
            customView: View? = null,
            cancelable: Boolean? = true
        ): Dialog {
            val dialog = MaterialAlertDialogBuilder(context)
                .setIcon(resIcon)

            if (title != 0) {
                dialog.setTitle(context.getString(title, *titleArgs))
            }
            if (content != 0) {
                dialog.setMessage(context.getString(content, *contentArgs))
            }
            if (resConfirmText != 0) {
                dialog.setPositiveButton(resConfirmText, confirmClick)
            }
            if (resCancelText != 0) {
                dialog.setNegativeButton(resCancelText, cancelClick)
                dialog.setOnCancelListener { listener -> cancelClick?.onClick(listener, 0) }
            }
            customView?.let {
                dialog.setView(customView)
            }
            cancelable?.let {
                dialog.setCancelable(it)
            }
            return dialog.create()
        }

        fun getMultiChoiceDialog(
            context: Context,
            @StringRes title: Int,
            items: Array<CharSequence>,
            checkedItems: BooleanArray,
            listener: DialogInterface.OnMultiChoiceClickListener? = null,
            confirmClick: DialogInterface.OnClickListener? = null
        ): Dialog {
            return MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMultiChoiceItems(items, checkedItems, listener)
                .setPositiveButton(android.R.string.ok, confirmClick)
                .create()
        }

        fun getListDialog(
            context: Context, items: Array<CharSequence>,
            listener: DialogInterface.OnClickListener? = null
        ): Dialog {
            return MaterialAlertDialogBuilder(context)
                .setItems(items, listener)
                .create()
        }
    }
}
