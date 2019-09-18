package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_timeline_middle.view.*
import java.text.SimpleDateFormat
import java.util.*

class TimeLineMiddleView: ConstraintLayout {

    constructor(context: Context) : super(context) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initView(context) }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_timeline_middle, this)
    }

    fun setTournamentName(name: String) {
        tournamentName.text = name
    }

    fun setTournamentDate(createdAt: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = createdAt
        tournamentDate.text = SimpleDateFormat("dd/MM/yyyy").format(calendar.timeInMillis)
    }
}