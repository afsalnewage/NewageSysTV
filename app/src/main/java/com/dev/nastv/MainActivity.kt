package com.dev.nastv

//import com.google.firebase.messaging.FirebaseMessaging

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
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
import com.dev.nastv.ui.UiPagesAdapter
import com.dev.nastv.uttils.AppUittils
import com.dev.nastv.uttils.AppUittils.applyFadeInAnimation
import com.dev.nastv.uttils.AppUittils.applyFadeOutAnimation
import com.dev.nastv.uttils.AppUittils.getFilesInDirectory
import com.dev.nastv.uttils.ConnectivityObserver
import com.dev.nastv.uttils.SessionUtils
import com.dev.nastv.worker.DownloadWorker
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_FILE_NAME
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_RESULT_PATH
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var workManager: WorkManager

    // private lateinit var firebase: FirebaseMessaging
    private lateinit var binding: ActivityMainBinding
    private lateinit var socket: Socket
    private var pageScrolling = false

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver


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
        for (data in dataList) {
            if (data.event_type == "Video") {
                val fileName = "${data._id}.${AppUittils.getFileTypeFromUrl(data.file_url)}"
                val file = AppUittils.getFileIfExists(this, fileName)
                validFileNames.add(fileName)

                if (file != null) {
                    Log.d("DownloadScheduler", "File already exists: $fileName")
                    continue
                } else {
                    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setInputData(
                            workDataOf(
                                "url" to data.file_url,
                                KEY_FILE_NAME to data._id
                            )
                        ).build()

                    if (workContinuation == null) {
                        workContinuation = workManager.beginWith(downloadWorkRequest)
                    } else {
                        workContinuation = workContinuation.then(downloadWorkRequest)
                    }
                }
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
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

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



        viewModel.workId.observe(this, Observer {
            it?.let { id ->
                workManager.getWorkInfoByIdLiveData(id).observe(this, Observer { workInfo ->

                    when (workInfo.state) {
                        WorkInfo.State.ENQUEUED -> {
                            binding.progressBar2.visibility = View.VISIBLE
                        }

                        WorkInfo.State.RUNNING -> {
                            binding.progressBar2.visibility = View.VISIBLE
                        }

                        WorkInfo.State.CANCELLED -> {
                            binding.progressBar2.visibility = View.GONE
                        }

                        WorkInfo.State.SUCCEEDED -> {
                            binding.progressBar2.visibility = View.GONE
                        }

                        else -> {}
                    }
                    Log.d("WorkInfo23", "${workInfo.state}")
                    //val da= workInfo.
                    val workResult = workInfo.outputData.getString(KEY_RESULT_PATH)
                    Log.d("WorkInfo23", "downloaded path $workResult")
                })
            }

        })




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

                        mediaList.addAll(it.data.data!!.tv_apps)
                        pagerAdapter = UiPagesAdapter(this, it.data.data!!.tv_apps)
                        updatePager()


                        scheduleDownloads(it.data.data!!.tv_apps)


                    } else {
                        showToast(it.data?.message ?: "data is null")
                    }
                }
            }
        }


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


    private fun updatePager() {
        applyFadeInAnimation(binding.mainPager)
        binding.mainPager.adapter = pagerAdapter
        binding.mainPager.offscreenPageLimit = 1
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
                    }

                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        pageScrolling = true
                    }

                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        pageScrolling = true
                    }

                }
            }
        })
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