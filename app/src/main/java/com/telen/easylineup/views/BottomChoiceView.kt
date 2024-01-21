/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.databinding.ViewBottomChoiceBinding

class BottomChoiceView : ConstraintLayout {
    private val binding: ViewBottomChoiceBinding =
        ViewBottomChoiceBinding.inflate(LayoutInflater.from(context), this, true)
    var saveClickListener: OnClickListener? = null
    var cancelClickListener: OnClickListener? = null

    init {
        binding.save.setOnClickListener { saveClickListener?.onClick(it) }
        binding.cancel.setOnClickListener { cancelClickListener?.onClick(it) }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setSaveButtonEnabled(enabled: Boolean) {
        binding.save.isEnabled = enabled
    }

    fun setCancelButtonEnabled(enabled: Boolean) {
        binding.cancel.isEnabled = enabled
    }
}
