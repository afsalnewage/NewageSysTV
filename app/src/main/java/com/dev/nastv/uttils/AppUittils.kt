package com.dev.nastv.uttils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dev.nastv.R

object AppUittils {
    fun loadImage(url: String, imgView: ImageView) {
        Glide
            .with(imgView.context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(imgView)
    }
}