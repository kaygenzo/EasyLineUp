package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import java.lang.StringBuilder

class NormalUppercaseStyledTextView: AppCompatTextView {

    constructor(context: Context) : super(context) {initView(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

    private fun initView(context: Context) {
        isAllCaps = true
        setTextColor(ContextCompat.getColor(context, R.color.normal_uppercase_style_text_color))
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(Typeface.createFromAsset(context.assets, "Chivo-Black.ttf"))
    }
}