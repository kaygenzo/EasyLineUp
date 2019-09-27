package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_player_position_filter.view.*

class PlayerPositionFilterView: ConstraintLayout {

    constructor(context: Context) : super(context) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initView(context) }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_player_position_filter, this)
    }

    fun setText(text: String) {
        playerPositionFilterView.text = text
    }

    fun setTextColor(@ColorRes color: Int) {
        playerPositionFilterView.setTextColor(ContextCompat.getColor(context, color))
    }

    fun setBackground(@DrawableRes background: Int) {
        playerPositionFilterRoot.setBackgroundResource(background)
    }
}