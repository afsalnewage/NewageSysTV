package com.dev.nastv.uttils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.dev.nastv.R
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object AppUittils {
    @SuppressLint("UseCompatLoadingForDrawables")
    fun loadImage(url: String?, imgView: ImageView) {


        if (!url.isNullOrBlank()){

            Glide.with(imgView.context).load(url)
                .placeholder(R.color.green)
                .into(imgView)

        } else {
             imgView.scaleType=ImageView.ScaleType.FIT_XY
            Glide.with(imgView.context).load(imgView.context.getDrawable(R.drawable.avatar))
                .fitCenter().placeholder(R.color.green).into(imgView)
        }

    }

    fun getOrdinalSuffix(number: Int): String? {
        return if (number % 100 >= 11 && number % 100 <= 13) {
            "th"
        } else when (number % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    fun applySlideInAnimation(view: View) {
        view.visibility = View.INVISIBLE
        val parentWidth = (view.parent as ViewGroup).width
        view.translationX = -parentWidth.toFloat()
        view.visibility = View.VISIBLE
        view.animate().translationX(0f).setDuration(1000).start()
        //  view.visibility=View.VISIBLE
    }

    fun applyFadeInAnimation(view: View, duration: Long = 1000) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(duration).setListener(null)
    }

    fun applyFadeOutAnimation(view: View, duration: Long = 1000) {
        view.alpha = 1f // Start with full opacity
        view.animate().alpha(0f) // End with full transparency
            .setDuration(duration).withEndAction {
                view.visibility = View.GONE // Hide the view after animation completes
            }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateString(inputDate: String): String {

        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)

        // Parse the input date string to a ZonedDateTime object
        val date = ZonedDateTime.parse(inputDate, inputFormatter)

        // Format the date to the desired output format
        return date.format(outputFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }



        private var simpleCache: SimpleCache? = null

        fun getSimpleCache(context: Context): SimpleCache {
            if (simpleCache == null) {
                val cacheDirectory = File(context.cacheDir, "media")
                val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100MB max cache size
                simpleCache = SimpleCache(cacheDirectory, evictor)
            }
            return simpleCache!!
        }

}