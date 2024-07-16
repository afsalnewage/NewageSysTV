package com.dev.nastv.ui

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dev.nastv.MainActivity
import com.dev.nastv.databinding.FragmentVideoBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.uttils.AppUittils.getFileIfExists
import com.dev.nastv.uttils.AppUittils.getFileTypeFromUrl
import com.dev.nastv.uttils.AppUittils.getSimpleCache
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource


private const val ARG_MEDIA = "media_data"
private const val ARG_COUNT = "media_count"



class VideoFragment : Fragment() {

    private var mediaCount = 0
    private var width = 0
    private var height = 0
    private var sourceLoaded = false
    private lateinit var exoPlayer: ExoPlayer
    private var mediaData: TvMedia? = null
   // private lateinit var dataSourceFactory: DataSource.Factory
    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaData = it.getSerializable(ARG_MEDIA) as TvMedia
            mediaCount = it.getInt(ARG_COUNT)
        }
        width = mediaData?.video_width ?: 0
        height = mediaData?.video_height ?: 0

//        val cache = getSimpleCache(requireContext())
//
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
//            setConnectTimeoutMs(8000)
//            setReadTimeoutMs(8000)
//            setAllowCrossProtocolRedirects(true)
//            setDefaultRequestProperties(mapOf("User-Agent" to "newage_tv"))
//        }
//
//        val cacheDataSourceFactory = CacheDataSource.Factory()
//            .setCache(cache)
//            .setUpstreamDataSourceFactory(httpDataSourceFactory)
//            .setCacheWriteDataSinkFactory(null) // Read-only cache

        //dataSourceFactory = DefaultDataSource.Factory(requireContext(), cacheDataSourceFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(media: TvMedia, mediaCount: Int) =
            VideoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MEDIA, media)
                    putInt(ARG_COUNT, mediaCount)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                16 * 1024,
                32 * 1024,
                1500,
                2500
            ).build()

        exoPlayer = ExoPlayer.Builder(requireContext())
            .setLoadControl(loadControl)
            .build()

//        exoPlayer = ExoPlayer.Builder(requireContext())
//            .setRenderersFactory(DefaultRenderersFactory(requireContext()))
//            //.setLoadControl(loadControl)
//            .build()

        binding.videoView.player = exoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("PlayerError", "${error.message} ${error.localizedMessage}")
                Log.e("PlayerError", "error code${error.errorCode} ${error.errorCodeName}")



                when(error.errorCode){

                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
                    PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSING_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
                    ->{
                        (activity as? MainActivity)?.scrollToNextPage()
                        exoPlayer.release()
                        sourceLoaded = false

                    }
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT->{
                        showToast(requireContext(),"Please check your internet connection ")

                        (activity as? MainActivity)?.scrollToNextPage()
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
                        binding.animationView.pauseAnimation()
                        binding.animationView.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        exoPlayer.pause()
                        //exoPlayer.release()
                        (activity as? MainActivity)?.scrollToNextPage()
                    }
                }
            }
        })

        val fileName = "${mediaData!!._id}.${getFileTypeFromUrl(mediaData!!.file_url)}"
        val file = getFileIfExists(requireContext(), fileName)

        val mediaItem = if (file != null) {

            MediaItem.fromUri(Uri.fromFile(file))

        } else {
            MediaItem.fromUri(mediaData!!.file_url)
        }

        showToast(requireContext(),"internal media on Create ${file?.name}")
//        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//            .createMediaSource(mediaItem)
        sourceLoaded = true
     //   exoPlayer.setMediaSource(mediaSource)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        if (mediaCount == 1) {
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    override fun onResume() {
        super.onResume()
        if (height > width) {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }

        if (!sourceLoaded) {
            val fileName = "${mediaData!!._id}.${getFileTypeFromUrl(mediaData!!.file_url)}"
            val file = getFileIfExists(requireContext(), fileName)

            val mediaItem = if (file != null) {
                val uri = Uri.fromFile(file)
                MediaItem.fromUri(uri)
            } else {
                MediaItem.fromUri(mediaData!!.file_url)
            }
            showToast(requireContext(),"internal media onResume ${file?.name}")
//            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(mediaItem)
            sourceLoaded = true
         //   exoPlayer.setMediaSource(mediaSource)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        exoPlayer.play()
        binding.textMessage.visibility = View.VISIBLE
        binding.textMessage.text = mediaData!!.title
        binding.textMessage.isSelected = true
        binding.textMessage.startAnimation()
        sourceLoaded = false
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        exoPlayer.release()
    }

    fun getVideoRotation(url: String): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap())
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt() ?: 0
        retriever.release()
        return rotation
    }
}

private fun showToast(context: Context,msg:String){
    Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                    .show()
}

//class VideoFragment : Fragment() {
//
//    private var mediaCount = 0
//    private var width = 0
//    private var height = 0
//    private var sourceLoaded = false
//    private lateinit var exoPlayer: ExoPlayer
//    private var mediaData: TvMedia? = null
//
//
//    private lateinit var dataSourceFactory: DataSource.Factory
//
//
//    private var _binding: FragmentVideoBinding? = null
//    private val binding get() = _binding!!
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//
//            mediaData = it.getSerializable(ARG_MEDIA) as TvMedia
//            mediaCount = it.getInt(ARG_COUNT)
//        }
////       Log.d("Source2vid", mediaData?.file_url.toString())
////       Log.d("Source2vid", " height ${mediaData?.video_height}  width ${mediaData?.video_width}")
//        width = mediaData?.video_width ?: 0
//        height = mediaData?.video_height ?: 0
//
//
//        val cache = getSimpleCache(requireContext())
//
//        // Set up HTTP data source factory with custom headers
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
//            setConnectTimeoutMs(8000)
//            setReadTimeoutMs(8000)
//            setAllowCrossProtocolRedirects(true)
//            setDefaultRequestProperties(mapOf("User-Agent" to "newage_tv"))
//        }
//
//        // Set up cache data source factory
//        val cacheDataSourceFactory = CacheDataSource.Factory()
//            .setCache(cache)
//            .setUpstreamDataSourceFactory(httpDataSourceFactory)
//            .setCacheWriteDataSinkFactory(null) // Read-only cache
//
//
//        // Create default data source factory
//        dataSourceFactory = DefaultDataSource.Factory(requireContext(), cacheDataSourceFactory)
//    }
//
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentVideoBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    companion object {
//
//        @JvmStatic        //MediaItemData
//        fun newInstance(media: TvMedia, mediaCount: Int) =
//            VideoFragment().apply {
//                arguments = Bundle().apply {
//
//                    putSerializable(ARG_MEDIA, media)
//                    putInt(ARG_COUNT, mediaCount)
//                }
//            }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        //exoPlayer = ExoPlayer.Builder(requireContext()).build()
//
//
//        val loadControl = DefaultLoadControl.Builder()
//            .setBufferDurationsMs(
//                32 * 1024,  // Min buffer size in milliseconds
//                64 * 1024,  // Max buffer size in milliseconds
//                1500,       // Min playback start buffer size in milliseconds
//                2500        // Min playback resume buffer size in milliseconds
//            ).build()
//
//
//        exoPlayer = ExoPlayer.Builder(requireContext())
//            .setLoadControl(loadControl)
//            .build()
//
//
//
//        Log.i("Source2vid", mediaData?.file_url.toString())
//
//        // scheduleDownload(mediaData?.file_url.toString())
//
//        binding.videoView.player = exoPlayer
//        exoPlayer.addListener(object : Player.Listener {
//            override fun onPlayerError(error: PlaybackException) {
//                super.onPlayerError(error) //${error.message}
//
//                Log.e("PlayerError", "${error.message}")
//
////                Toast.makeText(binding.root.context, "Can't play this video ", Toast.LENGTH_SHORT)
////                    .show()
//
//
//            }
//
//
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                super.onPlaybackStateChanged(playbackState)
//
//                when (playbackState) {
//
//
//                    Player.STATE_BUFFERING -> {
//                        binding.animationView.visibility = View.VISIBLE
//                        binding.animationView.playAnimation()
//
//
//                    }
//
//                    Player.STATE_READY -> {
//                        binding.animationView.pauseAnimation()
//                        binding.animationView.visibility = View.GONE
//
//
//                    }
//
//                    Player.STATE_ENDED -> {
//                        Log.d("TTT", "player state end")
//                        (activity as? MainActivity)?.scrollToNextPage()
//
//                    }
//                }
//            }
//        })
//        //rotation = getVideoRotation(mediaData!!.file_url)
//        val fileName = "${mediaData!!._id}.${getFileTypeFromUrl(mediaData!!.file_url)}"
//
//        val file = getFileIfExists(requireContext(), fileName)
//
//        Log.d("File34", "file exist $file")
//
//        val mediaItem = if (file != null) {
//            val uri = Uri.fromFile(file)
//            MediaItem.fromUri(uri)
//
//        } else {
//            MediaItem.fromUri(mediaData!!.file_url)
//        }
//
//
//        val mediaSource: MediaSource =
//            ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(mediaItem)
//        sourceLoaded = true
//        exoPlayer.setMediaSource(mediaSource)
//        exoPlayer.prepare()
//        if (mediaCount == 1) {
//            exoPlayer.repeatMode = REPEAT_MODE_ALL
//        }
//        //  Log.d("Source2vid1", mediaData?.file_url.toString())
//
//        //binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
//
//
////        val mediaItem1 = MediaItem.Builder()
////            .setUri(mediaData!!.file_url)
////            .setMimeType(MimeTypes.APPLICATION_MPD)
////            .build()
////val mediaItem =  MediaItem.fromUri(mediaData!!.file_url)
////exoPlayer.setMediaItem(mediaItem1)
////exoPlayer.prepare()
////exoPlayer.play()
//    }
//
//
//
//    override fun onResume() {
//        super.onResume()
//        Log.d("Source2vid", mediaData?.file_url.toString())
//        Log.d("Source2vid", " height ${mediaData?.video_height}  width ${mediaData?.video_width}")
//        if (height > width) {
//
//            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
//
//        } else if (width > height) {
//            // Landscape video, keep default behavior
//
//            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
//        }
//
//        if (!sourceLoaded) {
//
//            val fileName = "${mediaData!!._id}.${getFileTypeFromUrl(mediaData!!.file_url)}"
//            val file = getFileIfExists(requireContext(), fileName)
//            Log.d("File34", "file exist in resume $file")
//            val mediaItem = if (file != null) {
//                val uri = Uri.fromFile(file)
//                MediaItem.fromUri(uri)
//
//            } else {
//                MediaItem.fromUri(mediaData!!.file_url)
//            }
//
//
//
//            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(mediaItem)
//            sourceLoaded = true
//            exoPlayer.setMediaSource(mediaSource)
//            exoPlayer.prepare()
//            exoPlayer.playWhenReady = true
//        }
//
//        exoPlayer.play()
//        binding.textMessage.visibility = View.VISIBLE
//        binding.textMessage.setText(mediaData!!.title)
//        binding.textMessage.isSelected = true
//        binding.textMessage.startAnimation()
//        //  binding.textMessage.setText(mediaData!!.title)
//        // binding.textMessage.isSelected = true
//        //  binding.textMessage.startSlidingAnimation()
//
//        binding.textMessage.startAnimation()
//        //binding.textMessage.animateTo("Monthly gathering May 2024",)
//        sourceLoaded = false
//    }
//
//    override fun onPause() {
//        super.onPause()
//        exoPlayer.pause()
//    }
//
////    override fun onStop() {
////        super.onStop()
////        exoPlayer.stop()
////        exoPlayer.release()
////    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        exoPlayer.stop()
//        exoPlayer.release()
//    }
//
//    fun getVideoRotation(url: String): Int {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(url, HashMap())
//        val rotation =
//            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt()
//                ?: 0
//        retriever.release()
//        return rotation
//    }
//
////    fun createDataSourceFactory(context: Context): DefaultDataSource.Factory {
////        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
////            setDefaultRequestProperties(
////                mapOf(
////                    "User-Agent" to "YourAppName",
////                    "Authorization" to "Bearer your_token"
////                )
////            )
////        }
////        return DefaultDataSource.Factory(context, httpDataSourceFactory)
////    }
//
//
//}