/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.databinding.ViewItemTeamCardBinding

class TeamCardItemView : ConstraintLayout {
    private val binding = ViewItemTeamCardBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setIcon(@DrawableRes icon: Int) {
        binding.itemTeamCardIcon.setImageResource(icon)
    }

    fun setDescription(description: String) {
        binding.itemTeamCardDescription.text = description
    }
}
