package com.dev.nastv.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dev.nastv.MainActivity
import com.dev.nastv.R
import com.dev.nastv.databinding.FragmentVideoBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.uttils.AppUittils.getSimpleCache
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.smb.app.addsapp.model.MediaItemData
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File


private const val ARG_MEDIA = "media_data"


class VideoFragment : Fragment() {


    private var  sourceLoaded=false
    private lateinit var exoPlayer: ExoPlayer
    private var mediaData: TvMedia? = null


    private lateinit var dataSourceFactory: DataSource.Factory


    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            mediaData = it.getSerializable(ARG_MEDIA) as TvMedia
        }
//        Log.d("Source2vid", mediaData?.file_url.toString())


        val cache = getSimpleCache(requireContext())

        // Set up HTTP data source factory with custom headers
        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
            setConnectTimeoutMs(8000)
            setReadTimeoutMs(8000)
            setAllowCrossProtocolRedirects(true)
            setDefaultRequestProperties(mapOf("User-Agent" to "newage_tv"))
        }

        // Set up cache data source factory
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setCacheWriteDataSinkFactory(null) // Read-only cache


        // Create default data source factory
        dataSourceFactory = DefaultDataSource.Factory(requireContext(), cacheDataSourceFactory)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {

        @JvmStatic        //MediaItemData
        fun newInstance(media: TvMedia) =
            VideoFragment().apply {
                arguments = Bundle().apply {

                    putSerializable(ARG_MEDIA, media)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    //    exoPlayer = ExoPlayer.Builder(requireContext()).build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                32 * 1024,  // Min buffer size in milliseconds
                64 * 1024,  // Max buffer size in milliseconds
                1500,       // Min playback start buffer size in milliseconds
                2500        // Min playback resume buffer size in milliseconds
            ).build()


        exoPlayer = ExoPlayer.Builder(requireContext())
            .setLoadControl(loadControl)
            .build()





        binding.videoView.player = exoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Toast.makeText(binding.root.context, "Can't play this video", Toast.LENGTH_SHORT)
                    .show()


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
                        binding.textMessage.visibility = View.VISIBLE
                        binding.textMessage.setText(mediaData!!.title)
                        binding.textMessage.isSelected = true
                        // binding.textMessage.startSlidingAnimation()
                        binding.textMessage.startAnimation()


                    }

                    Player.STATE_ENDED -> {
                        Log.d("TTT", "player state end")
                        (activity as? MainActivity)?.scrollToNextPage()

                    }
                }
            }
        })


        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mediaData!!.file_url))
        sourceLoaded=true
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        Log.d("Source2vid1", mediaData?.file_url.toString())

//        val mediaItem1 = MediaItem.Builder()
//            .setUri(mediaData!!.file_url)
//            .setMimeType(MimeTypes.APPLICATION_MPD)
//            .build()
//val mediaItem =  MediaItem.fromUri(mediaData!!.file_url)
//exoPlayer.setMediaItem(mediaItem1)
//exoPlayer.prepare()
//exoPlayer.play()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Source2vid2", mediaData?.file_url.toString())
        if (!sourceLoaded){
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(mediaData!!.file_url))
            sourceLoaded=true
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }

        exoPlayer.play()

        binding.textMessage.setText(mediaData!!.title)
        binding.textMessage.isSelected = true
        //  binding.textMessage.startSlidingAnimation()

        binding.textMessage.startAnimation()
        //binding.textMessage.animateTo("Monthly gathering May 2024",)
        sourceLoaded=false
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

//    override fun onStop() {
//        super.onStop()
//        exoPlayer.stop()
//        exoPlayer.release()
//    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        exoPlayer.release()
    }


//    fun createDataSourceFactory(context: Context): DefaultDataSource.Factory {
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
//            setDefaultRequestProperties(
//                mapOf(
//                    "User-Agent" to "YourAppName",
//                    "Authorization" to "Bearer your_token"
//                )
//            )
//        }
//        return DefaultDataSource.Factory(context, httpDataSourceFactory)
//    }


}