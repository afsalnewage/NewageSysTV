package com.dev.nastv.ui

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
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.smb.app.addsapp.model.MediaItemData
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_MEDIA = "media_data"


class VideoFragment : Fragment() {

    private lateinit var exoPlayer: ExoPlayer
    private var mediaData:TvMedia?=null
     //private lateinit var binding: FragmentVideoBinding

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
            mediaData=it.getSerializable(ARG_MEDIA)as TvMedia
        }
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
        fun newInstance( media: TvMedia) =
            VideoFragment().apply {
                arguments = Bundle().apply {

                    putSerializable(ARG_MEDIA,media)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

 exoPlayer=ExoPlayer.Builder(requireContext()).build()


//        binding.textMessage.addOnAnimationListener(object: AnimatedTextView.AnimationListener{
//            override fun onAnimationStart(text: String, bareText: String) {
//
//
//            }
//
//            override fun onAnimationEnd(text: String, bareText: String) {
//
//            }
//        })


       // binding.textMessage.getBareText()



   binding.videoView.player=exoPlayer
exoPlayer.addListener(object : Player.Listener{
    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(binding.root.context,"Can't play this video", Toast.LENGTH_SHORT).show()


    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        when (playbackState) {


            Player.STATE_BUFFERING -> {
                binding.progressBar.visibility = View.VISIBLE

            }
            Player.STATE_READY -> {
                binding.progressBar.visibility = View.GONE
                binding.textMessage.visibility=View.VISIBLE
               binding.textMessage.setText(mediaData!!.title)
               // binding.textMessage.startSparkleAnimation()
             //   binding.textMessage.animateTo("Monthly gathering May 2024",)
              //  binding.textMessage.canScrollHorizontally(0)
//             val party=   Party(
//                    speed = 0f,
//                    maxSpeed = 30f,
//                    damping = 0.9f,
//                    spread = 360,
//                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
//                    emitter = Emitter(duration = 10000, TimeUnit.MILLISECONDS).max(1000),
//                    position = Position.Relative(0.5, 0.5)
//                )
//
//                binding.popperView.start(party =party )


            }
            Player.STATE_ENDED -> {
                Log.d("TTT","player state end")
                (activity as? MainActivity)?.scrollToNextPage()

            }
        }
    }
})

val mediaItem =  MediaItem.fromUri(mediaData!!.file_url)
exoPlayer.setMediaItem(mediaItem)
exoPlayer.prepare()
//exoPlayer.play()
}

    override fun onResume() {
        super.onResume()
        exoPlayer.playWhenReady = true
        exoPlayer.play()

        binding.textMessage.setText(mediaData!!.title)
        //binding.textMessage.animateTo("Monthly gathering May 2024",)
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
}