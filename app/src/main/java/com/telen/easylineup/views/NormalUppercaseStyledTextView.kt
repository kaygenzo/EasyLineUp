/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.R

class NormalUppercaseStyledTextView : MaterialTextView {
    constructor(context: Context) : super(context) {
        initView(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyle: Int) {
        isAllCaps = attrs?.let {
            val attributes = context.obtainStyledAttributes(
                attrs,
                R.styleable.NormalUppercaseStyledTextView,
                defStyle,
                0
            )
            val textAllCaps = attributes.getBoolean(
                R.styleable.NormalUppercaseStyledTextView_normalUppercaseStyledTextAllCaps,
                true
            )
            attributes.recycle()
            textAllCaps
        } ?: true
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(Typeface.createFromAsset(context.assets, "Chivo-Black.ttf"))
    }
}
