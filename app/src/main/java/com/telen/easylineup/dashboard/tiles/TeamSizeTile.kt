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
import com.telen.easylineup.dashboard.models.ITileData
import com.telen.easylineup.dashboard.models.KEY_DATA_SIZE
import com.telen.easylineup.dashboard.models.KEY_DATA_TEAM_IMAGE
import com.telen.easylineup.repository.model.Constants
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

        val image = map[KEY_DATA_TEAM_IMAGE] as String?
        image?.let {
            teamImage.post {
                Picasso.get().load(it)
                        .resize(teamImage.height, teamImage.height)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(Color.BLACK)
                                .borderWidthDp(2f)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build())
                        .placeholder(R.drawable.ic_unknown_team)
                        .error(R.drawable.ic_unknown_team)
                        .into(teamImage)
            }
        }

        team_size_warning_container.visibility = if (size < Constants.MIN_PLAYER_COUNT) View.VISIBLE else View.GONE

    }
}