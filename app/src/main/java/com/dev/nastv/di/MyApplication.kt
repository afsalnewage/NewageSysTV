package com.dev.nastv.di

import android.app.Application
import com.dev.nastv.connection.SocketManager
import com.dev.nastv.uttils.AppConstant.DEFAULT_AUTH
import com.dev.nastv.uttils.SessionUtils
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication:Application() {


    override fun onCreate() {
        super.onCreate()
        //FirebaseApp.initializeApp(this);
        SessionUtils.init(this)
        SocketManager.initialize(SessionUtils.authToken!!)

    }
}