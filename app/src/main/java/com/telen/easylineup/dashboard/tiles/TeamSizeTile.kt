/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.TileClickListener
import com.telen.easylineup.databinding.TileTeamSizeBinding
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.tiles.KEY_DATA_SIZE
import com.telen.easylineup.domain.model.tiles.KEY_DATA_TEAM_IMAGE
import com.telen.easylineup.domain.model.tiles.KEY_DATA_TEAM_TYPE
import com.telen.easylineup.domain.model.tiles.TileData
import com.telen.easylineup.utils.ready

class TeamSizeTile(context: Context) : ConstraintLayout(context) {
    private val binding = TileTeamSizeBinding.inflate(LayoutInflater.from(context), this, true)

    fun bind(data: TileData, inEditMode: Boolean, listener: TileClickListener) {
        val map = data.getData()
        val size = map[KEY_DATA_SIZE] as Int
        val teamType = TeamType.getTypeById(map[KEY_DATA_TEAM_TYPE] as Int)
        binding.tileTeamSizeText.text = size.toString()

        (map[KEY_DATA_TEAM_IMAGE] as String?).let {
            binding.teamImage.ready {
                Picasso.get().load(it)
                    .placeholder(R.drawable.ic_unknown_team)
                    .error(R.drawable.ic_unknown_team)
                    .into(binding.teamImage)
            }
        }

        binding.teamSizeWarningContainer.visibility =
                if (size < teamType.defaultStrategy.batterSize) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        binding.tileTeamSizeSendIcon.setOnClickListener {
            listener.onTileTeamSizeSendButtonClicked()
        }

        binding.mask.visibility = if (inEditMode) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }
}
