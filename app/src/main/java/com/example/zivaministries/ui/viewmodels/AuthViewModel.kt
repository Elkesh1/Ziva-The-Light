package com.example.zivaministries.ui.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zivaministries.models.AppUser
import com.example.zivaministries.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val TAG = "AuthViewModel"

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploadingProfileImage = MutableStateFlow(false)
    val isUploadingProfileImage: StateFlow<Boolean> = _isUploadingProfileImage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _linkedProviders =
        MutableStateFlow<Set<String>>(emptySet())

    val linkedProviders: StateFlow<Set<String>> =
        _linkedProviders.asStateFlow()

    sealed class AuthState {
        object Authenticated : AuthState()
        object Unauthenticated : AuthState()
        object Loading : AuthState()
    }

    init {
        checkCurrentUser()
    }

    fun refreshLinkedProviders() {
        _linkedProviders.value =
            authRepository.getLinkedProviders()
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.getCurrentUser()

            result.onSuccess { user ->
                val localImagePath = authRepository.getLocalProfileImage(user.uid)

                _currentUser.value = user.copy(
                    profileImageUrl = localImagePath
                )

                _authState.value = AuthState.Authenticated
                refreshLinkedProviders()

                Log.d(
                    TAG,
                    "Current user loaded: ${user.uid}"
                )

                Log.d(
                    TAG,
                    "Local profile image: $localImagePath"
                )
            }.onFailure { error ->
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated

                Log.d(TAG, "No current user: ${error.message}")
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signUp(email, password, name)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
                Log.d(TAG, "Sign up successful: ${user.uid}")
            }.onFailure { error ->
                _errorMessage.value = error.message
                _authState.value = AuthState.Unauthenticated
                Log.e(TAG, "Sign up failed: ${error.message}")
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated

                refreshLinkedProviders()
                Log.d(TAG, "Login successful: ${user.uid}")
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Login failed. Please try again."
                _authState.value = AuthState.Unauthenticated
                Log.e(TAG, "Login failed: ${error.message}")
            }
            _isLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.loginWithGoogle(idToken)

            result.onSuccess { user ->

                _currentUser.value = user
                _authState.value = AuthState.Authenticated

                refreshLinkedProviders()

                Log.d(
                    TAG,
                    "Google login successful: ${user.uid}"
                )

            }.onFailure { error ->

                _errorMessage.value =
                    error.message ?: "Google sign-in failed"

                _authState.value =
                    AuthState.Unauthenticated

                Log.e(
                    TAG,
                    "Google login failed",
                    error
                )
            }

            _isLoading.value = false
        }
    }

    fun linkGoogleAccount(idToken: String) {
        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.linkGoogleAccount(idToken)

            result.onSuccess { user ->

                val localImagePath =
                    authRepository.getLocalProfileImage(user.uid)

                _currentUser.value = user.copy(
                    profileImageUrl = localImagePath
                )

                _authState.value =
                    AuthState.Authenticated

                refreshLinkedProviders()

                Log.d(
                    TAG,
                    "Google account linked: ${user.uid}"
                )

            }.onFailure { error ->

                _errorMessage.value =
                    error.message
                        ?: "Failed to connect Google account"

                Log.e(
                    TAG,
                    "Google account linking failed",
                    error
                )
            }

            _isLoading.value = false
        }
    }

    fun linkEmailPassword(
        email: String,
        password: String
    ) {
        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.linkEmailPassword(
                email = email.trim(),
                password = password
            )

            result.onSuccess { user ->

                val localImagePath =
                    authRepository.getLocalProfileImage(user.uid)

                _currentUser.value = user.copy(
                    profileImageUrl = localImagePath
                )

                _authState.value =
                    AuthState.Authenticated

                refreshLinkedProviders()

                Log.d(
                    TAG,
                    "Email/password linked: ${user.uid}"
                )

            }.onFailure { error ->

                _errorMessage.value =
                    error.message
                        ?: "Failed to add email/password"

                Log.e(
                    TAG,
                    "Email/password linking failed",
                    error
                )
            }

            _isLoading.value = false
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {

            val user = currentUser.value
                ?: return@launch

            _isUploadingProfileImage.value = true

            val result = authRepository.saveProfileImageLocally(
                uid = user.uid,
                imageUri = imageUri
            )

            result.onSuccess { localImagePath ->
                _currentUser.value = user.copy(
                    profileImageUrl = localImagePath
                )

                Log.d(
                    TAG,
                    "Profile image saved: $localImagePath"
                )

            }.onFailure { error ->

                _errorMessage.value =
                    error.message ?: "Failed to save profile image"

                Log.e(
                    TAG,
                    "Profile image save failed",
                    error
                )
            }

            _isUploadingProfileImage.value = false
        }
    }

    fun isGoogleLinked(): Boolean {
        return "google.com" in _linkedProviders.value
    }

    fun isEmailLinked(): Boolean {
        return "password" in _linkedProviders.value
    }

    fun signOut() {
        authRepository.signOut()

        _currentUser.value = null
        _linkedProviders.value = emptySet()

        _authState.value = AuthState.Unauthenticated

        Log.d(TAG, "Signed out")
    }

    fun clearError() {
        _errorMessage.value = null
    }
}