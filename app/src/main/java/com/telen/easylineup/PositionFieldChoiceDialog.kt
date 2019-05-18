package com.telen.easylineup

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_field_position_picker.*

interface OnPositionListener {
    fun onPositionChosen(fieldPosition: FieldPosition)
}

class PositionFieldChoiceDialog(context: Context, val positionListener: OnPositionListener?) : AlertDialog(context), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> {
                val index = fieldPositionsPicker.currentItemPosition
                positionListener?.onPositionChosen(FieldPosition.values()[index])
            }
            BUTTON_NEGATIVE -> {
                dismiss()
            }
        }
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_field_position_picker, null)
        setView(view)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this)
        setCancelable(false)
    }
}