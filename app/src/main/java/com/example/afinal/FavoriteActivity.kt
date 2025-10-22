package com.example.afinal

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class FavoriteActivity : AppCompatActivity() {
    private lateinit var repo: FirestoreRepo
    private lateinit var adapter: FavAdapter
    private lateinit var shared: SharedPreferences
    private val FILE_NAME = "settings"
    private var sort:String?="date"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorite)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        shared=getSharedPreferences("settings", MODE_PRIVATE)
        repo = FirestoreRepo()

        val recycle = findViewById<RecyclerView>(R.id.favRecycle)
        recycle.layoutManager = LinearLayoutManager(this)
        adapter = FavAdapter(mutableListOf()) { event, favNow ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (favNow == false) {
                    uid?.let{
                        repo.unFav(it, event.id).addOnSuccessListener {
                            Toast.makeText(this,"Removed: ${event.name}",Toast.LENGTH_SHORT).show()
                            adapter.remove(event.id)
                            refresh()
                        }
                    }
            }
        }
        recycle.adapter=adapter
        refresh()
    }

    fun refresh() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        sort= shared.getString("sort","date")

        if (uid != null) {
            repo.loadFav(uid) { all ->
                var list = all
                list = when (sort) {
                    "name" -> list.sortedBy { it.name }
                    else -> list.sortedBy { it.date }

                }
                adapter.replace(list)
            }
        }
    }
}