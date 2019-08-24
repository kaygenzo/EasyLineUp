package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.models.KEY_DATA_SIZE
import kotlinx.android.synthetic.main.tile_team_size.view.*

class TeamSizeTile: ConstraintLayout {

    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.tile_team_size, this)
    }

    fun bind(data: ITileData) {
        val map = data.getData()
        val size: Int = map[KEY_DATA_SIZE] as Int
        tile_team_size_text.text = resources.getString(R.string.tile_team_size_message, size)
    }
}