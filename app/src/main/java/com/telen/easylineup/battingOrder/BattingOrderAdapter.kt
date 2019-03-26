package com.telen.easylineup.battingOrder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player

interface OnItemTouchedListener {
    fun onMoved(fromPosition: Int, toPosition: Int)
    fun onSwiped(position: Int)
}

class BattingOrderAdapter(private val players: List<Player>): RecyclerView.Adapter<BattingOrderAdapter.BatterViewHolder>(), OnItemTouchedListener {
    override fun onSwiped(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    class BatterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val playerName = view.findViewById<TextView>(R.id.playerName)
        val shortNumber = view.findViewById<TextView>(R.id.shortNumber)
        val fieldPosition = view.findViewById<TextView>(R.id.fieldPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatterViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(R.layout.batter_item, parent, false)
        return BatterViewHolder(viewItem)
    }

    override fun getItemCount(): Int {
       return players.size
    }

    override fun onBindViewHolder(holder: BatterViewHolder, position: Int) {
        val player = players[position]
        with(holder) {
            playerName.text = player.name
            shortNumber.text = player.shirtNumber.toString()
            fieldPosition.text = "-1"
        }
    }


}