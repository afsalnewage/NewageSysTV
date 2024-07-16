package com.dev.nastv

//import com.google.firebase.messaging.FirebaseMessaging

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dev.nastv.connection.SocketManager
import com.dev.nastv.databinding.ActivityMainBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.network.Resource
import com.dev.nastv.ui.MainViewModel
import com.dev.nastv.ui.MediaItemsAdapter
import com.dev.nastv.ui.UiPagesAdapter
import com.dev.nastv.uttils.AppUittils
import com.dev.nastv.uttils.AppUittils.applyFadeInAnimation
import com.dev.nastv.uttils.AppUittils.applyFadeOutAnimation
import com.dev.nastv.uttils.AppUittils.getFilesInDirectory
import com.dev.nastv.uttils.ConnectivityObserver
import com.dev.nastv.uttils.SessionUtils
import com.dev.nastv.worker.DownloadWorker
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_FILE_NAME
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

   private lateinit var   sharedPreferences: SharedPreferences
   //= getSharedPreferences("work_ids", Context.MODE_PRIVATE)
    private lateinit var workManager: WorkManager

    private lateinit var exoPlayer: ExoPlayer
    // private lateinit var firebase: FirebaseMessaging
    private lateinit var binding: ActivityMainBinding
    private lateinit var socket: Socket
    private var pageScrolling = false

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver


    private lateinit var pagerAdapter: UiPagesAdapter
    private lateinit var rvAdapter: MediaItemsAdapter
    // val mediaList = ArrayList<MediaItemData>()
    val mediaList = ArrayList<TvMedia>()
    private lateinit var viewModel: MainViewModel
   private val filteredList =ArrayList<TvMedia>()
    private val handler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val currentItem = binding.mainPager.currentItem
            //val nextItem =(currentItem + 1) % pagerAdapter.itemCount
            //  binding.mainPager.setCurrentItem(nextItem, 1000L)

            if (currentItem < (binding.mainPager.adapter?.itemCount ?: 0) - 1) {

                binding.mainPager.setCurrentItem(currentItem + 1)//, 1000L)
            } else {
                binding.mainPager.setCurrentItem(0)//, 1000L)

            }

        }


    }


    fun scheduleDownloads(dataList: List<TvMedia>) {
        val workManager = WorkManager.getInstance(this)
        var workContinuation: WorkContinuation? = null
        val validFileNames = mutableListOf<String>()
        val workRequests = mutableListOf<OneTimeWorkRequest>()
        val downloadingItems= mutableListOf<String>()
        val finalList =ArrayList<TvMedia>()
        for (data in dataList) {
            if (data.event_type == "Video") {
                val fileName = "${data._id}.${AppUittils.getFileTypeFromUrl(data.file_url)}"
                val file = AppUittils.getFileIfExists(this, fileName)
                validFileNames.add(fileName)
                downloadingItems.add(data._id)
                if (file != null) {
                    Log.d("DownloadScheduler", "File already exists: $fileName")
                   // viewModel.updateFinalList(data)
                    finalList.add(data)
                    continue
                } else {
                    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setInputData(
                            workDataOf(
                                "url" to data.file_url,
                                KEY_FILE_NAME to data._id
                            )
                        ).build()
                    workRequests.add(downloadWorkRequest)

                    viewModel.addItemToDownloading(data._id)

                    if (workContinuation == null) {
                        workContinuation = workManager.beginWith(downloadWorkRequest)
                    } else {
                        workContinuation = workContinuation.then(downloadWorkRequest)
                    }
                }
            }else{
              //viewModel.updateFinalList(data)
                finalList.add(data)
            }

            if (filteredList.isNotEmpty()){
              viewModel.updateFinalList(filteredList)
            }else{
                viewModel.updateFinalList(finalList)
            }

        }

//        if (workRequests.isNotEmpty()) {
//            workManager.enqueue(workRequests)
//            val workIds = workRequests.map { it.id }
//            viewModel.updateWorkIds(workIds)
//        }

        if (workRequests.isNotEmpty()) {
            workManager.enqueue(workRequests)
            val workIds = workRequests.map { it.id }
            viewModel.updateWorkIds(workIds)

            // Save workIds to SharedPreferences
            val workIdStrings = workIds.map { it.toString() }.toSet()
            val sharedPreferences = getSharedPreferences("work_ids", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putStringSet("work_ids_set", workIdStrings)
                putStringSet("valid_file_names", downloadingItems.toSet())
                apply()
            }
        }

        workContinuation?.enqueue()
        CoroutineScope(Dispatchers.IO).launch {
            deleteUnmatchedFiles(this@MainActivity, validFileNames)
        }
    }

    private suspend fun deleteUnmatchedFiles(context: Context, validFileNames: List<String>) {
        withContext(Dispatchers.IO) {
            val filesInDirectory = getFilesInDirectory(context)

            for (file in filesInDirectory) {
                if (file.name !in validFileNames) {
                    file.delete()
                    Log.d("FileDeletion", "Deleted file: ${file.absolutePath}")
                }
            }
        }
    }


    private fun checkWorkStatusOnAppStart() {
     ///   val sharedPreferences = getSharedPreferences("work_ids", Context.MODE_PRIVATE)
        val workIdStrings = sharedPreferences.getStringSet("work_ids_set", emptySet()) ?: emptySet()
        val downloadingFiles = sharedPreferences.getStringSet("valid_file_names", emptySet()) ?: emptySet()

        if (workIdStrings.isNotEmpty()) {
            val workIds = workIdStrings.map { UUID.fromString(it) }
            viewModel.updateWorkIds(workIds) // Update ViewModel with workIds
            observeWorkStatus(workIds)

            if (downloadingFiles.isNotEmpty()){
                val list=mediaList.filterNot {data ->
                    downloadingFiles.contains(data._id)
                }
                filteredList.addAll(list)
            }
        } else {
            // No existing workIds, proceed with API call and scheduling downloads if needed
            // scheduleDownloads(dataList) // Call this method with the appropriate data list

        }
    }

    private fun observeWorkStatus(workIds: List<UUID>) {
        workIds.forEach { workId ->
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(workId).observe(this) { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val fileName = workInfo.outputData.getString(KEY_FILE_NAME)
                            if (fileName != null) {
                                viewModel.removeItemFromDownloading(fileName)
                            }
                            viewModel.incrementCompletedRequests()
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            val fileName = workInfo.outputData.getString(KEY_FILE_NAME)
                            if (fileName != null) {
                                viewModel.removeItemFromDownloading(fileName)
                            }
                            viewModel.incrementCompletedRequests()
                        }
                        else -> {}
                    }

                    val totalRequests = workIds.size
                    if (viewModel.completedRequests.value == totalRequests || totalRequests == 0) {
                        onAllDownloadsComplete()

                        // Remove workIds from SharedPreferences after completion
                      //  val sharedPreferences = getSharedPreferences("work_ids", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            remove("work_ids_set")
                            remove("valid_file_names")
                            apply()
                        }
                    }
                }
            }
        }
    }



    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        //   this.unregisterReceiver(receiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        workManager = WorkManager.getInstance(applicationContext)
        sharedPreferences=getSharedPreferences("work_ids", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        exoPlayer= ExoPlayer.Builder(this).build()

        Log.d("TOKEN_refresh", SessionUtils.refreshToken.toString())
        Log.d("TOKEN_auth", SessionUtils.authToken.toString())


        lifecycleScope.launch {
            connectivityObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {

                        viewModel.getMediaItems()
                    }

                    ConnectivityObserver.Status.Lost, ConnectivityObserver.Status.Unavailable -> {
                        showToast("No internet connection")
                    }

                    else -> { /* No-op for other statuses */
                    }
                }
            }
        }

        checkWorkStatusOnAppStart()

        viewModel.finalMediaLit.observe(this){
//            mediaList.clear()
//            mediaList.addAll(it)
            Log.e("TTTTT","List update")
            //pagerAdapter = UiPagesAdapter(this,it)
            //updatePager()
        }







        viewModel.mediaDataState.asLiveData().observe(this) {
            when (it) {
                is Resource.Error -> {
                    showToast(it.errorMessage.toString())
                    Log.d("TTT", "${it.errorMessage.toString()}")


                }

                is Resource.Loading -> {
                    // binding.btnLogin.startLoading()

                    binding.animation.visibility = View.VISIBLE
                    binding.animation.playAnimation()
                }

                is Resource.Success -> {
                    Log.d("TTT", "${it.data.toString()}")
                    applyFadeOutAnimation(binding.mainPager)

                    if (it.data != null) {

                        binding.animation.pauseAnimation()
                        binding.animation.visibility = View.GONE

                        //val downloadingItems = viewModel.downloadingItems.value
//                        val filteredList = it.data.data!!.tv_apps.filterNot { tvMedia ->
//                            downloadingItems.contains(tvMedia._id)
//                        }

                        mediaList.addAll(it.data.data!!.tv_apps)
                        //pagerAdapter = UiPagesAdapter(this,it.data.data!!.tv_apps)
                        rvAdapter= MediaItemsAdapter(exoPlayer,this,it.data.data!!.tv_apps){mediaItem, url,pos ->

                            Log.d("RRRT","tittle $url           $pos")
                        }
                        updatePager()

//                        if (mediaList.isNotEmpty()){
//                            mediaList.clear()
//                        }
//                        mediaList.addAll(it.data.data!!.tv_apps)
//                        scheduleDownloads(it.data.data!!.tv_apps)


                    } else {
                        showToast(it.data?.message ?: "data is null")
                    }
                }

                else -> {}
            }
        }


//        viewModel.downloadingItems.observe(this) { downloadingItems ->
//            val filteredList = mediaList.filterNot { tvMedia ->
//                downloadingItems.contains(tvMedia._id)
//            }
//          //  pagerAdapter = UiPagesAdapter
//          //  pagerAdapter.updateData(filteredList as ArrayList<TvMedia>)
//        }



        // Initialize Socket
        socket = SocketManager.getSocket()

        socket.on(Socket.EVENT_CONNECT) {
            Log.d("Socket", "Connected")

        }
        socket.on("Refresh") { args ->


            runOnUiThread {

                binding.animation.visibility = View.VISIBLE
                binding.animation.playAnimation()
                viewModel.getMediaItems()
            }
        }
        SocketManager.connect()


    }

    private fun onAllDownloadsComplete() {
        val downloadingItems = viewModel.downloadingItems.value ?: emptySet()
        val downloadingFiles = sharedPreferences.getStringSet("valid_file_names", emptySet()) ?: emptySet()

        val filteredList = mediaList.filterNot { tvMedia ->
        //    downloadingItems.contains(tvMedia._id)
                     downloadingFiles.contains(tvMedia._id)
        }
        viewModel.updateFinalList(ArrayList(filteredList))
    }
    private fun updatePager() {
        applyFadeInAnimation(binding.mainPager)
        binding.mainPager.adapter =rvAdapter         // pagerAdapter       rvAdapter
        binding.mainPager.offscreenPageLimit = 1
        //  binding.mainPager.setPageTransformer(DepthPageTransformer())
        binding.mainPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(autoScrollRunnable)
                val mediaItem = mediaList[position]
                rvAdapter.updateCurrentPlayingPosition(position)
                Log.d("RRR2","tittle ${mediaItem.file_url}           $position")
                if (mediaItem.event_type != "Video") {
                    exoPlayer.pause()
                  //  exoPlayer.clearMediaItems()
                    handler.postDelayed(autoScrollRunnable, 10000) // 30 seconds
                }else{
                    /*
                     if ( exoPlayer.isPlaying) exoPlayer.pause()
                    exoPlayer.clearMediaItems()
                    val fileName = "${mediaItem!!._id}.${AppUittils.getFileTypeFromUrl(mediaItem!!.file_url)}"
                    val file = AppUittils.getFileIfExists(this@MainActivity, fileName)

                    val mediaItem = if (file != null) {
                        val uri = Uri.fromFile(file)
                        MediaItem.fromUri(uri)
                    } else {
                        MediaItem.fromUri(mediaItem!!.file_url)
                    }

                 //   rvAdapter.player=exoPlayer

                    //   exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true

                     */
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        pageScrolling = false
                        exoPlayer.play()
                    }

                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        pageScrolling = true
                        exoPlayer.pause()
                    }

                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        pageScrolling = true
                    }

                }
            }
        })
    }

    private fun setUi(){

    }

    private fun showToast(msg: String) {
        Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()

    }

    override fun onResume() {
        super.onResume()
        binding.mainPager.requestFocus()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val currentItem = binding.mainPager.currentItem

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {

                if (!pageScrolling) {
                    if (currentItem > 0) {
                        binding.mainPager.currentItem = currentItem - 1;
                        return true;
                    } else if (currentItem == 0) {
                        val count = (binding.mainPager.adapter?.itemCount ?: 0) - 1
                        binding.mainPager.currentItem = count
                    }
                }


            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                //showToast("KEYCODE_DPAD_RIGHT")
                if (!pageScrolling) {
                    if (currentItem < (binding.mainPager.adapter?.itemCount ?: 0) - 1) {
                        binding.mainPager.currentItem = currentItem + 1;
                        return true;
                    } else {
                        binding.mainPager.currentItem = 0
                    }
                }


            }

            KeyEvent.KEYCODE_DPAD_UP_LEFT -> {
                //  showToast("KEYCODE_DPAD_UP_LEFT")
            }

            KeyEvent.KEYCODE_DPAD_UP_RIGHT -> {
                // showToast("KEYCODE_DPAD_UP_RIGHT")

            }

            KeyEvent.KEYCODE_DPAD_UP -> {


            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
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