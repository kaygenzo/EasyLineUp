/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.edition

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.RosterItem
import com.telen.easylineup.views.RosterEntryView
import timber.log.Timber

class RosterAdapter(
    private val items: List<RosterItem>,
    private val listener: RosterAdapterCallback
) : RecyclerView.Adapter<RosterAdapter.RosterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RosterViewHolder {
        return RosterViewHolder(RosterEntryView(parent.context))
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: RosterViewHolder, position: Int) {
        val item = items[position]
        holder.view.setPlayerName(item.player.name)
        holder.view.setTextListener(null)
        item.playerNumberOverlay?.let {
            holder.view.setNumber(it.number)
        } ?: let {
            holder.view.setNumber(item.player.shirtNumber)
        }
        holder.view.setTextListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                try {
                    listener.onNumberChanged(item.player, text.toString().toInt())
                } catch (e: NumberFormatException) {
                    Timber.e(e.message)
                    listener.onNumberChanged(item.player, 0)
                }
            }

            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        holder.view.setSelectedStateListener(null)
        holder.view.setSelectedState(item.selected)
        holder.view.setSelectedStateListener { _, isChecked ->
            listener.onPlayerSelectedChanged(item.player, isChecked)
        }
    }

    /**
     * @property view
     */
    data class RosterViewHolder(val view: RosterEntryView) : RecyclerView.ViewHolder(view)
}
