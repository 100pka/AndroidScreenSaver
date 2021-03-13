package com.example.standbyvideosplasher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class IdleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val startActivityIntent = Intent(context, MainActivity::class.java)
                startActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(startActivityIntent)
            }
            Intent.ACTION_SCREEN_OFF -> {
                sendServiceCommand(context, "playByNotification")
            }
            Intent.ACTION_POWER_CONNECTED -> {
                sendServiceCommand(context, "play")
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                sendServiceCommand(context, "wait")
            }
        }
    }

    private fun sendServiceCommand(context: Context?, command: String) {
        val serviceIntent = Intent(context, IdleListenerForegroundService::class.java)
        serviceIntent.putExtra(FOREGROUND_SERVICE, command)
        context?.startService(serviceIntent)
    }
}