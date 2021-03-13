package com.example.standbyvideosplasher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.VideoView
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {

    lateinit var videoView: VideoView

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (savedInstanceState == null) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val lowChannel = NotificationChannel(
                LOW_CHANNEL_ID,
                LOW_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
            val highChannel = NotificationChannel(
                HIGH_CHANNEL_ID,
                HIGH_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannels(listOf(lowChannel, highChannel))
            startIdleListenerService()
        }

        registerReceiver(finishReceiver, IntentFilter("finish"))

        videoView = findViewById(R.id.video_view)
        val uriPath = "android.resource://" + packageName + "/raw/" + R.raw.gendalf
        val uri = Uri.parse(uriPath)
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.setOnPreparedListener { mp -> mp.isLooping = true }

        videoView.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startIdleListenerService() {
        val intent = Intent(this, IdleListenerForegroundService::class.java)
        intent.putExtra(FOREGROUND_SERVICE, "start")
        startForegroundService(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
    }

    override fun onDestroy() {
        unregisterReceiver(finishReceiver)
        super.onDestroy()
    }
}