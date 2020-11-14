package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.list.LineupsAdapter
import com.telen.easylineup.lineup.list.OnItemClickedListener
import kotlinx.android.synthetic.main.item_adapter_tournaments.view.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

interface OnActionsClickListener {
    fun onDeleteClicked()
    fun onStatsClicked()
}

class TournamentItemView : ConstraintLayout {

    lateinit var lineupsAdapter: LineupsAdapter
    private val lineups = mutableListOf<Lineup>()
    private var listener: OnActionsClickListener? = null

    constructor(context: Context, itemClickedListener: OnItemClickedListener?) : super(context) {initView(context, itemClickedListener)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initView(context, null)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context, null)}

    private fun initView(context: Context, itemClickedListener: OnItemClickedListener?) {
        LayoutInflater.from(context).inflate(R.layout.item_adapter_tournaments, this)
        lineupsAdapter = LineupsAdapter(lineups, itemClickedListener)

        val layoutMgr = GridLayoutManager(context, 1)
        val dividerItemDecoration = DividerItemDecoration(context, layoutMgr.orientation)

        lineupsOfTournamentRecycler.apply {
            layoutManager = layoutMgr
            addItemDecoration(dividerItemDecoration)
            adapter = lineupsAdapter
        }

        deleteTournament.setOnClickListener {
            listener?.onDeleteClicked()
        }

        statsTournament.setOnClickListener {
            listener?.onStatsClicked()
        }
    }

    fun setLineups(lineups: List<Lineup>) {
        this.lineups.clear()
        this.lineups.addAll(lineups)
        this.lineupsAdapter.notifyDataSetChanged()
    }

    fun setTournamentName(tournamentName: String) {
        this.tournamentName.text = tournamentName
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