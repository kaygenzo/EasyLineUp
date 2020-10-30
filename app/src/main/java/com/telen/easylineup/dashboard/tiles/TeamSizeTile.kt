package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.TileClickListener
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_DATA_SIZE
import com.telen.easylineup.domain.model.tiles.KEY_DATA_TEAM_IMAGE
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.tile_team_size.view.*

class TeamSizeTile: ConstraintLayout {

    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.tile_team_size, this)
    }

    fun bind(data: ITileData, inEditMode: Boolean, listener: TileClickListener) {
        val map = data.getData()
        val size: Int = map[KEY_DATA_SIZE] as Int
        tile_team_size_text.text = size.toString()

        val image = map[KEY_DATA_TEAM_IMAGE] as String?
        image?.let {
            teamImage.ready {
                Picasso.get().load(it)
                        .placeholder(R.drawable.ic_unknown_team)
                        .error(R.drawable.ic_unknown_team)
                        .into(teamImage)
            }
        } ?: Picasso.get().load(R.drawable.ic_unknown_team).into(teamImage)

        team_size_warning_container.visibility = if (size < Constants.MIN_PLAYER_COUNT) View.VISIBLE else View.GONE

        tile_team_size_send_icon.setOnClickListener { listener.onTileTeamSizeSendButtonClicked() }

        mask.visibility = if (inEditMode) View.VISIBLE else View.INVISIBLE
    }
}