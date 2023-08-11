package com.telen.easylineup.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.telen.easylineup.databinding.ItemAdapterTournamentsBinding
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.tournaments.list.LineupsAdapter
import com.telen.easylineup.tournaments.list.OnItemClickedListener
import java.text.SimpleDateFormat
import java.util.*

interface OnActionsClickListener {
    fun onDeleteClicked()
    fun onStatsClicked()
}

@SuppressLint("ViewConstructor")
class TournamentItemView(context: Context, itemClickedListener: OnItemClickedListener?) :
    ConstraintLayout(context) {

    lateinit var lineupsAdapter: LineupsAdapter
    private val lineups = mutableListOf<Lineup>()
    private var listener: OnActionsClickListener? = null

    private val binding =
        ItemAdapterTournamentsBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        lineupsAdapter = LineupsAdapter(lineups, itemClickedListener)

        val layoutMgr = GridLayoutManager(context, 1)
        val dividerItemDecoration = DividerItemDecoration(context, layoutMgr.orientation)

        binding.lineupsOfTournamentRecycler.apply {
            layoutManager = layoutMgr
            addItemDecoration(dividerItemDecoration)
            adapter = lineupsAdapter
        }

        binding.deleteTournament.setOnClickListener {
            listener?.onDeleteClicked()
        }

        binding.statsTournament.setOnClickListener {
            listener?.onStatsClicked()
        }
    }

    fun setLineups(lineups: List<Lineup>) {
        this.lineups.clear()
        this.lineups.addAll(lineups)
        this.lineupsAdapter.notifyDataSetChanged()
    }

    fun setTournamentName(tournamentName: String) {
        binding.tournamentName.text = tournamentName
    }

    fun setTournamentDate(start: Long, end: Long) {
        val builder = StringBuilder(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(start))
        if (start != end)
            builder.append(" - ").append(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(end))
        binding.tournamentDate.text = builder.toString()
    }

    fun setOnActionsClickListener(listener: OnActionsClickListener) {
        this.listener = listener
    }

    fun setTeamType(teamType: Int) {
        this.lineupsAdapter.apply {
            this.teamType = TeamType.getTypeById(teamType)
            notifyDataSetChanged()
        }
    }
}