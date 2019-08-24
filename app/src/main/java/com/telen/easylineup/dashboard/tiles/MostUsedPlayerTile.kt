package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.models.*
import kotlinx.android.synthetic.main.tile_most_used_player.view.*

class MostUsedPlayerTile: ConstraintLayout {

    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.tile_most_used_player, this)
    }

    fun bind(data: ITileData) {
        val map = data.getData()
        val iterator = map.entries.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            when(item.key) {
                KEY_DATA_IMAGE -> {
                    tile_player_most_used_image.post {
                        Picasso.get()
                                .load(item.value.toString())
                                .resize(tile_player_most_used_image.width, tile_player_most_used_image.height)
                                .centerCrop()
                                .placeholder(R.drawable.ic_unknown_field_player)
                                .error(R.drawable.ic_unknown_field_player)
                                .into(tile_player_most_used_image)
                    }
                }
                KEY_DATA_NAME -> {
                    tile_player_most_used_name.text = item.value.toString()
                }
                KEY_DATA_SHIRT_NUMBER -> {
                    tile_player_most_used_shirt_number.text = item.value.toString()
                }
                KEY_DATA_MATCH_PLAYED -> {
                    tile_player_most_used_match_played_value.text = item.value.toString()
                }
            }
        }
    }
}