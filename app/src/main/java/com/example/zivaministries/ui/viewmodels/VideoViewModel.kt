package com.example.zivaministries.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zivaministries.database.MagazineDatabase
import com.example.zivaministries.models.Video
import com.example.zivaministries.models.RemoteVideo
import com.example.zivaministries.network.RetrofitClient
import com.example.zivaministries.utils.VideoDownloadManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "VideoDebug"
    private val database = MagazineDatabase.getInstance(application)
    private val dao = database.videoDao()
    private val downloadManager = VideoDownloadManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _downloadState = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val downloadState: StateFlow<Map<Int, Int>> = _downloadState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val allVideos: StateFlow<List<Video>> = dao.getAllVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshVideos()
    }

    fun refreshVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Refreshing videos...")
                val response = RetrofitClient.instance.getVideos()
                val remoteVideos = response.videos
                Log.d(TAG, "Fetched ${remoteVideos.size} videos from Gist")

                val existingVideos = allVideos.value.associateBy { it.id }

                val localVideos = remoteVideos.map { remote ->
                    val existing = existingVideos[remote.id]
                    val fileExists = existing?.localFilePath?.let { File(it).exists() } ?: false
                    val isDownloaded = existing?.isDownloaded ?: false

                    Video(
                        id = remote.id,
                        title = remote.title,
                        description = remote.description,
                        speaker = remote.speaker,
                        duration = remote.duration,
                        thumbnailUrl = remote.thumbnailUrl,
                        videoUrl = remote.videoUrl,
                        fileSize = remote.fileSize,
                        isDownloaded = isDownloaded && fileExists,
                        localFilePath = if (fileExists) existing?.localFilePath else null,
                        downloadProgress = if (fileExists) 100 else 0,
                        isFavorite = existing?.isFavorite ?: false
                    )
                }

                dao.insertAll(localVideos)
                _errorMessage.value = null

            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing videos: ${e.message}", e)
                _errorMessage.value = "Failed to load videos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadVideo(video: Video) {
        viewModelScope.launch {
            _downloadState.update { current ->
                current.toMutableMap().apply { put(video.id, 0) }
            }

            val result = downloadManager.downloadVideo(video) { progress ->
                _downloadState.update { current ->
                    current.toMutableMap().apply { put(video.id, progress) }
                }
            }

            result.onSuccess { videoFile ->
                val updatedVideo = video.copy(
                    isDownloaded = true,
                    localFilePath = videoFile.absolutePath,
                    downloadProgress = 100
                )
                dao.updateVideo(updatedVideo)
                _downloadState.update { current ->
                    current.toMutableMap().apply { remove(video.id) }
                }
            }.onFailure { error ->
                _errorMessage.value = "Download failed: ${error.message}"
                _downloadState.update { current ->
                    current.toMutableMap().apply { remove(video.id) }
                }
            }
        }
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch {
            val updated = video.copy(isFavorite = !video.isFavorite)
            dao.updateVideo(updated)
        }
    }

    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            downloadManager.deleteVideo(video.id)
            val updated = video.copy(
                isDownloaded = false,
                localFilePath = null,
                downloadProgress = 0
            )
            dao.updateVideo(updated)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}