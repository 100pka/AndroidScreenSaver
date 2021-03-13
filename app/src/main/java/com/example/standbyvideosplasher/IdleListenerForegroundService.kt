package com.example.standbyvideosplasher

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

const val LOW_CHANNEL_ID = "low"
const val HIGH_CHANNEL_ID = "high"
const val FOREGROUND_SERVICE = "foreground service"
const val NOTIFICATION_ID = 10

class IdleListenerForegroundService : Service() {

    private val myBinder = MyBinder()
    private val receiver = IdleReceiver()
    lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder {
        return myBinder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ServiceCast")
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        registerReceiver(receiver, intentFilter)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.cancel(0)
        when (intent?.getStringExtra(FOREGROUND_SERVICE) ?: "") {
            "start" -> {
                val notificationIntent = Intent(this, MainActivity::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

                val cancelButtonIntent = Intent(this, IdleListenerForegroundService::class.java)
                cancelButtonIntent.putExtra(FOREGROUND_SERVICE, "stop")
                val cancelButtonPendingIntent =
                    PendingIntent.getService(this, 0, cancelButtonIntent, 0)

                val notification = NotificationCompat.Builder(this, LOW_CHANNEL_ID)
                    .setContentText("Splash video service running...")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.ic_delete, "Cancel", cancelButtonPendingIntent)
                    .setAutoCancel(true)

                startForeground(NOTIFICATION_ID, notification.build())
            }

            "playByNotification" -> {
                sendBroadcast(Intent("finish"))
                val startActivityIntent = Intent(this, MainActivity::class.java)
                startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val pendingIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0)
                val notificationBuilder = NotificationCompat.Builder(this, HIGH_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(pendingIntent, true)
                    .setAutoCancel(true)
                if (isChargingNow(this)) {
                    notificationManager.notify(0, notificationBuilder.build())
                }
            }

            "play" -> {
                val startActivityIntent = Intent(this, MainActivity::class.java)
                startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(startActivityIntent)
            }

            "wait" -> {
                sendBroadcast(Intent("finish"))
            }

            "stop" -> {
                sendBroadcast(Intent("finish"))
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun isChargingNow(context: Context?): Boolean {
        val batteryStatus: Intent? =
            IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                context?.registerReceiver(null, intentFilter)
            }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return (status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    inner class MyBinder : Binder() {
        fun getService(): IdleListenerForegroundService {
            return this@IdleListenerForegroundService
        }
    }
}