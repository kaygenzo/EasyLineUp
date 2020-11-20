package com.telen.easylineup.lineup.list

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.R
import com.telen.easylineup.views.CollegedStyledTextView
import com.telen.easylineup.views.DefenseFixedView
import com.telen.easylineup.views.MLBStyledTextView

data class LineupsViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    val lineupName: MLBStyledTextView = view.findViewById(R.id.lineupName)
    val lineupDate: MaterialTextView = view.findViewById(R.id.lineupDate)
    val lineupStrategy: MaterialTextView = view.findViewById(R.id.lineupStrategy)
    val rootView: View = view.findViewById(R.id.rootView)
}