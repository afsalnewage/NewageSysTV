package com.dev.nastv.fcm

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.dev.nastv.ui.MainViewModel
import com.dev.nastv.uttils.AppConstant.ACTION_UPDATE_LIST
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class FcmService: FirebaseMessagingService() {




    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("message2",message.toString())
        message.data.let {

        }
        val intent = Intent(ACTION_UPDATE_LIST)

        intent.putExtra("EventsUpdated",true)

        applicationContext.sendBroadcast(intent)
    }
}