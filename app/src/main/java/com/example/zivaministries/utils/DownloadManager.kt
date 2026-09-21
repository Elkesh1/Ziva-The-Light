package com.example.zivaministries.utils

import android.content.Context
import android.util.Log
import com.example.zivaministries.models.Magazine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadManager(private val context: Context) {

    private val TAG = "DownloadManager"
    suspend fun downloadPdf(
        magazine: Magazine,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting download for ${magazine.id}")
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(magazine.pdfUrl)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            val contentLength = response.body?.contentLength() ?: -1
            val inputStream = response.body?.byteStream()
            val outputFile = File(context.filesDir, "magazine_${magazine.id}.pdf")

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
            Log.d(TAG, "File exists: ${outputFile.exists()}, size: ${outputFile.length()} bytes")

            Result.success(outputFile)

        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun deletePdf(magazineId: Int): Boolean {
        return try {
            val file = File(context.filesDir, "magazine_$magazineId.pdf")
            file.delete()
        } catch (e
                                    : Exception) {
            false
        }
    }

    fun getPdfFile(magazineId: Int): File? {
        val file = File(context.filesDir, "magazine_$magazineId.pdf")
        return if (file.exists()) file else null
    }
}