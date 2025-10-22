package com.example.afinal

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony.Mms.Intents
import android.util.Log
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.time.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "HeroActivity"
class HeroActivity : AppCompatActivity() {
    private lateinit var loginButton:Button
    private lateinit var crowdSound: MediaPlayer
    var signedOut:Boolean=false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hero)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        Log.d(TAG, "onCreate: doesnt work")
        loginButton=findViewById(R.id.button)

        loginButton.setOnClickListener {
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val guest=findViewById<Button>(R.id.guestButton)
        guest.setOnClickListener {
            val intent=Intent(this, MainActivity::class.java)
            intent.putExtra("guest",true)
            startActivity(intent)
        }
    }
    override fun onStart() {
        super.onStart()
        crowdSound = MediaPlayer.create(this, R.raw.crowd)
        if(intent.getBooleanExtra("STOP_SOUND",signedOut)==true){
            crowdSound.stop()
            crowdSound.release()
        }else {
            crowdSound.isLooping = false
            crowdSound.start()
        }
    }
}