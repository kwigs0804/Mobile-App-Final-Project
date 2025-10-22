package com.example.afinal

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    val signOut=findViewById<Button>(R.id.signout)
    private lateinit var shared: SharedPreferences
    private lateinit var date: RadioButton
    private lateinit var name: RadioButton
    private lateinit var save: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, HeroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        shared=getSharedPreferences("settings", MODE_PRIVATE)
        date=findViewById(R.id.dateRadio)
        name=findViewById(R.id.nameRadio)
        save=findViewById(R.id.saveSet)

        val current=shared.getString("sort","date")
        if(current=="name"){
            name.isChecked==true

        }else{
            date.isChecked==true
        }
        save.setOnClickListener {
            val choice=if(name.isChecked){
                "name"
            }else{
                "date"
            }
            shared.edit().putString("sort",choice).apply()
            finish()
        }
    }
}