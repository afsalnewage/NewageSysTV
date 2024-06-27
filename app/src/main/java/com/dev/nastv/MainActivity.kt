     package com.dev.nastv

//import com.google.firebase.messaging.FirebaseMessaging

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.dev.nastv.connection.SocketManager
import com.dev.nastv.databinding.ActivityMainBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.network.Resource
import com.dev.nastv.ui.MainViewModel
import com.dev.nastv.ui.MediaItemAdapter
import com.dev.nastv.ui.UiPagesAdapter
import com.dev.nastv.uttils.AppUittils.applyFadeInAnimation
import com.dev.nastv.uttils.AppUittils.applyFadeOutAnimation
import com.dev.nastv.uttils.ConnectivityObserver
import com.dev.nastv.uttils.SessionUtils
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.launch
import javax.inject.Inject


     @AndroidEntryPoint
     class MainActivity : AppCompatActivity() {

   // private lateinit var firebase: FirebaseMessaging
    private lateinit var binding: ActivityMainBinding
         private lateinit var socket: Socket
         @Inject
         lateinit var connectivityObserver: ConnectivityObserver

    private lateinit var mediaItemAdapter: MediaItemAdapter
    private lateinit var pagerAdapter: UiPagesAdapter
        // val mediaList = ArrayList<MediaItemData>()
         val mediaList = ArrayList<TvMedia>()
         private lateinit var viewModel: MainViewModel

         private val handler = Handler(Looper.getMainLooper())
         private val autoScrollRunnable = object : Runnable {
             override fun run() {
                 val currentItem = binding.mainPager.currentItem
                 //val nextItem =(currentItem + 1) % pagerAdapter.itemCount
              //  binding.mainPager.setCurrentItem(nextItem, 1000L)

                 if (currentItem < (binding.mainPager.adapter?.itemCount ?: 0) - 1) {

                     binding.mainPager.setCurrentItem(currentItem+1, 1000L)
                 }else{
                     binding.mainPager.setCurrentItem(0, 1000L)
                 }

             }


         }


           /*
         private val receiver = object : BroadcastReceiver() {
             override fun onReceive(context: Context?, intent: Intent?) {
                 if (intent?.action == ACTION_UPDATE_LIST) {
                  //  val update = intent.getBooleanExtra("EventType", false)
                     Log.d("message2","broadcast received")
                     Toast.makeText(applicationContext, "push received", Toast.LENGTH_SHORT).show()

                     viewModel.getMediaItems()
                 }
             }
         }

            */



         override fun onStart() {
             super.onStart()
//             val filter = IntentFilter(ACTION_UPDATE_LIST)
//             this.registerReceiver(receiver, filter)
         }

         override fun onStop() {
             super.onStop()
          //   this.unregisterReceiver(receiver)
         }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding=ActivityMainBinding.inflate(layoutInflater)
        viewModel= ViewModelProvider(this)[MainViewModel::class.java]
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

           Log.d("TOKEN_refresh",SessionUtils.refreshToken.toString())
           Log.d("TOKEN_auth",SessionUtils.authToken.toString())


        lifecycleScope.launch {
            connectivityObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {

                        viewModel.getMediaItems()
                    }
                    ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                        showToast("No internet connection")
                    }
                    else -> { /* No-op for other statuses */ }
                }
            }
        }
              /*


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

               */


      // binding.mainPager.setPageTransformer(DepthPageTransformer())

        viewModel.mediaDataState.asLiveData().observe(this) {
            when (it) {
                is Resource.Error -> {
                    showToast(it.errorMessage.toString())



                }
                is Resource.Loading -> {
                   // binding.btnLogin.startLoading()
                    Log.d("TTT","loading")
                    binding.animation.visibility=View.VISIBLE
                    binding.animation.playAnimation()
                }
                is Resource.Success -> {
                    Log.d("TTT","success")
                    applyFadeOutAnimation(binding.mainPager)
                    //binding.btnLogin?.loadingSuccessful()
                    if (it.data!=null){
                      //  Log.d("TTT", "event type ${getType( it.data.data!!.tv_apps[0].event_type)}")
                        //showToast(it.data.message)
                        binding.animation.pauseAnimation()
                        binding.animation.visibility=View.GONE

                        mediaList.addAll(it.data.data!!.tv_apps)
                        pagerAdapter=UiPagesAdapter(this,it.data.data!!.tv_apps)
                        updatePager()
                    }else{
                        showToast(it.data?.message?:"data is null")
                    }
                }
            }
        }



        // Initialize Socket
        socket = SocketManager.getSocket()

        socket.on(Socket.EVENT_CONNECT) {
            Log.d("Socket", "Connected")
            runOnUiThread {
                //showToast("socket Connected")

            }
        }
        socket.on("Refresh") { args ->
//            if (args.isNotEmpty()) {
//                val data = args[0] as JSONObject
//                val eventsUpdated = data.getBoolean("EventsUpdated")
//
//                if (eventsUpdated) {
//                    runOnUiThread {
//                        showToast("socket called")
//                        viewModel.getMediaItems()
//                    }
//                }
//            }

            runOnUiThread {
                //showToast("socket called")
//                binding.progressBar.visibility=View.VISIBLE
                binding.animation.visibility=View.VISIBLE
                binding.animation.playAnimation()
                viewModel.getMediaItems()
            }
        }
        SocketManager.connect()




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
                   //  showToast("KEYCODE_DPAD_UP_LEFT")
                 }
                 KeyEvent.KEYCODE_DPAD_UP_RIGHT->{
                    // showToast("KEYCODE_DPAD_UP_RIGHT")

                 }
                 KeyEvent.KEYCODE_DPAD_UP->{
//                     val intent = Intent(ACTION_UPDATE_LIST)
//                     intent.putExtra("EventsUpdated",true)
//                     applicationContext.sendBroadcast(intent)
                     //showToast("KEYCODE_DPAD_UP")

                 }
                 KeyEvent.KEYCODE_DPAD_DOWN->{
                    // showToast("KEYCODE_DPAD_DOWN")
                 }

             }

             return super.onKeyUp(keyCode, event)
         }

         fun scrollToNextPage() {
             handler.post(autoScrollRunnable)
         }

         override fun onDestroy() {
             super.onDestroy()
             SocketManager.disconnect()
             handler.removeCallbacks(autoScrollRunnable)


         }

         fun ViewPager2.setCurrentItem(
             item: Int,
             duration: Long,
             interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
             pagePxWidth: Int = width // Default value taken from getWidth() from ViewPager2 view
         ) {
             val pxToDrag: Int = pagePxWidth * (item - currentItem)
             val animator = ValueAnimator.ofInt(0, pxToDrag)
             var previousValue = 0
             animator.addUpdateListener { valueAnimator ->
                 val currentValue = valueAnimator.animatedValue as Int
                 val currentPxToDrag = (currentValue - previousValue).toFloat()
                 fakeDragBy(-currentPxToDrag)
                 previousValue = currentValue
             }
             animator.addListener(object : Animator.AnimatorListener {

                 override fun onAnimationStart(animation: Animator) {
                     beginFakeDrag()
                 }

                 override fun onAnimationEnd(animation: Animator) {
                     endFakeDrag()
                 }

                 override fun onAnimationCancel(animation: Animator) {}

                 override fun onAnimationRepeat(animation: Animator) {}
             })
             animator.interpolator = interpolator
             animator.duration = duration
             animator.start()
         }


     }