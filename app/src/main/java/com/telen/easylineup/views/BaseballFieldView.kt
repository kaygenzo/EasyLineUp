package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R

class BaseballFieldView: ConstraintLayout {
    constructor(context: Context?) : super(context) {
        init(context)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.field_view, this)
    }
}