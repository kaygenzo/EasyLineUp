package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_timeline_middle.view.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

interface OnActionsClickListener {
    fun onDeleteClicked()
    fun onStatsClicked()
}

class TimeLineMiddleView: ConstraintLayout {

    private var listener: OnActionsClickListener? = null

    constructor(context: Context) : super(context) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initView(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initView(context) }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_timeline_middle, this)

        deleteTournament.setOnClickListener {
            listener?.onDeleteClicked()
        }

        statsTournament.setOnClickListener {
            listener?.onStatsClicked()
        }
    }

    fun setTournamentName(name: String) {
        tournamentName.text = name
    }

    fun setTournamentDate(start: Long, end: Long) {
        val builder = StringBuilder(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(start))
        if(start != end)
            builder.append(" - ").append(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(end))
        tournamentDate.text = builder.toString()
    }

    fun setOnActionsClickListener(listener: OnActionsClickListener) {
        this.listener = listener
    }
}