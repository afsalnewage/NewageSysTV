     package com.dev.nastv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.viewpager2.widget.ViewPager2
import com.dev.nastv.databinding.ActivityMainBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.network.Resource
import com.dev.nastv.ui.MainViewModel
import com.dev.nastv.ui.MediaItemAdapter
import com.dev.nastv.ui.UiPagesAdapter
import com.dev.nastv.uttils.AppConstant.ACTION_UPDATE_LIST
import com.dev.nastv.uttils.AppConstant.TOPIC
import com.dev.nastv.uttils.AppUittils.applyFadeInAnimation
import com.dev.nastv.uttils.AppUittils.applyFadeOutAnimation
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint


     @AndroidEntryPoint
     class MainActivity : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayer

    private lateinit var firebase: FirebaseMessaging
    private lateinit var binding: ActivityMainBinding
//         private val viewModel: MainViewModel by viewModels { MyViewModel.Factory }
//             private val viewModel by  viewModels<LoginViewModel>()
    private lateinit var mediaItemAdapter: MediaItemAdapter
    private lateinit var pagerAdapter: UiPagesAdapter
        // val mediaList = ArrayList<MediaItemData>()
         val mediaList = ArrayList<TvMedia>()
         private lateinit var viewModel: MainViewModel

         private val handler = Handler(Looper.getMainLooper())
         private val autoScrollRunnable = object : Runnable {
             override fun run() {
                 val currentItem = binding.mainPager.currentItem
                 val nextItem = (currentItem + 1) % pagerAdapter.itemCount
                 binding.mainPager.currentItem = nextItem
             }
         }


         private val receiver = object : BroadcastReceiver() {
             override fun onReceive(context: Context?, intent: Intent?) {
                 if (intent?.action == ACTION_UPDATE_LIST) {
                  //  val update = intent.getBooleanExtra("EventType", false)
                     Log.d("message2","broadcast received")
                     Toast.makeText(applicationContext, "push received", Toast.LENGTH_SHORT).show()

                     viewModel.getMedeaItems()
                 }
             }
         }

         override fun onStart() {
             super.onStart()
             val filter = IntentFilter(ACTION_UPDATE_LIST)
             this.registerReceiver(receiver, filter)
         }

         override fun onStop() {
             super.onStop()
             this.unregisterReceiver(receiver)
         }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding=ActivityMainBinding.inflate(layoutInflater)
        viewModel= ViewModelProvider(this)[MainViewModel::class.java]
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        firebase= FirebaseMessaging.getInstance()




        firebase.subscribeToTopic(TOPIC)
            .addOnCompleteListener { task: Task<Void?> ->
                var msg = "Subscribed to topic"
                if (!task.isSuccessful) {
                    msg = "Subscription failed"
                }
                Log.d("FCM", msg!!)
                showToast(msg)
            }.addOnFailureListener{
                showToast(it.message.toString())
            }.addOnCanceledListener{
                showToast("Task cancelled")
            }


      // binding.mainPager.setPageTransformer(DepthPageTransformer())

        viewModel.mediaDataState.asLiveData().observe(this) {
            when (it) {
                is Resource.Error -> {
                    showToast(it.errorMessage.toString())

                    Log.d("TTT","error")
                }
                is Resource.Loading -> {
                   // binding.btnLogin.startLoading()
                    Log.d("TTT","loading")
                }
                is Resource.Success -> {
                    Log.d("TTT","success")
                    applyFadeOutAnimation(binding.mainPager)
                    //binding.btnLogin?.loadingSuccessful()
                    if (it.data!=null){
                      //  Log.d("TTT", "event type ${getType( it.data.data!!.tv_apps[0].event_type)}")
                        //showToast(it.data.message)
                        mediaList.addAll(it.data.data!!.tv_apps)
                        pagerAdapter=UiPagesAdapter(this,it.data.data!!.tv_apps)
                        updatePager()
                    }else{
                        showToast(it.data?.message?:"data is null")
                    }
                }
            }
        }


    }

         private fun updatePager(){
             applyFadeInAnimation(binding.mainPager)
             binding.mainPager.adapter=pagerAdapter
             binding.mainPager.offscreenPageLimit=1
             //  binding.mainPager.setPageTransformer(DepthPageTransformer())
             binding.mainPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                 override fun onPageSelected(position: Int) {
                     super.onPageSelected(position)
                     handler.removeCallbacks(autoScrollRunnable)
                     val mediaItem = mediaList[position]
                     if (mediaItem.event_type != "Video") {
                         handler.postDelayed(autoScrollRunnable, 10000) // 30 seconds
                     }
                 }
             })
         }
         private fun showToast(msg:String){
             Toast.makeText(binding.root.context,msg, Toast.LENGTH_SHORT).show()

         }

         override fun onResume() {
             super.onResume()
             binding.mainPager.requestFocus()
         }

         override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
             val currentItem = binding.mainPager.currentItem

             when(keyCode){
                 KeyEvent.KEYCODE_DPAD_LEFT->{
                    // showToast("KEYCODE_DPAD_LEFT")

                     if (currentItem > 0) {
                         binding.mainPager.currentItem = currentItem - 1;
                         return true;
                     }else if (currentItem==0){
                         val count=(binding.mainPager.adapter?.itemCount ?: 0) - 1
                         binding.mainPager.currentItem=count
                     }
                 }
                 KeyEvent.KEYCODE_DPAD_RIGHT->{
                     //showToast("KEYCODE_DPAD_RIGHT")
                     if (currentItem < (binding.mainPager.adapter?.itemCount ?: 0) - 1) {
                         binding.mainPager.currentItem = currentItem + 1;
                         return true;
                     }else{
                         binding.mainPager.currentItem = 0
                     }

                 }
                 KeyEvent.KEYCODE_DPAD_UP_LEFT->{
                     showToast("KEYCODE_DPAD_UP_LEFT")
                 }
                 KeyEvent.KEYCODE_DPAD_UP_RIGHT->{
                     showToast("KEYCODE_DPAD_UP_RIGHT")

                 }
                 KeyEvent.KEYCODE_DPAD_UP->{
//                     val intent = Intent(ACTION_UPDATE_LIST)
//
//                     intent.putExtra("EventsUpdated",true)
//
//                     applicationContext.sendBroadcast(intent)
                     showToast("KEYCODE_DPAD_UP")

                 }
                 KeyEvent.KEYCODE_DPAD_DOWN->{
                     showToast("KEYCODE_DPAD_DOWN")
                 }

             }

             return super.onKeyUp(keyCode, event)
         }

         fun scrollToNextPage() {
             handler.post(autoScrollRunnable)
         }

         override fun onDestroy() {
             super.onDestroy()
             handler.removeCallbacks(autoScrollRunnable)
         }
//https://images.pexels.com/photos/460775/pexels-photo-460775.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1
//https://images.pexels.com/photos/709552/pexels-photo-709552.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1
         /*
         private fun createList():ArrayList<MediaItemData> {
             mediaList.add(
                 MediaItemData(
                    sourceUrl =  "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8cGVyc29ufGVufDB8fDB8fHww",

                     mediaType= Type.NewJoinee,0
//                     name = "Emma wilson",
//                     position = "Business Analyst",
//                     date = "06/05/2024",
//                     educationalQualification = "Masters of BusinessAdministration(Finance)",
//                     hobbies ="Reading and watching movies",
//                     professionalBackground = "Fresher"

                 )
             )
             mediaList.add(
                 MediaItemData(
                     sourceUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                     mediaType=  Type.Video,0
                    // programTitle = "NewAgeSyS  annual day celebration 2023 December"

                 )
             )
             mediaList.add(
                 MediaItemData(
                     sourceUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mzh8fHBlcnNvbnxlbnwwfHwwfHx8MA%3D%3D",
                     mediaType=  Type.Anniversary,0
//                     yearOfAnniversary = 3,
//                     position = "Senior QA",



                 )
             )


             mediaList.add(
                 MediaItemData(
                     "https://images.unsplash.com/photo-1531361171768-37170e369163?q=80&w=1935&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                     Type.Image,
                     0
                 )
             )


             mediaList.add(
                 MediaItemData(
                     "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                     Type.Video,
                     6000
                 )
             )
             mediaList.add(
                 MediaItemData(
                     "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
                     Type.Video,
                     6000
                 )
             )
             mediaList.add(
                 MediaItemData(
                     "https://images.pexels.com/photos/1172064/pexels-photo-1172064.jpeg?auto=compress&cs=tinysrgb&w=600",
                     Type.Image,
                     0
                 )
             )
             mediaList.add(
                 MediaItemData(
                     "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                     Type.Video,
                     6000
                 )
             )

             return  mediaList

         }

          */

     }