package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.R

class NormalUppercaseStyledTextView: MaterialTextView {

    constructor(context: Context) : super(context) {initView(context, null, 0)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initView(context, attrs, 0)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context, attrs, defStyleAttr)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {initView(context, attrs, defStyleAttr)}

    private fun initView(context: Context, attrs: AttributeSet?, defStyle: Int) {
        isAllCaps = attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NormalUppercaseStyledTextView, defStyle, 0)
            val textAllCaps = a.getBoolean(R.styleable.NormalUppercaseStyledTextView_normalUppercaseStyledTextAllCaps, true)
            a.recycle()
            textAllCaps
        } ?: true
        val defaultColor = ContextCompat.getColor(context, R.color.normal_uppercase_style_text_color)
        val textColor = attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NormalUppercaseStyledTextView, defStyle, 0)
            val color = a.getColor(R.styleable.NormalUppercaseStyledTextView_normalUppercaseStyledTextColor, defaultColor)
            a.recycle()
            color
        } ?: defaultColor

        setTextColor(textColor)
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(Typeface.createFromAsset(context.assets, "Chivo-Black.ttf"))
    }
}