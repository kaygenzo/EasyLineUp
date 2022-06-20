package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.databinding.TileLastLineupBinding
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_LINEUP_NAME

class LastLineupTile(context: Context) : ConstraintLayout(context) {

    private val binding = TileLastLineupBinding.inflate(LayoutInflater.from(context), this, true)

    fun bind(data: ITileData, inEditMode: Boolean) {
        val map = data.getData()
        val name = map[KEY_LINEUP_NAME] as String

        binding.lineupName.text = name
        binding.mask.visibility = if (inEditMode) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }
}