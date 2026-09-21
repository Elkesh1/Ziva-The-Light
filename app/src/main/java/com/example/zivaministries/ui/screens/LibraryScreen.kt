package com.example.zivaministries.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zivaministries.models.Magazine
import com.example.zivaministries.ui.components.MagazineCard
import com.example.zivaministries.ui.components.SearchBar
import com.example.zivaministries.ui.components.ShimmerMagazineCard
import com.example.zivaministries.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onMagazineClick: (Magazine) -> Unit,
    onDownloadClick: (Magazine) -> Unit,
    onReadClick: (Magazine) -> Unit
) {
    val magazines by viewModel.magazines.collectAsState()
    val filteredMagazines by viewModel.filteredMagazines.collectAsState()
    val downloadStates by viewModel.downloadState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Track whether search is active
    val isSearchActive = searchQuery.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchActive) {
                        Text(
                            "Ziva Ministries",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        // show search bar in the title area
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onClear = { viewModel.clearSearch() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Only show refresh icon when not searching
                    if (!isSearchActive) {
                        IconButton(onClick = { viewModel.refreshMagazines() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    // Search icon is inside the title area when not expanded

                    if (!isSearchActive) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onClear = { viewModel.clearSearch() }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            // 1. Show shimmer while loading AND no data
            isLoading && magazines.isEmpty() -> {
                ShimmerLoadingGrid(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            // 2. Show shimmer while refreshing with existing data
            isLoading && magazines.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Show a loading indicator at the top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    // Show existing data
                    MagazineGrid(
                        magazines = magazines,
                        downloadStates = downloadStates,
                        onMagazineClick = onMagazineClick,
                        onDownloadClick = onDownloadClick,
                        onReadClick = onReadClick,
                        onFavoriteClick = { magazine -> viewModel.toggleFavorite(magazine) },
                    )
                }
            }

            // 3. Show empty state
            magazines.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No magazines available")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refreshMagazines() }) {
                            Text("Refresh")
                        }
                    }
                }
            }

            // 4. Show search empty state
            isSearchActive && filteredMagazines.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No magazines found for '$searchQuery'",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "try a different search term",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. Show actual content
            else -> {
                MagazineGrid(
                    magazines = if (isSearchActive) filteredMagazines else magazines,
                    downloadStates = downloadStates,
                    onMagazineClick = onMagazineClick,
                    onDownloadClick = onDownloadClick,
                    onReadClick = onReadClick,
                    onFavoriteClick = { magazine -> viewModel.toggleFavorite(magazine) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun MagazineGrid(
    magazines: List<Magazine>,
    downloadStates: Map<Int, Int>,
    onMagazineClick: (Magazine) -> Unit,
    onDownloadClick: (Magazine) -> Unit,
    onReadClick: (Magazine) -> Unit,
    onFavoriteClick: (Magazine) -> Unit,
    modifier: Modifier = Modifier
) {
    // If search is active, show number of results
    val isSearchActive = downloadStates.isEmpty() && magazines.isNotEmpty()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(magazines) { magazine ->
            MagazineCard(
                magazine = magazine,
                downloadProgress = downloadStates[magazine.id] ?: magazine.downloadProgress,
                onClick = { onReadClick(magazine) },
                onDownloadClick = { onDownloadClick(magazine) },
                onFavoriteClick = { onFavoriteClick(magazine) }
            )
        }
    }
}

@Composable
fun ShimmerLoadingGrid(
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show 6 shimmer cards (3 rows of 2)
        items(6) { index ->
            ShimmerMagazineCard()
        }
    }
}
