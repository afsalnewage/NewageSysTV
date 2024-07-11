package com.dev.nastv.worker

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.net.URL

class DownloadWorker (private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val name =inputData.getString(KEY_FILE_NAME) ?: return Result.failure() /* id of the video object */
           //   Log.d("WorkInfo23","file name $name")
        val fileExtension = getFileTypeFromUrl(url)                //url.substringAfterLast('.', "")
        val fileName = "$name.$fileExtension"
      // val file = File(applicationContext.filesDir, fileName)
       // val outputFile = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

       // val appSpecificDir = context.getExternalFilesDir("com.dev.nastv.data")
        //val mediaDir = context.getExternalFilesDir(Environment.Directory_m)
        val externalStorageDir = Environment.getExternalStorageDirectory()

        // Create the path to Android/media/com.dev.nastv
        val mediaDir = File(externalStorageDir, "Android/media/com.dev.nastv")
        val appSpecificDir = File(mediaDir, "com.dev.nastv.media")
        if (!appSpecificDir.exists()) {
            appSpecificDir.mkdirs()
        }
        val file = File(appSpecificDir, fileName)



        val request = DownloadManager.Request(Uri.parse(url))
            .setMimeType(getMimeType(fileExtension!!))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Downloading $fileExtension file")
            .setDestinationUri(Uri.fromFile(file))

        val downloadId = downloadManager.enqueue(request)
        return if (monitorDownload(downloadId)) {
            Result.success(
                workDataOf(KEY_RESULT_PATH to file.absolutePath                // bitmap.allocationByteCount
                )

            )
        } else {
            Result.failure()
        }





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
    private fun getMimeType(fileExtension: String): String {
        return when (fileExtension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            else -> "*/*" // Fallback to a generic MIME type
        }
    }

    @SuppressLint("Range")
    private fun monitorDownload(downloadId: Long): Boolean {
        var isDownloadComplete = false
        var downloadStatus = false

        val downloadQuery = DownloadManager.Query().setFilterById(downloadId)

        while (!isDownloadComplete) {
            val cursor = downloadManager.query(downloadQuery)
            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        isDownloadComplete = true
                        downloadStatus = true
                    }
                    DownloadManager.STATUS_FAILED -> {
                        isDownloadComplete = true
                        downloadStatus = false
                    }
                   DownloadManager.STATUS_PENDING->{

                   }
                }
            }
            cursor?.close()
            if (!isDownloadComplete) {
                Thread.sleep(1000) // Sleep for a while before querying again
            }
        }
        return downloadStatus
    }

    companion object{
        const val KEY_CONTENT_URI="KEY_CONTENT_URI"
        const val KEY_COMPRESSION_THRESHOLD="KEY_COMPRESSION_THRESHOLD"
        const val KEY_RESULT_PATH="KEY_RESULT_PATH"
        const val KEY_ORIGINEL_SIZE="KEY_ORIGINEL_SIZE"

        const val KEY_FILE_URL = "key_file_url"
        const val KEY_FILE_TYPE = "key_file_type"
        const val KEY_FILE_NAME = "key_file_name"
        const val KEY_FILE_URI = "key_file_uri"
    }

    /*
     try {
         val okHttpClient = OkHttpClient.Builder().build()
         val request = Request.Builder().url(url).build()
         val call = okHttpClient.newCall(request)
         val response = call.execute()

         if (response.isSuccessful) {
             val inputStream = response.body!!.byteStream()
             val outputStream =
                 withContext(Dispatchers.IO) {
                     FileOutputStream(file)
                 }

             val buffer = ByteArray(1024)
             var readBytes: Int
             while (withContext(Dispatchers.IO) {
                     inputStream.read(buffer)
                 }.also { readBytes = it } > 0) {
                 withContext(Dispatchers.IO) {
                     outputStream.write(buffer, 0, readBytes)
                 }
             }

             withContext(Dispatchers.IO) {
                 outputStream.close()
                 inputStream.close()
             }

             return Result.success()
         } else {
             return Result.failure()
         }
     } catch (e: Exception) {
         return Result.failure()
     }

        */


}