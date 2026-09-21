package com.example.zivaministries.utils

import android.content.Context
import android.util.Log
import com.example.zivaministries.models.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class VideoDownloadManager(private val context: Context) {

    private val TAG = "VideoDownloadManager"

    suspend fun downloadVideo(
        video: Video,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(video.videoUrl)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            val contentLength = response.body?.contentLength() ?: -1
            val inputStream = response.body?.byteStream()
            val outputFile = File(context.filesDir, "video_${video.id}.mp4")

            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (contentLength > 0) {
                        val progress = ((totalBytesRead * 100) / contentLength).toInt()
                        onProgress(progress)
                    }
                }
            }

            Log.d(TAG, "File saved to: ${outputFile.absolutePath}")
            Result.success(outputFile)

        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun deleteVideo(videoId: Int): Boolean {
        return try {
            val file = File(context.filesDir, "video_$videoId.mp4")
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getVideoFile(videoId: Int): File? {
        val file = File(context.filesDir, "video_$videoId.mp4")
        return if (file.exists()) file else null
    }
}