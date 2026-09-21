package com.example.zivaministries.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.compose.ui.platform.LocalContext
import com.example.zivaministries.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.example.zivaministries.ui.viewmodels.AuthViewModel

@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by authViewModel.currentUser.collectAsState()
    val linkedProviders by authViewModel.linkedProviders.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.refreshLinkedProviders()
    }

    fun connectGoogle() {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(
                        context.getString(R.string.default_web_client_id)
                    )
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context,
                    request
                )

                val credential = GoogleIdTokenCredential.createFrom(
                    result.credential.data
                )

                authViewModel.linkGoogleAccount(
                    credential.idToken
                )
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message ?: "Failed to connect Google",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        currentUser?.let { user ->

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Text(
            text = "Sign-in methods",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Email
        ProviderCard(
            title = "Email & Password",
            connected = "password" in linkedProviders
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Google
        ProviderCard(
            title = "Google",
            connected = "google.com" in linkedProviders,
            onConnect = ::connectGoogle
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        TextButton(
            onClick = onBack
        ) {
            Text("Back")
        }
    }
}
@Composable
private fun ProviderCard(
    title: String,
    connected: Boolean,
    onConnect: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (connected) {
                        "Connected"
                    } else {
                        "Not connected"
                    },
                    color = if (connected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (!connected && onConnect != null) {
                TextButton(
                    onClick = onConnect
                ) {
                    Text("Connect")
                }
            }
        }
    }
}