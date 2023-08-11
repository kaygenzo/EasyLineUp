package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class SwashedStyledTextView : MaterialTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(Typeface.createFromAsset(context.assets, "KrinkesDecorPERSONAL.ttf"))
    }
}