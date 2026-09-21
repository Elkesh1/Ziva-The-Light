package com.example.zivaministries.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.zivaministries.models.Video
import com.example.zivaministries.ui.components.VideoCard
import com.example.zivaministries.ui.viewmodels.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit,
    onDownloadClick: (Video) -> Unit,
    onFavoriteClick: (Video) -> Unit
) {
    val videos by viewModel.allVideos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val downloadStates by viewModel.downloadState.collectAsState()

    // Track which video is currently playing
    var playingVideoId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            videos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No videos available")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(videos) { video ->
                        VideoCard(
                            video = video,
                            isPlaying = playingVideoId == video.id,
                            onPlayClick = {
                                playingVideoId = video.id
                                onVideoClick(video)
                            },
                            onDownloadClick = { onDownloadClick(video) },
                            onFavoriteClick = { onFavoriteClick(video) }
                        )
                    }
                }
            }
        }
    }
}