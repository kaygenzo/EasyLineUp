package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.dashboard.TileClickListener
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.KEY_DATA_HISTORY
import kotlinx.android.synthetic.main.tile_last_player_number.view.*

class PlayerNumberSearchTile: ConstraintLayout {

    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){init(context)}

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.tile_last_player_number, this)
    }

    fun bind(data: ITileData, inEditMode: Boolean, listener: TileClickListener) {

        progressBar.visibility = View.GONE
        tilePlayerNumberResult.visibility = View.GONE

        mask.visibility = if (inEditMode) View.VISIBLE else View.INVISIBLE
        tilePlayerNumberTextField.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyNumberSearch(listener)
                true
            }
            else
                false
        }

        tilePlayerNumberLayout.setEndIconOnClickListener {
            applyNumberSearch(listener)
        }

        (data.getData()[KEY_DATA_HISTORY] as? List<ShirtNumberEntry>)?.let { history ->

            tilePlayerNumberResult.visibility = View.VISIBLE
            history.firstOrNull()?.let {
                tilePlayerNumberResult.text = it.playerName
            } ?: let {
                tilePlayerNumberResult.setText(R.string.generic_not_found)
            }

            tilePlayerNumberHistory.setOnClickListener {
                listener.onTileSearchNumberHistoryClicked(history)
            }

            history.takeIf { it.isNotEmpty() }?.let {
                tilePlayerNumberHistory.visibility = View.VISIBLE
            } ?: let {
                tilePlayerNumberHistory.visibility = View.GONE
            }
        } ?: let {
            tilePlayerNumberHistory.visibility = View.GONE
        }
    }

    private fun applyNumberSearch(listener: TileClickListener) {
        try {
            val number = tilePlayerNumberTextField.text.toString().toInt()
            listener.onTileSearchNumberClicked(number)
        }
        catch (e: NumberFormatException) {
        }
    }
}