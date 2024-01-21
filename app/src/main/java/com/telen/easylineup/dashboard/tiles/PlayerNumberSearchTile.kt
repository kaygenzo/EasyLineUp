/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.dashboard.tiles

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout

import com.telen.easylineup.R
import com.telen.easylineup.dashboard.TileClickListener
import com.telen.easylineup.databinding.TileLastPlayerNumberBinding
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.model.tiles.KEY_DATA_HISTORY
import com.telen.easylineup.domain.model.tiles.TileData
import timber.log.Timber

class PlayerNumberSearchTile(context: Context) : ConstraintLayout(context) {
    private val binding =
        TileLastPlayerNumberBinding.inflate(LayoutInflater.from(context), this, true)

    fun bind(data: TileData, inEditMode: Boolean, listener: TileClickListener) {
        binding.progressBar.visibility = View.GONE
        binding.tilePlayerNumberResult.visibility = View.GONE

        binding.mask.visibility = if (inEditMode) View.VISIBLE else View.INVISIBLE
        binding.tilePlayerNumberTextField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyNumberSearch(listener)
                true
            } else {
                false
            }
        }

        binding.tilePlayerNumberLayout.setEndIconOnClickListener {
            applyNumberSearch(listener)
        }

        (data.getData()[KEY_DATA_HISTORY] as? List<*>)
            ?.mapNotNull { it as? ShirtNumberEntry }
            ?.let { history ->
                binding.tilePlayerNumberResult.apply {
                    visibility = View.VISIBLE
                    history.firstOrNull()?.let {
                        text = it.playerName
                    } ?: setText(R.string.generic_not_found)
                }

                binding.tilePlayerNumberHistory.apply {
                    setOnClickListener {
                        listener.onTileSearchNumberHistoryClicked(history)
                    }

                    visibility = history.takeIf { it.isNotEmpty() }?.let {
                        View.VISIBLE
                    } ?: let {
                        View.GONE
                    }
                }
            } ?: let {
            binding.tilePlayerNumberHistory.visibility = View.GONE
        }
    }

    private fun applyNumberSearch(listener: TileClickListener) {
        try {
            val number = binding.tilePlayerNumberTextField.text.toString().toInt()
            listener.onTileSearchNumberClicked(number)
        } catch (e: NumberFormatException) {
            Timber.d("Not a numeric text, skip")
        }
    }
}
