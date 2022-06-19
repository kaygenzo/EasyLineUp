package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.TileMostUsedPlayerBinding
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_DATA_IMAGE
import com.telen.easylineup.domain.model.tiles.KEY_DATA_NAME
import com.telen.easylineup.domain.model.tiles.KEY_DATA_SHIRT_NUMBER
import com.telen.easylineup.utils.ready
import timber.log.Timber

class MostUsedPlayerTile(context: Context) : ConstraintLayout(context) {

    private val binding =
        TileMostUsedPlayerBinding.inflate(LayoutInflater.from(context), this, false)

    fun bind(data: ITileData, inEditMode: Boolean) {
        val map = data.getData()

        map.forEach { item ->
            when (item.key) {
                KEY_DATA_IMAGE -> {
                    binding.tilePlayerMostUsedImage.apply {
                        ready {
                            try {
                                Picasso.get()
                                    .load(item.value.toString())
                                    .resize(this.width, this.height)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_unknown_field_player)
                                    .error(R.drawable.ic_unknown_field_player)
                                    .into(this)
                            } catch (e: IllegalArgumentException) {
                                Timber.e(e)
                            }
                        }
                    }
                }
                KEY_DATA_NAME -> {
                    binding.tilePlayerMostUsedName.text = item.value.toString().trim()
                }
                KEY_DATA_SHIRT_NUMBER -> {
                    binding.tilePlayerMostUsedShirtNumber.text = item.value.toString()
                }
            }
        }

        if (!map.containsKey(KEY_DATA_IMAGE)) {
            Picasso.get()
                .load(R.drawable.ic_unknown_field_player)
                .into(binding.tilePlayerMostUsedImage)
        }

        binding.mask.visibility = if (inEditMode) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }
}