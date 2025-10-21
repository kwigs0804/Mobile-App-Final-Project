package com.example.afinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavAdapter(
    private var items: MutableList<FavEventData>,
    private val onFavClick: (FavEventData, Any?) -> Unit
) : RecyclerView.Adapter<FavAdapter.FavViewHolder>() {

    inner class FavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventName = itemView.findViewById<TextView>(R.id.eventName)
        private val eventVenue = itemView.findViewById<TextView>(R.id.eventVenue)
        private val favButton = itemView.findViewById<ImageButton>(R.id.favorite)

        fun bind(event: FavEventData) {
            eventName.text = event.name
            eventVenue.text = event.venue

            favButton.background = null

            if (event.favorite) {
                favButton.setImageResource(android.R.drawable.star_on)
            } else {
                favButton.setImageResource(android.R.drawable.star_off)
            }

            favButton.setOnClickListener {
                val currentlyFav = event.favorite
                event.favorite = !currentlyFav

                favButton.setImageResource(
                    if (event.favorite) {
                        android.R.drawable.star_off
                    } else{
                        android.R.drawable.star_on
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fav_row, parent, false)
        return FavViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun replace(newList: List<FavEventData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
