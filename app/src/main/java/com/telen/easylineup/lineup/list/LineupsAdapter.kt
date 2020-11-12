package com.telen.easylineup.lineup.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup

class LineupsAdapter(private val lineups: List<Lineup>, private val itemClickedListener: OnItemClickedListener?): RecyclerView.Adapter<LineupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineupsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_adapter_lineup, parent, false)
        return LineupsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lineups.size
    }

    override fun onBindViewHolder(holder: LineupsViewHolder, position: Int) {
        val lineup = lineups[position]

        holder.field.apply {
            //TODO to get from a strategy when it will be developed
            initField(FieldPosition.values().filter { FieldPosition.isDefensePlayer(it.position) || it == FieldPosition.DP_DH })
            setSmallPlayerPosition(lineup.playerPositions, lineup.mode)
        }
        holder.lineupName.text = lineup.name
        holder.tournamentName.visibility = View.GONE
        holder.rootView.setOnClickListener {
            itemClickedListener?.onLineupClicked(lineup)
        }
    }
}