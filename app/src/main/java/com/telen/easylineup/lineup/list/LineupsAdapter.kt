package com.telen.easylineup.lineup.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Lineup
import java.text.SimpleDateFormat
import java.util.*

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
        holder.lineupName.text = lineup.name
        val date = lineup.eventTimeInMillis.takeIf { it > 0L } ?: lineup.createdTimeInMillis
        val builder = StringBuilder(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(date))
        holder.lineupDate.text = builder.toString()
        holder.rootView.setOnClickListener {
            itemClickedListener?.onLineupClicked(lineup)
        }
    }
}