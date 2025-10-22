package com.example.afinal

import android.content.Context
import android.content.Intent
import android.media.metrics.Event
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.VisibleRegion

private const val TAG = "FavAdapter"
class FavAdapter(private var items: MutableList<FavEventData>, private val onFavClick: (FavEventData, Any?) -> Unit) : RecyclerView.Adapter<FavAdapter.FavViewHolder>() {

    inner class FavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventName = itemView.findViewById<TextView>(R.id.eventName)
        private val eventVenue = itemView.findViewById<TextView>(R.id.eventVenue)
        private val eventDate=itemView.findViewById<TextView>(R.id.eventDate)
        private val favButton = itemView.findViewById<ImageButton>(R.id.favorite)
        private val image=itemView.findViewById<ImageView>(R.id.eventPic)
        private val eventAddress=itemView.findViewById<TextView>(R.id.eventAddy)
        private val price=itemView.findViewById<TextView>(R.id.priceRange)
        val maps=itemView.findViewById<ImageView>(R.id.mapButton)
        val purchase=itemView.findViewById<Button>(R.id.ticketButton)

        fun bind(event: FavEventData) {
            eventName.text = event.name
            eventVenue.text = event.venue
            eventAddress.text = event.address
            eventDate.text = event.dateTime

            purchase.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(items[adapterPosition].url)
                itemView.context.startActivity(intent)
            }

            favButton.background = null
            if (event.favorite) {
                favButton.setImageResource(android.R.drawable.star_on)
            } else {
                favButton.setImageResource(android.R.drawable.star_off)
            }

            if (event.imagesUrl.isNotEmpty()) {
                Glide.with(itemView)
                    .load(event.imagesUrl)
                    .into(image)
            } else {
                image.setImageResource(R.drawable.img)
            }

            if (event.pricing.isNotEmpty()) {
                price.visibility = VISIBLE
                price.text = event.pricing
            } else {
                price.visibility = GONE
            }

            favButton.setOnClickListener {
                val wasFav = event.favorite
                val nowFav = !wasFav
                event.favorite = nowFav


                favButton.setImageResource(
                    if (event.favorite) {
                        android.R.drawable.star_off
                    } else {
                        android.R.drawable.star_on
                    }
                )
                onFavClick(event, nowFav)

            }
            val name=event.name
            val lat = event.lat?.toDoubleOrNull()
            val lon = event.lon?.toDoubleOrNull()

            maps.setOnClickListener {
                Log.d(TAG, "bind: ${lat} ${lon}")
                if (lat==null || lon==null) {
                    return@setOnClickListener
                }
                val intent=Intent(itemView.context,MapsActivity::class.java).apply{
                    putExtra("lat",lat)
                    putExtra("lon", lon)
                    putExtra("name",name)
                }
                Log.d(TAG, "bind: ${lat} ${lon}")
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fav_row, parent, false)
        return FavViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val event=items[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = items.size

    fun replace(newList: List<FavEventData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    fun remove(id:String){
        val id=items.indexOfFirst { it.id == id}
        if(id >=0){
            items.removeAt(id)
            notifyItemRemoved(id)
        }
    }

}
