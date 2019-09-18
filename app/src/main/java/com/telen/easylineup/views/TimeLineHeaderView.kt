package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_timeline_header.view.*
import pl.hypeapp.materialtimelineview.MaterialTimelineView

class TimeLineHeaderView: ConstraintLayout {

    constructor(context: Context) : super(context) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initView(context) }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_timeline_header, this)
    }

    fun setTimeLinePosition(position: Int) {
        if(position>=0) {
            material_timeline_view.position = position
            material_timeline_view.radioMarginStart = resources.getDimension(R.dimen.timeline_radio_margin_left)
        }
        else {
            material_timeline_view.radioMarginStart = resources.getDimension(R.dimen.timeline_radio_margin_left_reverse)
        }
    }
}