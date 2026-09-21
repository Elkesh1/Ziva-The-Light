package com.example.zivaministries.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.zivaministries.models.Magazine
import com.example.zivaministries.models.Video
import com.example.zivaministries.ui.components.SidebarContent
import com.example.zivaministries.ui.viewmodels.MainViewModel
import com.example.zivaministries.ui.viewmodels.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    videoViewModel: VideoViewModel,
    onMagazineClick: (Magazine) -> Unit,
    onDownloadClick: (Magazine) -> Unit,
    onVideoClick: (Video) -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val magazines by viewModel.magazines.collectAsState()
    val videos by videoViewModel.allVideos.collectAsState()

    val isMagazineLoading by viewModel.isLoading.collectAsState()
    val isVideoLoading by videoViewModel.isLoading.collectAsState()

    val magazineDownloadState by viewModel.downloadState.collectAsState()

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val downloadedMagazines = remember(magazines) {
        magazines.filter { it.isDownloaded }
    }

    val readingMagazines = remember(magazines) {
        magazines.filter { it.isDownloaded && it.lastPage > 0 }
    }

    val latestMagazine = magazines.firstOrNull()

    val continueReading = readingMagazines.firstOrNull()

    val latestVideos = remember(videos) {
        videos.take(3)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SidebarContent(
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        onSettingsClick()
                    },
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        onAboutClick()
                    },
                    onContactClick = {
                        scope.launch { drawerState.close() }
                        onContactClick()
                    },
                    onClose = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Ziva Ministries",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {

                // =========================
                // WELCOME
                // =========================

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp)
                ) {
                    Text(
                        text = "Welcome back 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Grow in faith through the Word, magazines and teachings.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // =========================
                // STATS
                // =========================

                StatsSection(
                    totalMagazines = magazines.size,
                    downloaded = downloadedMagazines.size,
                    reading = readingMagazines.size
                )

                Spacer(modifier = Modifier.height(28.dp))

                // =========================
                // CONTINUE READING
                // =========================

                if (continueReading != null) {

                    SectionTitle(
                        title = "Continue Reading",
                        icon = Icons.Default.MenuBook
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ContinueReadingCard(
                        magazine = continueReading,
                        onClick = {
                            onMagazineClick(continueReading)
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // =========================
                // LATEST MAGAZINE
                // =========================

                SectionTitle(
                    title = "Latest Magazine",
                    icon = Icons.Default.AutoStories
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isMagazineLoading && magazines.isEmpty() -> {
                        LoadingCard()
                    }

                    latestMagazine != null -> {
                        LatestMagazineCard(
                            magazine = latestMagazine,
                            downloadProgress = magazineDownloadState[
                                latestMagazine.id
                            ],
                            onRead = {
                                if (latestMagazine.isDownloaded) {
                                    onMagazineClick(latestMagazine)
                                }
                            },
                            onDownload = {
                                onDownloadClick(latestMagazine)
                            }
                        )
                    }

                    else -> {
                        EmptyContentCard(
                            message = "No magazines available yet."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // =========================
                // DOWNLOADED MAGAZINES
                // =========================

                if (downloadedMagazines.isNotEmpty()) {

                    SectionTitle(
                        title = "Your Library",
                        icon = Icons.Default.Download
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            )
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        downloadedMagazines
                            .take(5)
                            .forEach { magazine ->

                                DownloadedMagazineCard(
                                    magazine = magazine,
                                    onClick = {
                                        onMagazineClick(magazine)
                                    }
                                )
                            }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // =========================
                // LATEST VIDEOS
                // =========================

                SectionTitle(
                    title = "Latest Videos",
                    icon = Icons.Default.PlayArrow
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isVideoLoading && videos.isEmpty() -> {
                        LoadingCard()
                    }

                    latestVideos.isNotEmpty() -> {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            latestVideos.forEach { video ->

                                VideoHomeCard(
                                    video = video,
                                    onClick = {
                                        onVideoClick(video)
                                    }
                                )
                            }
                        }
                    }

                    else -> {
                        EmptyContentCard(
                            message = "No videos available yet."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


// =====================================================
// STATS
// =====================================================

@Composable
private fun StatsSection(
    totalMagazines: Int,
    downloaded: Int,
    reading: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            StatItem(
                value = totalMagazines.toString(),
                label = "Magazines"
            )

            StatItem(
                value = downloaded.toString(),
                label = "Downloaded"
            )

            StatItem(
                value = reading.toString(),
                label = "Reading"
            )
        }
    }
}


// =====================================================
// SECTION TITLE
// =====================================================

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}


// =====================================================
// CONTINUE READING
// =====================================================

@Composable
private fun ContinueReadingCard(
    magazine: Magazine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = magazine.coverUrl,
                contentDescription = magazine.title,
                modifier = Modifier
                    .width(95.dp)
                    .height(125.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Continue Reading",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = magazine.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = magazine.issueDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (magazine.pageCount > 0) {
                    LinearProgressIndicator(
                        progress = {
                            (
                                    magazine.lastPage.toFloat() /
                                            magazine.pageCount.toFloat()
                                    ).coerceIn(0f, 1f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Page ${magazine.lastPage + 1} of ${magazine.pageCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Continue reading",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


// =====================================================
// LATEST MAGAZINE
// =====================================================

@Composable
private fun LatestMagazineCard(
    magazine: Magazine,
    downloadProgress: Int?,
    onRead: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {

            AsyncImage(
                model = magazine.coverUrl,
                contentDescription = magazine.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    ),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = magazine.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = magazine.issueDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    downloadProgress != null -> {

                        Text(
                            text = "Downloading $downloadProgress%",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = {
                                (downloadProgress / 100f)
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    magazine.isDownloaded -> {

                        Button(
                            onClick = onRead,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Read Magazine")
                        }
                    }

                    else -> {

                        OutlinedButton(
                            onClick = onDownload,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Download Magazine")
                        }
                    }
                }
            }
        }
    }
}


// =====================================================
// DOWNLOADED MAGAZINE CARD
// =====================================================

@Composable
private fun DownloadedMagazineCard(
    magazine: Magazine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {

            AsyncImage(
                model = magazine.coverUrl,
                contentDescription = magazine.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp
                        )
                    ),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = magazine.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (magazine.pageCount > 0) {
                        "Page ${magazine.lastPage + 1} / ${magazine.pageCount}"
                    } else {
                        "Downloaded"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =====================================================
// VIDEO CARD
// =====================================================
@Composable
private fun VideoHomeCard(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(75.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {

                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.9f
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.speaker,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.duration,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


// =====================================================
// LOADING
// =====================================================

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


// =====================================================
// EMPTY
// =====================================================

@Composable
private fun EmptyContentCard(
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// =====================================================
// STAT
// =====================================================

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}