/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.databinding.ViewTrashFieldButtonBinding

class TrashFieldButton : ConstraintLayout {
    private val binding: ViewTrashFieldButtonBinding =
        ViewTrashFieldButtonBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setScaleType(scaleType: ImageView.ScaleType) {
        binding.fab.scaleType = scaleType
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.fab.setOnClickListener(listener)
    }
}
