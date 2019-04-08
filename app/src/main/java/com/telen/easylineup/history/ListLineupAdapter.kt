package com.telen.easylineup.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.DatabaseMockProvider
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.views.CardDefenseFixed
import com.telen.easylineup.views.DefenseFixedView

class ListLineupAdapter(val lineupList: List<Lineup>): RecyclerView.Adapter<ListLineupAdapter.LineupCardHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineupCardHolder {
        val itemAdapter = CardDefenseFixed(parent.context).apply {
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        }
        return LineupCardHolder(itemAdapter)
    }

    override fun getItemCount(): Int {
       return lineupList.size
    }

    override fun onBindViewHolder(holder: LineupCardHolder, position: Int) {
        val lineup = lineupList[position]
        holder.title.text = lineup.name
        holder.cardDefense.setListPlayer(lineup.playerFieldPosition)
    }

    class LineupCardHolder(view: CardDefenseFixed): RecyclerView.ViewHolder(view) {
        val cardDefense = view
        val title = view.findViewById<TextView>(R.id.lineup_name)
    }
}