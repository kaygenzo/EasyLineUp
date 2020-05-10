package com.telen.easylineup.lineup.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.views.CollegedStyledTextView
import com.telen.easylineup.views.DefenseFixedView
import com.telen.easylineup.views.MLBStyledTextView

data class LineupsViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    val field: DefenseFixedView = view.findViewById(R.id.lineupField)
    val lineupName: MLBStyledTextView = view.findViewById(R.id.lineupName)
    val tournamentName: CollegedStyledTextView = view.findViewById(R.id.tournamentName)
    val rootView: View = view.findViewById(R.id.rootView)
}