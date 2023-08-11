package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.databinding.HeaderPlayerAttackBinding

class PlayerBattingHeader : ConstraintLayout {

    private val binding =
        HeaderPlayerAttackBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        binding.reorderImage.visibility = View.INVISIBLE
        binding.playerName.setText(R.string.header_batting_order_player_name)
        binding.fieldPosition.setText(R.string.header_batting_order_player_position)
        binding.shirtNumber.setText(R.string.header_batting_order_player_shirt)
    }

    fun setIsEditable(isEditable: Boolean) {
        binding.reorderImage.visibility = if (isEditable) View.INVISIBLE else View.GONE
    }
}