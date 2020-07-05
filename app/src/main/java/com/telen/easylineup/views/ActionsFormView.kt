package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_actions_form.view.*

class ActionsFormView: ConstraintLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_actions_form, this)
    }

    fun setOnSaveClickListener(listener: OnClickListener) {
        saveButton.setOnClickListener(listener)
    }

    fun setOnCancelClickListener(listener: OnClickListener) {
        cancelButton.setOnClickListener(listener)
    }
}