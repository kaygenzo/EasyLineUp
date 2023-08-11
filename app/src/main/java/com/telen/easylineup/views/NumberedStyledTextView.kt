package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.R

class NumberedStyledTextView : MaterialTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setTextColor(ContextCompat.getColor(context, R.color.lineup_name))
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(Typeface.createFromAsset(context.assets, "All Star Resort.ttf"))
    }
}