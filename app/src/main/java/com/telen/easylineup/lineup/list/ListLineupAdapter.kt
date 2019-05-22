package com.telen.easylineup.lineup.list

import android.graphics.PointF
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.PositionWithLineup

class ListLineupAdapter(val lineups: List<PositionWithLineup>): RecyclerView.Adapter<LineupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.categorized_lineup_item, parent, false)
        return LineupViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lineups.size
    }

    override fun onBindViewHolder(holder: LineupViewHolder, position: Int) {
        val lineup = lineups[position]
        holder.field.setSmallPlayer(PointF(lineup.x, lineup.y))
        holder.lineupName.text = lineup.lineupName
        holder.tournamentName.text = lineup.tournamentName
    }

}