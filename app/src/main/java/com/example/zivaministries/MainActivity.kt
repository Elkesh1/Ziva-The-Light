package com.example.zivaministries

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zivaministries.models.Magazine
import com.example.zivaministries.models.Video
import com.example.zivaministries.ui.screens.*
import com.example.zivaministries.ui.theme.ZivaMinistriesTheme
import com.example.zivaministries.ui.viewmodels.AuthViewModel
import com.example.zivaministries.ui.viewmodels.MainViewModel
import com.example.zivaministries.ui.viewmodels.VideoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            ZivaMinistriesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZivaApp()
                }
            }
        }
    }
}

@Composable
fun ZivaApp() {
    var darkTheme by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val videoViewModel: VideoViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // Track selected items
    var selectedMagazine by remember { mutableStateOf<Magazine?>(null) }
    var selectedVideo by remember { mutableStateOf<Video?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val authState by authViewModel.authState.collectAsState()
    val isAuthenticated = authState is AuthViewModel.AuthState.Authenticated

    if (authState is AuthViewModel.AuthState.Loading) {
        return
    }

    ZivaMinistriesTheme(darkTheme = darkTheme) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {

                if (isAuthenticated) {
                    NavigationBar {
                        val tabs = listOf(
                            "Home" to Icons.Default.Home,
                            "Library" to Icons.Default.Book,
                            "Favorites" to Icons.Default.Favorite,
                            "Videos" to Icons.Default.VideoLibrary,
                            "Profile" to Icons.Default.Person
                        )

                        tabs.forEachIndexed { index, (label, icon) ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = {
                                    selectedTab = index
                                    when (index) {
                                        0 -> navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                        1 -> navController.navigate("library") {
                                            popUpTo("library") { inclusive = true }
                                        }
                                        2 -> navController.navigate("favorites") {
                                            popUpTo("favorites") { inclusive = true }
                                        }
                                        3 -> navController.navigate("videos") {
                                            popUpTo("videos") { inclusive = true }
                                        }
                                        4 -> navController.navigate("profile") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    }
                                },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = if (isAuthenticated) {
                    "home"
                } else {
                    "login"
                },
                modifier = Modifier.padding(paddingValues)
            ) {
                // ===== AUTH SCREENS =====
                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onNavigateToSignUp = {
                            navController.navigate("signup")
                        }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        authViewModel = authViewModel,
                        onNavigateToLogin = {
                            navController.navigate("login")
                        }
                    )
                }

                // ===== MAIN APP SCREENS =====
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        videoViewModel = videoViewModel,

                        onMagazineClick = { magazine ->
                            if (magazine.isDownloaded) {
                                selectedMagazine = magazine
                                navController.navigate("reader")
                            }
                        },
                        onSettingsClick = { navController.navigate("settings") },
                        onAboutClick = { navController.navigate("about") },
                        onContactClick = { navController.navigate("contact") },
                        onDownloadClick = { magazine ->
                            viewModel.downloadMagazine(magazine)
                        },
                        onVideoClick = { video ->
                            selectedVideo = video
                            navController.navigate("video_player")
                        },
                    )
                }

                composable("library") {
                    LibraryScreen(
                        viewModel = viewModel,
                        onMagazineClick = { /* Optional */ },
                        onDownloadClick = { magazine ->
                            viewModel.downloadMagazine(magazine)
                        },
                        onReadClick = { magazine ->
                            if (magazine.isDownloaded) {
                                selectedMagazine = magazine
                                navController.navigate("reader")
                            }
                        }
                    )
                }

                composable("favorites") {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onMagazineClick = { /* Optional */ },
                        onDownloadClick = { magazine ->
                            viewModel.downloadMagazine(magazine)
                        },
                        onReadClick = { magazine ->
                            if (magazine.isDownloaded) {
                                selectedMagazine = magazine
                                navController.navigate("reader")
                            }
                        }
                    )
                }

                composable("videos") {
                    VideoScreen(
                        viewModel = videoViewModel,
                        onVideoClick = { video ->
                            selectedVideo = video
                            navController.navigate("video_player")
                        },
                        onDownloadClick = { video ->
                            videoViewModel.downloadVideo(video)
                        },
                        onFavoriteClick = { video ->
                            videoViewModel.toggleFavorite(video)
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onSignOut = {
                            authViewModel.signOut()
                            selectedMagazine = null
                            selectedVideo = null
                            selectedTab = 0

                        }
                    )
                }

                // ===== DETAIL SCREENS =====
                composable("video_player") {
                    selectedVideo?.let { video ->
                        VideoPlayerScreen(
                            video = video,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("reader") {
                    selectedMagazine?.let { magazine ->
                        ReaderScreen(
                            magazine = magazine,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("settings") {
                    SettingsScreen(
                        darkTheme = darkTheme,
                        onDarkThemeChange = { darkTheme = it },
                        authViewModel = authViewModel
                    )
                }

                composable("about") {
                    AboutScreen(onBack = { navController.popBackStack() })
                }

                composable("contact") {
                    ContactScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}