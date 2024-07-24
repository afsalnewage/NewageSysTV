package com.dev.nastv

//import com.google.firebase.messaging.FirebaseMessaging

//import android.R
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
import android.text.Spannable
import android.text.SpannedString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.work.*
import com.dev.nastv.connection.SocketManager
import com.dev.nastv.databinding.ActivityMainBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.network.Resource
import com.dev.nastv.ui.MainViewModel
import com.dev.nastv.ui.MediaItemsAdapter
import com.dev.nastv.uttils.AppUittils
import com.dev.nastv.uttils.AppUittils.anniversary_party
import com.dev.nastv.uttils.AppUittils.applyFadeInAnimation
import com.dev.nastv.uttils.AppUittils.applySlideInAnimation
import com.dev.nastv.uttils.AppUittils.loadImage
import com.dev.nastv.uttils.AppUittils.party
import com.dev.nastv.uttils.ConnectivityObserver
import com.dev.nastv.uttils.CustomTypefaceSpan
import com.dev.nastv.uttils.SessionUtils
import com.dev.nastv.worker.DownloadWorker
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_FILE_NAME
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_FILE_NAME_REQUEST
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_FILE_URL
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var previousView: View

    private var alertDialog: AlertDialog? = null
    private val imageDuration=30000L
    private val birthdayMusic = R.raw.happy_birthday_acoustic                   //happy_birthday
    private val newJoineeMusic = R.raw.whip_afro_dancehall
    private val anniversaryMusic = R.raw.infinte_melodic_beat
    private val customMusic = R.raw.sunset_piano
    //= getSharedPreferences("work_ids", Context.MODE_PRIVATE)
    private lateinit var workManager: WorkManager
   private var isFromBackgruond =false
    private lateinit var exoPlayer: ExoPlayer

    // private lateinit var firebase: FirebaseMessaging
    private lateinit var binding: ActivityMainBinding
    private lateinit var socket: Socket


    private var leftSwipe = false
    private var pageScrolling = false
    private var currentMediaIndex = 0

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver



    private lateinit var rvAdapter: MediaItemsAdapter

    // val mediaList = ArrayList<MediaItemData>()
    val mediaList = ArrayList<TvMedia>()
    private lateinit var viewModel: MainViewModel
    private val filteredList = ArrayList<TvMedia>()
    private val handler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = Runnable { // val currentMediaIndex = viewModel.mediaIndex.value ?: 0
        Log.d("Index23", "current index $currentMediaIndex")

        // Update the media index by incrementing and wrapping around the media list size
        currentMediaIndex = (currentMediaIndex + 1) % mediaList.size
        playMedia(currentMediaIndex)
        // viewModel.updateIndex(newIndex)


    }

    fun  checkAndDownload(dataList: List<TvMedia>){
        val workManager = WorkManager.getInstance(this)
        val workRequests = mutableListOf<OneTimeWorkRequest>()
        val finalList = ArrayList<TvMedia>()
        for (data in dataList) {
            if (data.event_type == "Video") {

                val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        workDataOf(
                            "url" to data.file_url,
                            KEY_FILE_NAME to data._id
                        )
                    ).build()
                workRequests.add(downloadWorkRequest)


            } else {
                //viewModel.updateFinalList(data)
                finalList.add(data)
            }
        }

            if (workRequests.isNotEmpty()) {
                workManager.enqueue(workRequests)
                val workIds = workRequests.map { it.id }
                observeWorkStatus(workIds)
                var continuation = workManager.beginWith(workRequests.first())

                for (i in 1 until workRequests.size) {
                    continuation = continuation.then(workRequests[i])
                }

                continuation.enqueue()
            }

            mediaList.clear()
            mediaList.addAll(finalList)

           // viewModel.updateIndex(0)
    }





    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleDownloads(dataList: List<TvMedia>) {
        val finalList = ArrayList<TvMedia>()
        val validFileNames = mutableListOf<String>()
        val workManager = WorkManager.getInstance(this)

       // val videos=dataList.filter {  it.event_type == "Video" }
        for (data in dataList) {
            if (data.event_type == "Video") {


                val fileName = "${data._id}.${AppUittils.getFileTypeFromUrl(data.file_url)}"
                val file = AppUittils.getFileIfExists(this, fileName)
                Log.d("Index23"," file exists  ${file?.exists()}")
                if (file!=null &&file.exists()){
                    validFileNames.add(fileName)
                   finalList.add(data)
                }else{
                    viewModel.videoList.add(data)
                }

            }else{
                finalList.add(data)
            }

        }
        mediaList.clear()
        mediaList.addAll(finalList)
       // mediaList.addAll(videoList)
        Log.d("Index23","final list ${mediaList.size}")
       // viewModel.updateIndex(0)
        playMedia(0)

        for (data in viewModel.videoList){
            val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(
                    workDataOf(
                        "url" to data.file_url,
                        KEY_FILE_NAME_REQUEST to data._id
                    )
                ).build()
            workManager.enqueue(downloadWorkRequest)
            observeWork(downloadWorkRequest.id)
            break
        }

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.deleteUnmatchedFiles(this@MainActivity, validFileNames)
        }
    }

    private fun showBackupDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Backup in Progress")
        builder.setMessage("Data backing up, please wait...")
        builder.setCancelable(false)
        alertDialog = builder.create()
        alertDialog?.show()
    }
    private fun dismissBackupDialog() {
        alertDialog?.takeIf { it.isShowing }?.dismiss()
    }
    private fun downloadSingleItem(data: TvMedia){
        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    "url" to data.file_url,
                    KEY_FILE_NAME_REQUEST to data._id
                )
            ).build()
        workManager.enqueue(downloadWorkRequest)
        observeWork(downloadWorkRequest.id)
    }

    private fun observeWork(workId:UUID){
        workManager.getWorkInfoByIdLiveData(workId)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val fileName = workInfo.outputData.getString(KEY_FILE_NAME)
                            val url = workInfo.outputData.getString(KEY_FILE_URL)
                            Log.d("Update211y", "url $url          SUCCEEDED")



                          viewModel.removeDownloadedItem(url.toString())
                                for (item in viewModel.videoList){
                                    downloadSingleItem(item)
                                    break
                                }

                            if (url != null) {


                                updateMediaList(url)


                            }
                            viewModel.incrementCompletedRequests()
                        }

                        WorkInfo.State.RUNNING -> {

                        }

                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            val url = workInfo.outputData.getString(KEY_FILE_URL)
                            val  fileName=workInfo.outputData.getString(KEY_FILE_NAME)
                            val  _id=workInfo.outputData.getString(KEY_FILE_NAME_REQUEST)

                            if (fileName != null) {
                                deleteFailedFile(fileName)
                            }

                            /*
                            if we need we can request for the same
                             */
                             if (url!=null){
                                 val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                                     .setInputData(
                                         workDataOf(
                                             "url" to url,
                                             KEY_FILE_NAME_REQUEST to _id
                                         )
                                     ).build()
                                 workManager.enqueue(downloadWorkRequest)
                                 observeWork(downloadWorkRequest.id)
                             }


                        }

                        else -> {}
                    }

//                    val totalRequests = workIds.size
//                    if (viewModel.completedRequests.value == totalRequests) {
//                        //   onAllDownloadsComplete()
//
//                        // Remove workIds from SharedPreferences after completion
//                        //  val sharedPreferences = getSharedPreferences("work_ids", Context.MODE_PRIVATE)
//                        with(sharedPreferences.edit()) {
//                            remove("work_ids_set")
//                            remove("valid_file_names")
//                            apply()
//                        }
//                    }
                }
            }
    }


    private fun deleteFailedFile(file:String){
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.deleteFileWithFileName(this@MainActivity, file)
        }
    }



    private fun checkWorkStatusOnAppStart() {
        ///   val sharedPreferences = getSharedPreferences("work_ids", Context.MODE_PRIVATE)
        val workIdStrings = sharedPreferences.getStringSet("work_ids_set", emptySet()) ?: emptySet()

        // Log.d("Update211","downloadingFiles $downloadingFiles")
        if (workIdStrings.isNotEmpty()) {
            val workIds = workIdStrings.map { UUID.fromString(it) }
            //viewModel.updateWorkIds(workIds) // Update ViewModel with workIds
            observeWorkStatus(workIds)

//            if (downloadingFiles.isNotEmpty()) {
//                val list = mediaList.filterNot { data ->
//                    downloadingFiles.contains(data._id)
//                }
//             //   Log.d("Update211","list out of downloadingFiles  $list")
//             //   filteredList.clear()
//               // filteredList.addAll(list)
//            }
        } else {
            // No existing workIds, proceed with API call and scheduling downloads if needed
            // scheduleDownloads(dataList) // Call this method with the appropriate data list

        }
    }

    private fun observeWorkStatus(workIds: List<UUID>) {
//        val downloadingFiles =
//            sharedPreferences.getStringSet("valid_file_names", emptySet()) ?: emptySet()
        workIds.forEach { workId ->
            //   WorkManager.getInstance(this).getWorkInfoByIdLiveData(workId)
            workManager.getWorkInfoByIdLiveData(workId)
                .observe(this) { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                val fileName = workInfo.outputData.getString(KEY_FILE_NAME)
                                val url = workInfo.outputData.getString(KEY_FILE_URL)
                                Log.d("Update211y", "url $url          SUCCEEDED")

                                val downloadingFiles =
                                    sharedPreferences.getStringSet("valid_file_names", emptySet()) ?: emptySet()



                                if (url != null) {
                                    val newSet = downloadingFiles.filterTo(mutableSetOf()) { it != url }
                                    sharedPreferences.edit().apply {
                                        putStringSet("valid_file_names", newSet.toSet())
                                        apply()
                                    }

                                    updateMediaList(url)


                                }
                                viewModel.incrementCompletedRequests()
                            }

                            WorkInfo.State.RUNNING -> {

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
                        if (viewModel.completedRequests.value == totalRequests) {
                            //   onAllDownloadsComplete()

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

    private fun updateMediaList(url: String) {

        val downloaded = viewModel.initlMediaList.filter {
            it.file_url == url
        }
        mediaList.add(downloaded.first())
        if (mediaList.size==1){
            binding.animation.pauseAnimation()
            binding.animation.visibility = View.GONE
            dismissBackupDialog()
              playMedia(0)
        }

     //  showToast("Adding items $url")
        Log.d("pppR","new items $downloaded")
        Log.d("pppR","mediaList ${mediaList.size}")
       // mediaList.shuffle()

    }



    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()

        //   this.unregisterReceiver(receiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        workManager = WorkManager.getInstance(applicationContext)
        sharedPreferences = getSharedPreferences("work_ids", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)


        previousView = binding.startView
        exoPlayer = ExoPlayer.Builder(this).build()

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



        viewModel.finalMediaLit.observe(this) {
//            mediaList.clear()
//            mediaList.addAll(it)

           // viewModel.updateIndex(0)
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
                    // applyFadeOutAnimation(binding.mainPager)

                    if (it.data != null) {

                        binding.animation.pauseAnimation()
                        binding.animation.visibility = View.GONE


                         if (it.data.data!!.tv_apps.isNotEmpty()){
                             binding.noDatScreen.visibility=View.GONE
                             viewModel.initlMediaList = it.data.data!!.tv_apps
                             scheduleDownloads(it.data.data!!.tv_apps)
                         }else{
                             //showCustomAlertDialog()
                             binding.noDatScreen.visibility=View.VISIBLE
                             handler.removeCallbacks(autoScrollRunnable)
                              mediaList.clear()
                             if (exoPlayer.isPlaying){
                                 exoPlayer.pause()
                                 exoPlayer.stop()
                                 exoPlayer.clearMediaItems()
                                 binding.videoScreen.visibility=View.GONE
                             }
                             binding.anniversaryFrame.visibility=View.GONE
                             binding.birthdayFrame.visibility=View.GONE
                             binding.newjoineeFrame.visibility=View.GONE
                             binding.customFrame.visibility=View.GONE
                         }
//

//                        mediaList.addAll(it.data.data!!.tv_apps)

                        //checkAndDownload(it.data.data!!.tv_apps)


                    } else {
                        showToast(it.data?.message ?: "data is null")
                    }
                }

                else -> {}
            }
        }


        viewModel.mediaIndex.observe(this, Observer {

            //playMedia(it)
        })




        // Initialize Socket
        socket = SocketManager.getSocket()

        socket.on(Socket.EVENT_CONNECT) {
            Log.d("Socket", "Connected")
//            runOnUiThread {
//                showToast("Socket connected")
//            }

        }
        socket.on("Refresh") { args ->


            runOnUiThread {
                //showToast("Socket trigerd")
//                binding.animation.visibility = View.VISIBLE
//                binding.animation.playAnimation()
                viewModel.getMediaItems()
            }
        }
        SocketManager.connect()


        //checkWorkStatusOnAppStart()
        setUpPlayer()


    }

    fun showCustomAlertDialog() {

        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.no_items_lay , null)

        // Build the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setCancelable(true)
        val alertDialog = builder.create()

        alertDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        exoPlayer.stop()
        isFromBackgruond=true
    }



    val playBackListner=object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING -> {}

                Player.STATE_READY -> {

                    Log.d("RRT","duration ${exoPlayer.duration}")
                    val duration=exoPlayer.duration
                    handler.postDelayed(autoScrollRunnable, duration)
                }

                Player.STATE_ENDED -> {}
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playMedia(index: Int) {
        if (mediaList.isEmpty()) {
            showBackupDialog(this)
            binding.animation.visibility = View.VISIBLE
            binding.animation.playAnimation()
            binding.animationView.playAnimation()
            return
        }

        val media = mediaList[index]
        Log.d("Index23", "playMedia index $index")
        Log.d("YYYY","date without ${media.event_type}")

        handler.removeCallbacks(autoScrollRunnable)
              exoPlayer.removeListener(playBackListner)
       // currentMediaIndex=(currentMediaIndex + 1) % mediaList.size
        when (media.event_type) {

            "Video" -> {
                Log.d("Index23", "playMedia type video $index")


                animateViews(showView = binding.videoScreen, hideView = previousView)
               // if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                    // exoPlayer.release()

                val width = media.video_width ?: 0
                val height = media.video_height ?: 0
                binding.videoView.resizeMode = if (height > width) {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                }




                val fileName = "${media!!._id}.${AppUittils.getFileTypeFromUrl(media!!.file_url)}"
                val file = AppUittils.getFileIfExists(this, fileName)

                val mediaItem = if (file != null) {

                    MediaItem.fromUri(Uri.fromFile(file))
                } else {
                    MediaItem.fromUri(media.file_url)
                }

                exoPlayer.clearMediaItems()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.addListener(playBackListner)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                exoPlayer.play()

                binding.textMessage.text = media.title
                binding.textMessage.isSelected = true
                binding.textMessage.startAnimation()

                previousView = binding.videoScreen
            }

            "Image" -> {
                animateViews(showView = binding.customFrame, hideView = previousView)
                handler.postDelayed(autoScrollRunnable, imageDuration)
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    //exoPlayer.stop()
                    exoPlayer.clearMediaItems() }
                    val mediaItem: MediaItem =
                        MediaItem.fromUri("android.resource://" +this.packageName + "/" + customMusic)
                    exoPlayer!!.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()



                loadImage(media.file_url, binding.customImage, placeholderImg = R.drawable.splash_tv)

                previousView = binding.customFrame
            }

            "Anniversary" -> {
                animateViews(showView = binding.anniversaryFrame, hideView = previousView)
                handler.postDelayed(autoScrollRunnable, imageDuration)
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    //exoPlayer.stop()
                    exoPlayer.clearMediaItems()    }
                    val mediaItem: MediaItem =
                        MediaItem.fromUri("android.resource://" +this.packageName + "/" + anniversaryMusic)
                    exoPlayer!!.setMediaItem(mediaItem)
                     exoPlayer.prepare()
                    exoPlayer.play()

                loadImage(media.file_url, binding.profileAnniversary)
                binding.anniversaryProfileName.text = media.title
                binding.anniversaryProfileDesignation.text = media.user_position
                binding.anniversaryTittle.text =
                    setAnniversaryYearText(media.anniversary_year ?: 0)
                binding.popperViewAnniversary.start(party = anniversary_party)

                previousView = binding.anniversaryFrame

            }

            "Birthday" -> {

                animateViews(showView = binding.birthdayFrame, hideView = previousView)
                handler.postDelayed(autoScrollRunnable, imageDuration)

                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    //exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                }
                    val mediaItem: MediaItem =
                        MediaItem.fromUri("android.resource://" +this.packageName + "/" + birthdayMusic)
                    exoPlayer!!.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()



                binding.birthdayPersonName.text = media.title

                binding.birthdayDat.text = AppUittils.formatDateString(media.end_date)
                binding.imgBanner.visibility = View.VISIBLE
                loadImage(
                    media.file_url,
                    binding.imageBg,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                )
                binding.popperViewBirthDay.start(party = party)

                previousView = binding.birthdayFrame

            }


            "New Joinee"-> {
                animateViews(showView = binding.newjoineeFrame, hideView = previousView)
                handler.postDelayed(autoScrollRunnable, imageDuration)
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    //exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                }
                    val mediaItem: MediaItem =
                        MediaItem.fromUri("android.resource://" +this.packageName + "/" + newJoineeMusic)
                    exoPlayer!!.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()



                binding.apply {
                    loadImage(media.file_url, this.profileNewjoinee)
                    this.newjoineeProfileName.text = media.title
                    this.newjoineeDesignation.text = media.user_position
                    this.textJoiningDate.text =
                        "Date of joining: ${AppUittils.formatDateString(media.event_date)}"
                    this.textEducation.text = media.educational_qualification
                    this.textProfessionalBackground.text = media.professional_background
                    this.textHobbies.text = media.hobbies
                }

                applyFadeInAnimation(binding.imgBannerNewjoinee)
                applyFadeInAnimation(binding.profileNewjoinee)
                applySlideInAnimation(binding.logo1)
                applySlideInAnimation(binding.newjoineeProfileName)
                applySlideInAnimation(binding.newjoineeMessage)
                applySlideInAnimation(binding.newjoineeProfileName)
                applySlideInAnimation(binding.newjoineeDesignation)
                applySlideInAnimation(binding.education)
                applySlideInAnimation(binding.textJoiningDate)
                applySlideInAnimation(binding.textEducation)
                applySlideInAnimation(binding.hobbies)
                applySlideInAnimation(binding.textHobbies)
                applySlideInAnimation(binding.professional)
                applySlideInAnimation(binding.textProfessionalBackground)
                previousView = binding.newjoineeFrame
            }
        }


    }

    private fun setUpPlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                16 * 1024,
                32 * 1024,
                1500,
                2500
            ).build()

        exoPlayer = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()

        binding.videoView.player = exoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("PlayerError", "${error.message} ${error.localizedMessage}")
                Log.e("PlayerError", "error code${error.errorCode} ${error.errorCodeName}")



                when (error.errorCode) {

                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
                    PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSING_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    -> {

                        //commented for tempprary

                     //   val newIndex = (currentMediaIndex + 1) % mediaList.size
                        //for replay
                        playMedia(currentMediaIndex)
//                        val currentMediaIndex = viewModel.mediaIndex.value ?: 0
//                        viewModel.updateIndex((currentMediaIndex + 1) % mediaList.size)

                    }
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND->{
                        playMedia(currentMediaIndex)

                    }


                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                        showToast(

                            "Please check your internet connection "
                        )

//                        (activity as? MainActivity)?.scrollToNextPage()
                    }


                }

            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        binding.animationView.visibility = View.VISIBLE
                        binding.animationView.playAnimation()
                    }

                    Player.STATE_READY -> {
                        Log.d("RRT","duration ${exoPlayer.duration}")
                        binding.animationView.pauseAnimation()
                        binding.animationView.visibility = View.GONE
                    }

                    Player.STATE_ENDED -> {

                      //  exoPlayer.pause()
                      //  val newIndex = (currentMediaIndex + 1) % mediaList.size
                      //  playMedia(newIndex)
//                        val currentMediaIndex = viewModel.mediaIndex.value ?: 0
//                        viewModel.updateIndex((currentMediaIndex + 1) % mediaList.size)
                        //exoPlayer.release()
                        // (activity as? MainActivity)?.scrollToNextPage()
                    }
                }
            }
        })

    }

    private fun showToast(msg: String) {
        Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        //binding.mainPager.requestFocus()
        if (isFromBackgruond&&mediaList.isNotEmpty()){
            playMedia(0)
            isFromBackgruond=false
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {

       // val currentMediaIndex = viewModel.mediaIndex.value ?: 0
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {

                leftSwipe = true
                handler.removeCallbacks(autoScrollRunnable)
//                if (currentMediaIndex > 0) {
//                    currentMediaIndex=(currentMediaIndex - 1) % mediaList.size
//                    playMedia(currentMediaIndex)
//                  //  viewModel.updateIndex((currentMediaIndex - 1) % mediaList.size)
//                } else {
//                   // viewModel.updateIndex(mediaList.size - 1)
//
//                    playMedia(mediaList.size-1)
//                }
                if (currentMediaIndex==0){
                    currentMediaIndex=mediaList.size-1
                    playMedia(currentMediaIndex)
                }
                else{
                    currentMediaIndex=(currentMediaIndex - 1) % mediaList.size
                    playMedia(currentMediaIndex)
                }


            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {

                handler.removeCallbacks(autoScrollRunnable)
                currentMediaIndex = (currentMediaIndex + 1) % mediaList.size
                playMedia(currentMediaIndex)
               // viewModel.updateIndex((currentMediaIndex + 1) % mediaList.size)



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

    private fun animateViews(showView: View, hideView: View) {
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)


        val slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        val slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)

        if (leftSwipe) {
            hideView.startAnimation(slideOutRight)
            hideView.visibility = View.GONE

            showView.startAnimation(slideInLeft)
            showView.visibility = View.VISIBLE
            leftSwipe = false
        } else {
            hideView.startAnimation(slideOut)
            hideView.visibility = View.GONE

            showView.startAnimation(slideIn)
            showView.visibility = View.VISIBLE
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAnniversaryYearText(year: Int): SpannedString {
        val suffix = AppUittils.getOrdinalSuffix(year)
        val regularBrush = this.resources.getFont(R.font.regular_brash)
        val customFont = this.resources.getFont(R.font.sansita_regular)
        val buildedText = buildSpannedString {

            append("Happy ")

            setSpan(
                CustomTypefaceSpan("", regularBrush),
                length - "Happy ".length,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            append("$year $suffix")
            setSpan(SuperscriptSpan(), length - 2, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(0.5f), length - 2, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                CustomTypefaceSpan("", regularBrush),
                length - 2,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            append("\nWork Anniversary ")

            setSpan(
                CustomTypefaceSpan("", regularBrush),
                length - "Work Anniversary ".length,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


        }
        return buildedText
    }

    private fun setLayouts() {

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