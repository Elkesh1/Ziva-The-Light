package com.example.zivaministries.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zivaministries.ui.viewmodels.AuthViewModel
import com.example.zivaministries.ui.viewmodels.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {

    val isUploadingProfileImage by authViewModel.isUploadingProfileImage.collectAsState()
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var showProfilePictureDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val magazines by mainViewModel.magazines.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    val photoFile = remember {
        File.createTempFile(
            "profile_image",
            ".jpg",
            context.cacheDir
        )
    }

    val photoUri = remember(photoFile) {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { result ->
        if (result != null) {
            selectedImage = result
            authViewModel.uploadProfileImage(result)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImage = photoUri
            authViewModel.uploadProfileImage(photoUri)
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Unauthenticated) {
            onSignOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(
                        enabled = !isUploadingProfileImage
                    ) {
                        showProfilePictureDialog = true
                      },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingProfileImage) {
                        CircularProgressIndicator()
                    } else if (selectedImage != null) {
                        AsyncImage(
                            model = selectedImage,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (currentUser?.profileImageUrl != null) {
                        AsyncImage(
                            model = currentUser?.profileImageUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showProfilePictureDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showProfilePictureDialog = false
                    },
                    title = {
                        Text("Change Profile Picture")
                    },
                    text = {
                        Text("Choose an option")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showProfilePictureDialog = false
                                cameraLauncher.launch(
                                    photoUri
                                )
                            }
                        ) {
                            Text("Take Photo")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showProfilePictureDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        ) {
                            Text("Choose from device")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            currentUser?.let { user ->
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Joined ${user.joinedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = magazines.count { it.isDownloaded }.toString(),
                        label = "Downloaded"
                    )
                    StatItem(
                        value = magazines.count { it.lastPage > 0 }.toString(),
                        label = "Reading"
                    )
                    StatItem(
                        value = magazines.size.toString(),
                        label = "Magazines"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out Button
            Button(
                onClick = {
                    showSignOutDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign Out"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }

            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSignOutDialog = false
                    },
                    title = {
                        Text("Sign Out")
                    },
                    text = {
                        Text("Are you sure you want to sign out?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSignOutDialog = false
                                authViewModel.signOut()
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showSignOutDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}