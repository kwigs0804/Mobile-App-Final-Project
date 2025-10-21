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
        shared = getSharedPreferences("settings", MODE_PRIVATE)
        repo = FirestoreRepo()

        val recycle = findViewById<RecyclerView>(R.id.favRecycle)
        recycle.layoutManager = LinearLayoutManager(this)
        adapter = FavAdapter(mutableListOf()) { event, favNow ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (favNow == true) {
                if (uid != null) {
                    repo.unFav(uid, event.id).addOnSuccessListener {
                        Toast.makeText(this, "Removed: ${event.name}", Toast.LENGTH_SHORT).show()
                        refresh()
                    }
                }
            } else {
                if (uid != null) {
                    repo.addFav(uid, event).addOnSuccessListener {
                        Toast.makeText(this, "Added: ${event.name}", Toast.LENGTH_SHORT).show()
                        refresh()
                    }
                }
            }
            recycle.adapter=adapter
        }
    }

    fun refresh() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val homeCity = shared.getString("homeCity", "")
        val sort = shared.getString("sortBy", "name")

        if (uid != null) {
            repo.loadFav(uid) { all ->
                var list = all
                if (homeCity?.isNotBlank() == true) {
                    list = list.filter { it.city.equals(homeCity, true) }
                    list = when (sort) {
                        "date" -> list.sortedBy { it.date }
                        else -> list.sortedBy { it.name }
                    }
                }
                adapter.replace(list.toList())
            }
        }
    }
}