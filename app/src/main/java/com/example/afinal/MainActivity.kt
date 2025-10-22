package com.example.afinal

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val TAG="EventFragment"
private const val API_KEY="StGgQ7Xcyt48ukakiENXFpT4eqXVRhmE"
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val BASE_URL="https://app.ticketmaster.com/discovery/v2/"
    private lateinit var keyword: String
    private lateinit var eventList: ArrayList<Events>
    private lateinit var eventAdapter: EventsAdapter
    private lateinit var city: EditText
    private lateinit var recyclerView: RecyclerView
    private val repo=FirestoreRepo()
    private val auth=FirebaseAuth.getInstance()
    private val favID= mutableSetOf<String>()
    private var itemSelected: Boolean=false
    private var signedOut: Boolean=false
    private lateinit var shared: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val spinner=findViewById<Spinner>(R.id.catChoice)
        recyclerView=findViewById<RecyclerView>(R.id.recyclerView)
        eventList=ArrayList<Events>()
        eventAdapter=EventsAdapter(eventList,this)
        recyclerView.adapter=eventAdapter
        keyword="Select Category"
        recyclerView.layoutManager= LinearLayoutManager(this)
        setUp()
        city = findViewById<EditText>(R.id.cityText)
        val choiceList= listOf("Select Category","Music", "Sports", "Theater", "Family", "Arts & Theater", "Concerts", "Comedy", "Dance")
        val choiceAdapter= ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,choiceList)
        spinner.adapter=choiceAdapter
        spinner.onItemSelectedListener=this
        val search=findViewById<Button>(R.id.searchButton)
        val favList=findViewById<ImageButton>(R.id.favList)
        val guest=intent.getBooleanExtra("guest", false)
        val settings=findViewById<ImageButton>(R.id.settingsButton)
        val favButton=findViewById<ImageButton>(R.id.favorite)

        if(FirebaseAuth.getInstance().currentUser==null){
            signedOut=true
        }
        if(guest==true || signedOut==true){
            favList.isEnabled=false
            favList.alpha=0.38f
            settings.isEnabled=false
            settings.alpha=0.38f
        }

        favList.setOnClickListener{
            val intent= Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
        settings.setOnClickListener {
            val intent= Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        search.setOnClickListener {
            if(!checkChoices()){
                return@setOnClickListener
            }
            eventList.clear()
            eventAdapter.notifyDataSetChanged()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val ticketmasterAPI = retrofit.create(TicketMaster::class.java)
            ticketmasterAPI.getEvents(API_KEY, keyword,city.text.toString(), "date,asc")
                .enqueue(object : Callback<EventData> {
                    override fun onResponse(call: Call<EventData>, response: Response<EventData>) {
                        val body = response.body()
                        val info = body?._embedded?.events
                        val options =findViewById<RecyclerView>(R.id.recyclerView)
                        val boring = findViewById<TextView>(R.id.resultNotice)

                        if (body == null || !response.isSuccessful) {
                            options.visibility= INVISIBLE
                            boring.visibility= VISIBLE
                        }

                        if(info.isNullOrEmpty()){
                            options.visibility= INVISIBLE
                            boring.visibility= VISIBLE
                            eventList.clear()
                            eventAdapter.notifyDataSetChanged()
                        }else{
                            boring.visibility= INVISIBLE
                            eventList.clear()
                            eventList.addAll(info)
                            eventAdapter.notifyDataSetChanged()
                            options.visibility= VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<EventData>, p1: Throwable) {
                        Log.d(TAG, "onFailure: $p1")
                    }
                }
                )
        }


    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        keyword="Select Category"
        itemSelected=false
    }
    override fun onItemSelected(adapter: AdapterView<*>?, view: View?, int: Int, id: Long) {
        if(int>0) {
            keyword = adapter?.getItemAtPosition(int).toString()
            itemSelected=true
        }else{
            onNothingSelected(adapter)
        }
        eventList.clear()
        eventAdapter.notifyDataSetChanged()
    }
    fun checkChoices():Boolean{
        val noCity=city.text.isNullOrEmpty() || city.text.toString()=="City"
        val noCat=keyword=="Select Category" || !itemSelected

        val alert= this?.let { AlertDialog.Builder(it) }
        alert?.setTitle("Missing Input")
        if(noCity && noCat){
            alert?.setMessage("Please Choose Category and Enter City!")?.create()?.show()
            return false
        }else if(noCat && !noCity){
            alert?.setMessage("Please Choose Category")?.create()?.show()
            return false
        }else if(noCity && !noCat){
            alert?.setMessage("Please Enter City!")?.create()?.show()
            return false
        }
        return true
    }

    override fun onResume(){
        super.onResume()
        val uid=auth.currentUser?.uid
        if (uid != null) {
            repo.loadFav(uid){favs->
                favID.clear()
                favID.addAll(favs.map{it.id})
                eventAdapter.setFavorites(favID)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setUp(){
        eventAdapter=EventsAdapter(eventList,this)
        recyclerView.adapter=eventAdapter

        auth.currentUser?.uid?.let{uid ->
            repo.loadFav(uid){favs->
                favID.clear()
                favID.addAll(favs.map { it.id })
                eventAdapter.setFavorites(favID)
            }
        }
        eventAdapter.favClick={event,favNow->
            val uid=auth.currentUser?.uid

            if(favNow==true){
                if (uid != null) {
                    repo.unFav(uid, event.id)
                        .addOnSuccessListener {
                            favID.remove(event.id)
                            eventAdapter.setFavorites(favID)
                        }
                }
            }else{
                val price=event.priceRanges?.firstOrNull()
                val min=price?.min
                val max=price?.max
                var pricing=""
                val dateForm= DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                val timeForm= DateTimeFormatter.ofPattern("hh:mm a")
                val start=event.dates.start
                var dateTime: String? =null
                val lat= event._embedded.venues[0].location.lat
                val lon= event._embedded.venues[0].location.lon

                if(max==0.0 || (min==null && max==null)){
                    pricing=""
                }else if(max==min){
                    pricing="Starting Price: $${"%.2f".format(min)}"
                }else{
                    pricing="Price Range: $${"%.2f".format(min)} - $${"%.2f".format(max)}"
                }

                if(start.localTime?.isNotEmpty() == true && start.localDate.isNotEmpty()){
                    val eventDate=LocalDate.parse(start.localDate).format(dateForm)
                    val eventTime=LocalTime.parse(start.localTime).format(timeForm)

                    dateTime="Date: $eventDate @ $eventTime"
                }else if(start.localTime?.isEmpty() == true && start.localDate.isNotEmpty()){
                    val eventDate=LocalDate.parse(start.localDate).format(dateForm)

                    dateTime="Date: $eventDate @ TBD"
                }else{
                    dateTime="TBD"
                }

                val fav= FavEventData(
                    event.id,
                    event.name,
                    start.localDate,
                    "${event._embedded.venues[0].name}",
                    "${event._embedded.venues[0].address.line1}, ${event._embedded.venues[0].city.name}, ${event._embedded.venues[0].state.name}",
                    event._embedded.venues[0].city.name,
                    event.images.maxByOrNull{it.width*it.height}?.url.toString(),
                    pricing,
                    dateTime,
                    true,
                    lat,
                    lon
                )
                if (uid != null) {
                    repo.addFav(uid, fav)
                        .addOnSuccessListener{
                            favID.add(event.id)
                            eventAdapter.setFavorites(favID)
                        }
                }
            }
        }
    }
}