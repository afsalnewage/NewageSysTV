package com.dev.nastv.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dev.nastv.uttils.DownloadUtil
import com.dev.nastv.worker.DownloadWorker.Companion.KEY_FILE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class FileDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val fileName =inputData.getString(KEY_FILE_NAME) ?: return Result.failure()

        if (DownloadUtil.isFileDownloaded(applicationContext, fileName)) {
            // File already marked as downloaded, skip download
            return Result.success()
        }

        val outputDir = applicationContext.cacheDir
        val file = File(outputDir, fileName)

        if (file.exists()) {
            // File already exists, mark it as downloaded and skip download
            DownloadUtil.markFileAsDownloaded(applicationContext, fileName)
            return   Result.success(
                workDataOf(
                    DownloadWorker.KEY_FILE_URL to  url
                ))
        }

        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to download file")

            withContext(Dispatchers.IO) {
                val fos = FileOutputStream(file)
                fos.use {
                    it.write(response.body?.bytes())
                }
            }

            // Mark file as downloaded
            DownloadUtil.markFileAsDownloaded(applicationContext, fileName)

            Result.success(
                workDataOf(
                    DownloadWorker.KEY_FILE_URL to  url
                ))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
