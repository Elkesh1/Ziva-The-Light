package com.example.zivaministries.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zivaministries.database.MagazineDatabase
import com.example.zivaministries.models.Magazine
import com.example.zivaministries.network.RetrofitClient
import com.example.zivaministries.utils.DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ZivaDebug"
    private val database = MagazineDatabase.getInstance(application)
    private val dao = database.magazineDao()
    private val downloadManager = DownloadManager(application)

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _downloadState = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val downloadState: StateFlow<Map<Int, Int>> = _downloadState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val magazines: StateFlow<List<Magazine>> = dao.getAllMagazines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ✅ 3. THIRD: Combine magazines and searchQuery (both are now defined)
    val filteredMagazines: StateFlow<List<Magazine>> = combine(
        magazines,
        searchQuery
    ) { magazines, query ->
        if (query.isBlank()) {
            magazines
        } else {
            magazines.filter { magazine ->
                magazine.title.contains(query, ignoreCase = true) ||
                        magazine.issueDate.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search functions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    init {
        Log.d(TAG, "ViewModel initialized")
        refreshMagazines()
        forceRefreshStatus()
        checkDatabaseStatus()
    }

    fun refreshMagazines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Refreshing magazines...")
                val response = RetrofitClient.instance.getMagazines()
                val remoteMagazines = response.magazines
                Log.d(TAG, "Fetched ${remoteMagazines.size} magazines from Gist")

                val existingMagazines = dao
                    .getAllMagazinesOnce()
                    .associateBy { it.id }
                Log.d(TAG, "Existing magazines in DB: ${existingMagazines.size}")

                val localMagazines = remoteMagazines.map { remote ->
                    val existing = existingMagazines[remote.id]

                    val fileExists = existing?.localFilePath?.let { File(it).exists() } ?: false
                    val isDownloaded = existing?.isDownloaded ?: false

                    Log.d(TAG, "Magazine ${remote.id}: ${remote.title} - DB says downloaded: $isDownloaded, File exists: $fileExists")

                    Magazine(
                        id = remote.id,
                        title = remote.title,
                        issueDate = remote.issueDate,
                        coverUrl = remote.coverUrl,
                        pdfUrl = remote.pdfUrl,
                        fileSize = remote.fileSize,

                        isDownloaded = isDownloaded && fileExists,

                        localFilePath = if (fileExists) {
                            existing?.localFilePath
                        } else {
                            null
                        },

                        downloadProgress = if (fileExists) {
                            100
                        } else {
                            0
                        },

                        lastPage = existing?.lastPage ?: 0,

                        pageCount = existing?.pageCount ?: 0,

                        isFavorite = existing?.isFavorite ?: false
                    )
                }

                dao.insertAll(localMagazines)
                Log.d(TAG, "Saved ${localMagazines.size} magazines to database")

                val saved = magazines.value
                saved.forEach {
                    Log.d(TAG, "Final: ${it.id} - ${it.title} - Downloaded: ${it.isDownloaded}")
                }

                _errorMessage.value = null

            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing magazines: ${e.message}", e)
                _errorMessage.value = "Failed to load magazines: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadMagazine(magazine: Magazine) {
        viewModelScope.launch {
            Log.d(TAG, "Starting download for ${magazine.id}")

            _downloadState.update { current ->
                current.toMutableMap().apply { put(magazine.id, 0) }
            }

            val result = downloadManager.downloadPdf(magazine) { progress ->
                Log.d(TAG, "Download progress: $progress%")
                _downloadState.update { current ->
                    current.toMutableMap().apply { put(magazine.id, progress) }
                }
            }

            result.onSuccess { pdfFile ->
                Log.d(TAG, "Download complete! File: ${pdfFile.absolutePath}")
                Log.d(TAG, "File exists: ${pdfFile.exists()}, size: ${pdfFile.length()} bytes")

                val updatedMagazine = magazine.copy(
                    isDownloaded = true,
                    localFilePath = pdfFile.absolutePath,
                    downloadProgress = 100
                )

                dao.updateMagazine(updatedMagazine)
                Log.d(TAG, "Database updated for magazine ${magazine.id}")

                val saved = dao.getMagazineById(magazine.id)
                Log.d(TAG, "Verified in DB: ${saved?.isDownloaded}, Path: ${saved?.localFilePath}")

                _downloadState.update { current ->
                    current.toMutableMap().apply { remove(magazine.id) }
                }
            }.onFailure { error ->
                Log.e(TAG, "Download failed: ${error.message}", error)
                _errorMessage.value = "Download failed: ${error.message}"
                _downloadState.update { current ->
                    current.toMutableMap().apply { remove(magazine.id) }
                }
            }
        }
    }

    fun deleteMagazine(magazine: Magazine) {
        viewModelScope.launch {
            val prefs = getApplication<Application>()
                .getSharedPreferences("ziva_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("magazine_${magazine.id}-page").apply()

            downloadManager.deletePdf(magazine.id)
            val updatedMagazine = magazine.copy(
                isDownloaded = false,
                localFilePath = null,
                downloadProgress = 0
            )
            dao.updateMagazine(updatedMagazine)
        }
    }

    fun forceRefreshStatus() {
        viewModelScope.launch {
            val currentMagazines = magazines.value
            currentMagazines.forEach { magazine ->
                if (magazine.isDownloaded) {
                    val file = File(magazine.localFilePath ?: "")
                    if (!file.exists()) {
                        val updated = magazine.copy(
                            isDownloaded = false,
                            localFilePath = null,
                            downloadProgress = 0
                        )
                        dao.updateMagazine(updated)
                    }
                }
            }
        }
    }

    fun updateLastPage(
        magazineId: Int,
        lastPage: Int
    ) {
        viewModelScope.launch {
            dao.updateLastPage(
                magazineId = magazineId,
                lastPage = lastPage
            )
        }
    }

    fun updatePageCount(
        magazineId: Int,
        pageCount: Int
    ) {
        viewModelScope.launch {
            dao.updatePageCount(
                magazineId = magazineId,
                pageCount = pageCount
            )
        }
    }

    fun toggleFavorite(magazine: Magazine) {
        viewModelScope.launch {
            val updated = magazine.copy(isFavorite = !magazine.isFavorite)
            dao.updateMagazine(updated)
        }
    }

    fun checkDatabaseStatus() {
        viewModelScope.launch {
            val all = magazines.value
            all.forEach {
                Log.d("zivaDebug", "Magazine ${it.id}: ${it.title} - Downloaded: ${it.isDownloaded}, Path: ${it.localFilePath}")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}