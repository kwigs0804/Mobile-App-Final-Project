package com.example.afinal

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val TAG = "EventsAdapter"
class EventsAdapter(private val events: ArrayList<Events>, private val context: Context): RecyclerView.Adapter<EventsAdapter.MyViewHolder>() {

    private var buttonImage: Int? = null
    private val favID: MutableSet<String> = mutableSetOf()
    var favClick: ((Events,Boolean)-> Unit)? = null
    fun setFavorites(ids: Set<String>){
        favID.clear()
        favID.addAll(ids)
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val eventName=itemView.findViewById<TextView>(R.id.eventName)
        val venue=itemView.findViewById<TextView>(R.id.eventVenue)
        val eventAddress= itemView.findViewById<TextView>(R.id.eventAddy)
        val date= itemView.findViewById<TextView>(R.id.eventDate)
        val image= itemView.findViewById<ImageView>(R.id.eventPic)
        val price= itemView.findViewById<TextView>(R.id.priceRange)
        val purchase=itemView.findViewById<Button>(R.id.ticketButton)
        val favButton=itemView.findViewById<ImageButton>(R.id.favorite)

        init{
            purchase.setOnClickListener{
                val intent=Intent(Intent.ACTION_VIEW)
                intent.data= Uri.parse(events[adapterPosition].url)
                context.startActivity(intent)
            }

            favButton.setOnClickListener {
                val position=adapterPosition
                    .takeIf{it != RecyclerView.NO_POSITION}
                val event=events[position!!]
                val favNow=favID.contains(event.id)

                if(favNow==true){
                    buttonImage=android.R.drawable.star_off
                }else{
                    buttonImage=android.R.drawable.star_on
                }
                favButton.setImageResource(buttonImage!!)
                favClick?.invoke(event,favNow)
            }
        }
        fun mapping(event:Events){
            val map=itemView.findViewById<ImageButton>(R.id.mapButton)
            val name=event._embedded.venues[0].name
            val location = event._embedded.venues[0].location
            val lat = location.lat?.toDouble()
            val lon = location.lon?.toDouble()

            map.setOnClickListener {
                if (lat==0.0 || lon==0.0) {
                    return@setOnClickListener
                }
                val intent=Intent(context,MapsActivity::class.java).apply{
                    putExtra("lat",lat)
                    putExtra("lon", lon)
                    putExtra("name",name)
                }
                context.startActivity(intent)
            }
        }
        fun bindFav(event: String){
            val favored=favID.contains(event)
            favButton.background=null
            if(favored==true){
                buttonImage=android.R.drawable.star_on
            }else{
                buttonImage=android.R.drawable.star_off
            }
            favButton.setImageResource(buttonImage!!)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): MyViewHolder{
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.event_row, parent,false)
        return MyViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem=events[position]
        val context=holder.itemView.context
        holder.eventName.text=currentItem.name
        holder.venue.text= currentItem._embedded.venues[0].name
        holder.eventAddress.text="${currentItem._embedded.venues[0].address.line1},${currentItem._embedded.venues[0].city.name}, ${currentItem._embedded.venues[0].state.name}"
        val dateForm=DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        val timeForm=DateTimeFormatter.ofPattern("hh:mm a")
        holder.mapping(currentItem)
        val bestPhoto=currentItem.images.maxByOrNull{
            it.width * it.height
        }

        if(bestPhoto != null){
            Glide.with(context)
                .load(bestPhoto.url)
                .into(holder.image)
        }

        if(currentItem.dates.start.localTime?.isNotEmpty() == true && currentItem.dates.start.localDate.isNotEmpty()){
            val eventDate=LocalDate.parse(currentItem.dates.start.localDate).format(dateForm)
            val eventTime=LocalTime.parse(currentItem.dates.start.localTime).format(timeForm)

            holder.date.text="Date: $eventDate @ $eventTime"
        }else if(currentItem.dates.start.localTime?.isEmpty() == true && currentItem.dates.start.localDate.isNotEmpty()){
            val eventDate=LocalDate.parse(currentItem.dates.start.localDate).format(dateForm)

            holder.date.text="Date: $eventDate @ TBD"
        }else{
            holder.date.text="TBD"
        }
        //A lot of events in Hartford do not have prices in JSON and prices from other cities were $0.00 in JSON
        val minCost= currentItem.priceRanges?.get(0)?.min
        val maxCost= currentItem.priceRanges?.get(0)?.max
        if(currentItem.priceRanges.isNullOrEmpty() || maxCost?.toInt()==0){
            holder.price.visibility= INVISIBLE
            Log.d(TAG, "onBindViewHolder: ${currentItem.priceRanges}")
        }else if(!currentItem.priceRanges.isNullOrEmpty() && minCost!=maxCost){
            holder.price.visibility=VISIBLE
            holder.price.text="Price Range: $${"%.2f".format(minCost)} - $${"%.2f".format(maxCost)}"
            Log.d(TAG, "onBindViewHolder: ${currentItem.priceRanges?.get(0)}")
        }else if(!currentItem.priceRanges.isNullOrEmpty() && minCost==maxCost){
            holder.price.visibility=VISIBLE
            holder.price.text="Starting Price: $${"%.2f".format(minCost)}"
            Log.d(TAG, "onBindViewHolder: ${currentItem.priceRanges?.get(0)}")
        }
        holder.bindFav(currentItem.id)
    }
    override fun getItemCount(): Int{
        return events.size
    }
}