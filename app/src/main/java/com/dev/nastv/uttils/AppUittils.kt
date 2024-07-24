package com.dev.nastv.uttils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.dev.nastv.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppUittils {
    @SuppressLint("UseCompatLoadingForDrawables")
    fun loadImage(
        url: String?, imgView: ImageView, scaleType:ImageView.ScaleType=ImageView.ScaleType.FIT_XY,
        placeholderImg: Int =R.drawable.avatar) {


        if (!url.isNullOrBlank()){
Log.d("TTR","Url")
            Glide.with(imgView.context).load(url)
                .placeholder(placeholderImg)
                .into(imgView)

        } else {
            Log.d("TTR","Url dummy")
             imgView.scaleType=scaleType
            Glide.with(imgView.context).load(imgView.context.getDrawable(R.drawable.no_user))
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

    fun convertDurationToMillis(duration: Long): Long {
        return if (duration == C.TIME_UNSET) {
            -1L // Indicating unknown duration
        } else {
            duration / 1000 // Convert from microseconds to milliseconds
        }
    }

    fun getFileIfExists(context: Context, fileName: String): File? {
        val externalStorageDir = Environment.getExternalStorageDirectory()

        // Create the path to Android/media/com.dev.nastv
        val mediaDir = File(externalStorageDir, "Android/media/com.dev.nastv")
        val appSpecificDir = File(mediaDir, "com.dev.nastv.media")

        if (!appSpecificDir.exists()) {
            appSpecificDir.mkdirs()
        }

        val file = File(appSpecificDir, fileName)
        return if (file.exists()) file else null
    }

    fun getFilesInDirectory(context: Context): List<File> {
        val externalStorageDir = Environment.getExternalStorageDirectory()
        val mediaDir = File(externalStorageDir, "Android/media/com.dev.nastv")
        val appSpecificDir = File(mediaDir, "com.dev.nastv.media")

        if (!appSpecificDir.exists()) {
            return emptyList()
        }

        return appSpecificDir.listFiles()?.toList() ?: emptyList()
    }


    fun getFileTypeFromUrl(url: String): String? {
        try {
            val urlObject = URL(url)
            val path = urlObject.path
            val extension = path.substringAfterLast(".")
            return if (extension.isEmpty()) null else extension.lowercase()
        } catch (e: MalformedURLException) {
            return null
        }
    }

    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 30000, TimeUnit.MILLISECONDS).max(900),
        position = Position.Relative(0.1, 0.1)
    )

    val anniversary_party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce23a, 0xff706d, 0xf7906d, 0xb044def),
        emitter = Emitter(duration = 30000, TimeUnit.MILLISECONDS).max(900),
        position = Position.Relative(0.1, 0.1)
    )

}