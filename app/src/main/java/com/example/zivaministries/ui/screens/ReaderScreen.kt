package com.example.zivaministries.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zivaministries.models.Magazine
import com.example.zivaministries.ui.theme.ZivaMinistriesTheme
import com.example.zivaministries.ui.viewmodels.MainViewModel
import com.example.zivaministries.utils.PreferencesManager
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    magazine: Magazine,
    viewModel: MainViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pdfFile = File(magazine.localFilePath ?: "")
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var totalPages by remember { mutableStateOf(0) }

    // Create PreferencesManager instance
    val prefsManager = remember { PreferencesManager(context) }

    // Load saved page from Preferences
    val savedPage = remember {
        if (magazine.lastPage > 0)magazine.lastPage else prefsManager.getLastPage(magazine.id) }
    var currentPage by remember { mutableStateOf(savedPage) }

    // Share dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Magazine") },
            text = { Text("Share this magazine with your friends and family.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        shareMagazine(context, magazine)
                        showShareDialog = false
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Magazine") },
            text = { Text("Are you sure you want to delete \"${magazine.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefsManager.clearLastPage(magazine.id)
                        viewModel.deleteMagazine(magazine)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(magazine.title)
                        Text(
                            text = "Page ${currentPage + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                        },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share button
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pdfFile.exists()) {
                PDFViewer(
                    pdfPath = pdfFile.absolutePath,
                    initialPage = savedPage,

                    onPageChange = { pageNumber ->
                        // Save the page number whenever it changes
                        currentPage = pageNumber

                        prefsManager.saveLastPage(
                            magazine.id,
                            pageNumber
                        )

                        // save to database
                        viewModel.updateLastPage(
                            magazine.id,
                            pageNumber
                        )
                    },

                    onPageCount = { pageCount ->

                        viewModel.updatePageCount(
                            magazine.id,
                            pageCount
                        )
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("PDF file not found")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

// Share function
fun shareMagazine(context: Context, magazine: Magazine) {
    val shareMessage = """
        📖 ${'$'}{magazine.title}
                📅 ${'$'}{magazine.issueDate}
                
                Download the Ziva Ministries app to read this magazine and more!
                https://zivaministries.com/app
    """.trimIndent()

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareMessage)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}
@Composable
fun PDFViewer(
    pdfPath: String,
    initialPage: Int = 0,
    onPageChange: (Int) -> Unit,
    onPageCount: (Int) -> Unit
) {

    AndroidView(
        factory = { context ->

            PDFView(context, null).apply {

                fromFile(File(pdfPath))
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .defaultPage(initialPage)
                    .enableAnnotationRendering(true)
                    .enableAntialiasing(true)

                    .onPageChange { page, pageCount ->

                        onPageChange(page)

                        if (pageCount > 0) {
                            onPageCount(pageCount)
                        }
                    }

                    .load()
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { pDFView -> }
    )
}