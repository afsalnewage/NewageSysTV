package com.dev.nastv.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dev.nastv.databinding.PosterItemBinding
import com.dev.nastv.databinding.VideoItemBinding
import com.dev.nastv.uttils.AppUittils.loadImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.smb.app.addsapp.model.MediaItemData
import com.smb.app.addsapp.model.Type

class MediaItemAdapter(val mediaList:ArrayList<MediaItemData>,private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource
    private var player: ExoPlayer? = null
    private var currentViewHolder: VideoViewHolder? = null

    init {

        exoPlayer=ExoPlayer.Builder(context).build()

    }

    companion object {
        private const val VIEW_TYPE_VIDEO = 1
        private const val VIEW_TYPE_IMAGE = 2
    }

    class VideoViewHolder(val mBinding:VideoItemBinding ) : RecyclerView.ViewHolder(mBinding.root) {


        fun videoBind(mediaItem: MediaItemData,exoPlayer: ExoPlayer) {
            mBinding.videoView.player=exoPlayer
            exoPlayer.addListener(object : Player.Listener{
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(mBinding.root.context,"Can't play this video", Toast.LENGTH_SHORT).show()


                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    when (playbackState) {


                        Player.STATE_BUFFERING -> {
                            mBinding.progressBar.visibility = View.VISIBLE

                        }
                        Player.STATE_READY -> {
                            mBinding.progressBar.visibility = View.GONE

                        }
                        Player.STATE_ENDED -> {
                            Log.d("TTT","player state end")


                        }
                    }
                }
            })
            val mediaItem =  MediaItem.fromUri(mediaItem.sourceUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()

        }
    }

    class ImageViewHolder(val mBinding: PosterItemBinding) : RecyclerView.ViewHolder(mBinding.root) {


        fun ImageBind(mediaItem: MediaItemData) {
            loadImage(mediaItem.sourceUrl,mBinding.imgBanner)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (mediaList[position].mediaType) {
            Type.Vidio->{ VIEW_TYPE_VIDEO}
            Type.Image->{ VIEW_TYPE_IMAGE}

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VIDEO -> {

                val binding=VideoItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                VideoViewHolder(binding)
            }
            VIEW_TYPE_IMAGE -> {
                val binding = PosterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

}