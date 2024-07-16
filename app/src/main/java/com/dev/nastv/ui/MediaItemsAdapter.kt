package com.dev.nastv.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.nastv.R
import com.dev.nastv.databinding.PosterItemBinding
import com.dev.nastv.databinding.VideoItemBinding
import com.dev.nastv.model.TvMedia
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class MediaItemsAdapter ( var exoPlayer:ExoPlayer,
    private val context: Context,//playerView:PlayerView
    private var mediaList: ArrayList<TvMedia>,val videoCalbacks:(mediaItem:MediaItem,playerView:PlayerView,pos:Int)->Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var currentMediaItemPosition = -1
    companion object {
        private const val TYPE_VIDEO = 0
        private const val TYPE_IMAGE = 1
        private const val TYPE_ANNIVERSARY = 2
        private const val TYPE_BIRTHDAY = 3
        private const val TYPE_NEW_JOINEE = 4
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_VIDEO -> {
                val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoViewHolder(binding)

            }
            TYPE_IMAGE, TYPE_ANNIVERSARY, TYPE_BIRTHDAY, TYPE_NEW_JOINEE -> {
                val binding = PosterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageViewHolder(binding)

            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (mediaList[position].event_type) {
            "Video" -> TYPE_VIDEO
            "Image" -> TYPE_IMAGE
            "Anniversary" -> TYPE_ANNIVERSARY
            "Birthday" -> TYPE_BIRTHDAY
            "New Joinee" -> TYPE_NEW_JOINEE
            else -> throw IllegalArgumentException("Invalid media type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        when (holder.itemViewType) {
            TYPE_VIDEO ->{

                (holder as VideoViewHolder).bind(mediaItem,position)

            }
            TYPE_IMAGE, TYPE_ANNIVERSARY, TYPE_BIRTHDAY, TYPE_NEW_JOINEE ->
                (holder as ImageViewHolder).bind(mediaItem)

        }
    }

    inner class VideoViewHolder(val binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(media: TvMedia,position: Int) {


            if (currentMediaItemPosition == absoluteAdapterPosition &&currentMediaItemPosition!= RecyclerView.NO_POSITION) {
                // Current item: Prepare and play the video
                val mediaItem = MediaItem.fromUri(media.file_url)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                binding.videoView.player = exoPlayer
            } else {
                // Other items: Pause any previously playing video
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                }
             //   binding.videoView.player = null
            }
            // Initialize your video player (e.g., ExoPlayer) here
//            val mediaItem = MediaItem.fromUri(media.file_url)
//            videoCalbacks.invoke(mediaItem,media.title.toString(),position)
//              binding.videoView.player=exoPlayer

         ////   initializePlayer(binding.videoView, media)
            binding.textMessage.setText(media.title)
        }

        private fun initializePlayer(playerView: PlayerView, media: TvMedia) {
            val player = ExoPlayer.Builder(context).build()
            playerView.player = player
            val mediaItem = MediaItem.fromUri(media.file_url)
            player.setMediaItem(mediaItem)
            player.prepare()
        }
    }

    inner class ImageViewHolder(val  binding: PosterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(media: TvMedia) {
            // Bind your image data here
         //   val imageView = itemView.findViewById<ImageView>(R.id.image_view)

            Glide.with(context).load(media.file_url).into(binding.imageBg)
        }
    }

    fun updateCurrentPlayingPosition(position: Int) {
        currentMediaItemPosition = position
        notifyItemChanged(position) // Notify adapter of change
    }


}