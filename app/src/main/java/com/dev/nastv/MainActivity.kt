     package com.dev.nastv

import android.graphics.Path.Direction
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast

import com.dev.nastv.databinding.ActivityMainBinding

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.smb.animatedtextview.AnimatedTextView
import com.smb.app.addsapp.model.MediaItemData
import com.smb.app.addsapp.model.Type
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

     class MainActivity : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var binding: ActivityMainBinding

    val data=  MediaItemData(
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        Type.Vidio,
        6000
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        exoPlayer=ExoPlayer.Builder(this).build()

        // binding.textMessage.setSelected(true)
     //   binding.textMessage.setPrefixSuffix("Price: ", " CAD")
       // binding.textMessage.setText("123,456")
        binding.textMessage.addOnAnimationListener(object: AnimatedTextView.AnimationListener{
            override fun onAnimationStart(text: String, bareText: String) {


            }

            override fun onAnimationEnd(text: String, bareText: String) {

            }
        })


        binding.textMessage.getBareText()

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
                       binding.textMessage.setText("Monthly gathering May 2024")
                        binding.textMessage.animateTo("Monthly gathering May 2024",)
                        binding.textMessage.canScrollHorizontally(0)
                     val party=   Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                            emitter = Emitter(duration = 10000, TimeUnit.MILLISECONDS).max(1000),
                            position = Position.Relative(0.5, 0.5)
                        )

                        binding.popperView.start(party =party )


                    }
                    Player.STATE_ENDED -> {
                        Log.d("TTT","player state end")


                    }
                }
            }
        })
        val mediaItem =  MediaItem.fromUri(data.sourceUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

    }
}