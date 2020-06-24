package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_NAME
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_PLAYERS
import kotlinx.android.synthetic.main.tile_last_lineup.view.*

class LastLineupTile: ConstraintLayout{

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.tile_last_lineup, this)
    }

    fun bind(data: ITileData, inEditMode: Boolean) {
        val map = data.getData()
        val name = map[KEY_LINEUP_NAME] as String
        val players = map[KEY_LINEUP_PLAYERS] as? List<PlayerWithPosition>

        lineup_name.text = name
        players?.let {
            fieldAndPlayersRootView.setListPlayerInField(it)
        }

        mask.visibility = if (inEditMode) View.VISIBLE else View.INVISIBLE
    }
}