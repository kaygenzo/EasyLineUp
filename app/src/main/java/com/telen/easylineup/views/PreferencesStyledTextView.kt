package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.R
import java.lang.StringBuilder

enum class LineupTypeface(val valueType: Int) {
    NORMAL(0),
    HAND_WRITING(1);

    companion object {
        fun getByValue(value: String?): LineupTypeface {
            return when(value) {
                HAND_WRITING.valueType.toString() -> HAND_WRITING
                else -> NORMAL
            }
        }
    }
}

class PreferencesStyledTextView: MaterialTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        setTextColor(ContextCompat.getColor(context, R.color.lineup_name))
    }

    fun setTypeface(typeface: LineupTypeface) {
        when(typeface) {
            LineupTypeface.HAND_WRITING -> {
                super.setTypeface(Typeface.createFromAsset(context.assets, "HomemadeApple-Regular.ttf"))
            }
            else -> {
                // nothing for now
            }
        }
    }
}