package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.tiles.ITileData
import kotlinx.android.synthetic.main.tile_last_player_number.view.*

class PlayerNumberSearchTile: ConstraintLayout {

    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.tile_last_player_number, this)
    }

    fun bind(data: ITileData, inEditMode: Boolean) {
        mask.visibility = if (inEditMode) View.VISIBLE else View.INVISIBLE
    }
}