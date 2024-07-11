package com.dev.nastv.di

import android.app.Application
import com.dev.nastv.connection.SocketManager
import com.dev.nastv.uttils.SessionUtils
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication:Application() {

    //  private val token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXNzaW9uSWQiOiI2Njc5M2FkMzVkZjc1NmFkZThmZGY5ZDgiLCJ1c2VySWQiOjEsImlhdCI6MTcxOTIyMDk0NywiZXhwIjoxNzE5MzA3MzQ3fQ.7Io3PaoYKk8kZMdru2Sbc6oDsH3nmTSl_xH1UFgWaYM"
   //   private val token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXNzaW9uSWQiOiI2Njc5NDI4MTVkZjc1NmFkZThmZGZhMDgiLCJ1c2VySWQiOjM0MywiaWF0IjoxNzE5MjIyOTEzLCJleHAiOjE3MTkzMDkzMTN9.y1WR0YiFESAgLtohA-Vn_TuCeV1Gk6RHmrqNjEe2Aro"
    override fun onCreate() {
        super.onCreate()
        //FirebaseApp.initializeApp(this);
        SessionUtils.init(this)
        SocketManager.initialize(SessionUtils.authToken!!)
    }
}