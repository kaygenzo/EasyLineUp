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

class DialogFactory {
    companion object {
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
    }
}
