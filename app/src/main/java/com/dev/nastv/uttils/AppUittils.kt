package com.dev.nastv.uttils

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.dev.nastv.R
import java.lang.Exception
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object AppUittils {
    fun loadImage(url: String?, imgView: ImageView) {
        try {
            Glide.with(imgView.context).load(url).centerCrop().placeholder(R.color.green)
                .into(imgView)
        } catch (e: Exception) {
            Glide.with(imgView.context).load(imgView.context.getDrawable(R.drawable.avatar))
                .centerCrop().placeholder(R.color.green).into(imgView)
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
}