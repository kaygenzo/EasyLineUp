/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.telen.easylineup.databinding.ViewPlayerPositionFilterBinding

class PlayerPositionFilterView : ConstraintLayout {
    private val binding =
        ViewPlayerPositionFilterBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setText(text: String) {
        binding.playerPositionFilterView.text = text
    }

    fun setTextColor(@ColorRes color: Int) {
        binding.playerPositionFilterView.setTextColor(ContextCompat.getColor(context, color))
    }

    fun setBackground(@DrawableRes background: Int) {
        binding.playerPositionFilterRoot.setBackgroundResource(background)
    }
}
