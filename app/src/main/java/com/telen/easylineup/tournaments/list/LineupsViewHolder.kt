package com.telen.easylineup.tournaments.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.R
import com.telen.easylineup.views.MLBStyledTextView

data class LineupsViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    val lineupName: MLBStyledTextView = view.findViewById(R.id.lineupName)
    val lineupDate: MaterialTextView = view.findViewById(R.id.lineupDate)
    val lineupStrategy: MaterialTextView = view.findViewById(R.id.lineupStrategy)
    val rootView: View = view.findViewById(R.id.rootView)
}