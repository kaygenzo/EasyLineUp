package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_edit_text_simple.view.*

class CustomEditTextView: ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_edit_text_simple, this)
    }

    fun getName() : String {
        return textInputField.text.toString()
    }

    fun setPlaceholder(placeholder: String) {
        //textField.pla =
        textField.isHelperTextEnabled = true
        textField.helperText = placeholder
    }
}