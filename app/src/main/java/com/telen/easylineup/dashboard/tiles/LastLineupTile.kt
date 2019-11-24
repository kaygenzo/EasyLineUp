package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.models.KEY_LINEUP_NAME
import com.telen.easylineup.dashboard.models.KEY_LINEUP_PLAYERS
import com.telen.easylineup.repository.data.PlayerWithPosition
import kotlinx.android.synthetic.main.tile_last_lineup.view.*

class LastLineupTile: ConstraintLayout{

    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.tile_last_lineup, this)
    }

    fun bind(data: ITileData) {
        val map = data.getData()
        val name = map[KEY_LINEUP_NAME] as String
        val players = map[KEY_LINEUP_PLAYERS] as? List<PlayerWithPosition>

        lineup_name.text = name
        players?.let {
            fieldAndPlayersRootView.setListPlayerInField(it)
//                setListPlayer(it)
        }
    }
}