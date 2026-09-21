package com.example.zivaministries.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.zivaministries.R
import com.example.zivaministries.ui.viewmodels.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by authViewModel.currentUser.collectAsState()
    val linkedProviders by authViewModel.linkedProviders.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    var showPasswordDialog by remember {
        mutableStateOf(false)
    }

    /*
     * Refresh linked authentication providers
     * whenever the Settings screen is opened.
     */
    LaunchedEffect(Unit) {
        authViewModel.refreshLinkedProviders()
    }

    /*
     * Connect the currently signed-in Firebase user
     * to a Google account.
     */
    fun connectGoogle() {
        coroutineScope.launch {
            try {
                val credentialManager =
                    CredentialManager.create(context)

                val googleIdOption =
                    GetGoogleIdOption.Builder()
                        .setServerClientId(
                            context.getString(
                                R.string.default_web_client_id
                            )
                        )
                        .setFilterByAuthorizedAccounts(false)
                        .build()

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                val result =
                    credentialManager.getCredential(
                        context,
                        request
                    )

                val credential =
                    GoogleIdTokenCredential.createFrom(
                        result.credential.data
                    )

                authViewModel.linkGoogleAccount(
                    credential.idToken
                )

                /*
                 * Give Firebase a moment to complete the
                 * linking operation before refreshing UI state.
                 */
                authViewModel.refreshLinkedProviders()

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message ?: "Google connection failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary,
                    titleContentColor =
                        MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp)
        ) {

            // =========================================
            // APPEARANCE
            // =========================================

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Dark Mode",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text =
                                "Switch to dark theme for night reading",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = darkTheme,
                        onCheckedChange =
                            onDarkThemeChange
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // =========================================
            // ACCOUNT
            // =========================================

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    currentUser?.let { user ->

                        Text(
                            text = user.name,
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = user.email,
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Sign-in methods",
                        style =
                            MaterialTheme.typography.titleSmall
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    // Email / Password
                    ProviderRow(
                        title = "Email & Password",
                        connected =
                            "password" in linkedProviders,
                        actionText =
                            if ("password" in linkedProviders) {
                                null
                            } else {
                                "Add"
                            },
                        enabled = !isLoading,
                        onAction = {
                            showPasswordDialog = true
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    // Google
                    ProviderRow(
                        title = "Google",
                        connected =
                            "google.com" in linkedProviders,
                        actionText =
                            if ("google.com" in linkedProviders) {
                                null
                            } else {
                                "Connect"
                            },
                        enabled = !isLoading,
                        onAction = {
                            connectGoogle()
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // =========================================
            // DOWNLOADS
            // =========================================

            Text(
                text = "Downloads",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Auto-Download",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text =
                                "Automatically download new issues",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // =========================================
            // STORAGE
            // =========================================

            Text(
                text = "Storage",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Storage",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "App storage usage: Coming soon",
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // =========================================
            // ABOUT
            // =========================================

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ziva Ministries",
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Version 1.0",
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "Faith-based magazines for your spiritual growth",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }

    // =============================================
    // ADD PASSWORD DIALOG
    // =============================================

    if (showPasswordDialog) {
        AddPasswordDialog(
            currentEmail = currentUser?.email ?: "",
            isLoading = isLoading,
            onDismiss = {
                if (!isLoading) {
                    showPasswordDialog = false
                }
            },
            onConfirm = { email, password ->

                authViewModel.linkEmailPassword(
                    email = email,
                    password = password
                )

                showPasswordDialog = false

                coroutineScope.launch {
                    kotlinx.coroutines.delay(500)
                    authViewModel.refreshLinkedProviders()
                }
            }
        )
    }
}


// =================================================
// PROVIDER ROW
// =================================================

@Composable
private fun ProviderRow(
    title: String,
    connected: Boolean,
    actionText: String?,
    enabled: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.bodyLarge
            )

            Text(
                text = if (connected) {
                    "Connected"
                } else {
                    "Not connected"
                },
                style =
                    MaterialTheme.typography.bodySmall,
                color = if (connected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
                }
            )
        }

        if (actionText != null) {
            TextButton(
                onClick = onAction,
                enabled = enabled
            ) {
                Text(actionText)
            }
        }
    }
}


// =================================================
// ADD PASSWORD DIALOG
// =================================================

@Composable
private fun AddPasswordDialog(
    currentEmail: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var email by remember {
        mutableStateOf(currentEmail)
    }

    var password by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },

        title = {
            Text("Add password")
        },

        text = {
            Column {

                Text(
                    text =
                        "Add email and password as another way to sign in to your Ziva account.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    label = {
                        Text("Email")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email
                    ),
                    enabled = !isLoading
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = {
                        Text("Password")
                    },
                    singleLine = true,
                    visualTransformation =
                        PasswordVisualTransformation(),
                    enabled = !isLoading
                )

                if (
                    password.isNotEmpty() &&
                    password.length < 6
                ) {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "Password must be at least 6 characters.",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        email.trim(),
                        password
                    )
                },
                enabled =
                    !isLoading &&
                            email.isNotBlank() &&
                            password.length >= 6
            ) {
                Text("Add")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}