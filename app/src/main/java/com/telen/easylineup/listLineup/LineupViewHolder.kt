package com.telen.easylineup.listLineup

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.views.CollegedStyledTextView
import com.telen.easylineup.views.DefenseFixedView
import com.telen.easylineup.views.MLBStyledTextView

class LineupViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val field = view.findViewById<DefenseFixedView>(R.id.lineupField)
    val lineupName = view.findViewById<MLBStyledTextView>(R.id.lineupName)
    val tournamentName = view.findViewById<CollegedStyledTextView>(R.id.tournamentName)
}