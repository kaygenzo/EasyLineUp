/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.databinding.NavDrawerHeaderBinding

class DrawerHeader(context: Context) : ConstraintLayout(context) {
    val binding = NavDrawerHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    fun setImage(image: String?, teamName: String) {
        binding.teamItem.setImage(image, teamName)
    }

    fun setTitle(title: String) {
        binding.teamItem.setTeamName(title)
    }

    fun setOnItemClickListener(listener: OnClickListener?) {
        binding.teamItem.setOnClickListener(listener)
    }

    fun setOnSwapTeamClickListener(listener: OnClickListener?) {
        binding.changeTeam.setOnClickListener(listener)
    }
}
