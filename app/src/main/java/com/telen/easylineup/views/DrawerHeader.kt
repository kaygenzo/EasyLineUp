package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.NavDrawerHeaderBinding
import com.telen.easylineup.utils.ready
import timber.log.Timber

class DrawerHeader(context: Context) : ConstraintLayout(context) {

    val binding = NavDrawerHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    fun setImage(image: String?, teamName: String) {
       binding.teamItem.setImage(image, teamName)
    }

    fun setTitle(title: String) {
        binding.teamItem.setTeamName(title)
    }

    fun setOnItemClickListener(l: OnClickListener?) {
        binding.teamItem.setOnClickListener(l)
    }

    fun setOnSwapTeamClickListener(l: OnClickListener?) {
        binding.changeTeam.setOnClickListener(l)
    }
}