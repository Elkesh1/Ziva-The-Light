package com.example.zivaministries.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.example.zivaministries.models.AppUser
import android.content.Context
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthRepository(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthRepository"

    suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): Result<AppUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")

            val currentUser = AppUser(
                uid = user.uid,
                name = name,
                email = user.email ?: email,
                joinedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            )

            firestore.collection("users")
                .document(user.uid)
                .set(currentUser)
                .await()

            Log.d(TAG, "User created successfully: ${user.uid}")
            Result.success(currentUser)

        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account with this email already exists."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email address."))
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error: ${e.message}", e)
            Result.failure(Exception("Sign up failed: ${e.message}"))
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<AppUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")

            Log.d(TAG, "Auth successful for user: ${user.uid}")

            try {
                val document = firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .await()

                val appUser = document.toObject(AppUser::class.java)
                if (appUser != null) {
                    Log.d(TAG, "User data fetched from Firestore")
                    return Result.success(appUser)
                } else {
                    val fallbackUser = AppUser(
                        uid = user.uid,
                        name = user.displayName ?: "User",
                        email = user.email ?: email,
                        joinedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                    )
                    firestore.collection("users")
                        .document(user.uid)
                        .set(fallbackUser)
                        .await()
                    Log.d(TAG, "Fallback user created")
                    return Result.success(fallbackUser)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore read failed, but auth succeeded: ${e.message}")
                val basicUser = AppUser(
                    uid = user.uid,
                    name = user.displayName ?: "User",
                    email = user.email ?: email,
                    joinedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                )
                Result.success(basicUser)
            }

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email or password."))
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            if (e.message?.contains("password") == true) {
                Result.failure(Exception("Invalid email or password."))
            } else if (e.message?.contains("user") == true) {
                Result.failure(Exception("Account not found. Please sign up first."))
            } else {
                Result.failure(Exception("Login failed: ${e.message}"))
            }
        }
    }

    suspend fun loginWithGoogle(
        idToken: String
    ): Result<AppUser> {
        return try {

            val credential = GoogleAuthProvider.getCredential(
                idToken,
                null
            )

            val result = auth
                .signInWithCredential(credential)
                .await()

            val firebaseUser = result.user
                ?: throw Exception("Google Login failed")

            Log.d(
                TAG,
                "Google authentication successful: ${firebaseUser.uid}"
            )

            val userDocument = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val existingUser =
                userDocument.toObject(AppUser::class.java)

            if (existingUser != null) {

                Log.d(
                    TAG,
                    "Existing Ziva user found"
                )
                Result.success(existingUser)

            } else {

                val newUser = AppUser(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "User",
                    email =firebaseUser.email ?: "",
                    joinedDate = SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.getDefault()
                    ).format(Date())
                )

                firestore
                    .collection("users")
                    .document(firebaseUser.uid)
                    .set(newUser)
                    .await()

                Log.d(
                    TAG,
                    "New Google user created in Firestore"
                )

                Result.success(newUser)
            }
        } catch (e: Exception) {

            Log.e(
                TAG,
                "Google login failed",
                e
            )

            Result.failure(
                Exception(
                    "Google sign-in failed: ${e.message}"
                )
            )
        }
    }

    suspend fun linkGoogleAccount(
        idToken: String
    ): Result<AppUser> {
        return try {

            val firebaseUser = auth.currentUser
                ?: throw Exception("No authenticated user")

            val credential = GoogleAuthProvider.getCredential(
                idToken,
                null
            )

            val result = firebaseUser
                .linkWithCredential(credential)
                .await()

            val linkedUser = result.user
                ?: throw Exception("Failed to link Google account")

            Log.d(
                TAG,
                "Google account linked successfully: ${linkedUser.uid}"
            )

            getUserProfile(linkedUser.uid)

        } catch (e: FirebaseAuthUserCollisionException) {

            Log.e(
                TAG,
                "Google account is already linked to another account",
                e
            )

            Result.failure(
                Exception(
                    "This Google account is already connected to another Ziva account."
                )
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to link Google account",
                e
            )

            Result.failure(
                Exception(
                    "Failed to connect Google account: ${e.message}"
                )
            )
        }
    }

    suspend fun linkEmailPassword(
        email: String,
        password: String
    ): Result<AppUser> {
        return try {

            val firebaseUser = auth.currentUser
                ?: throw Exception("No authenticated user")

            val credential =
                EmailAuthProvider.getCredential(
                    email,
                    password
                )

            val result = firebaseUser
                .linkWithCredential(credential)
                .await()

            val linkedUser = result.user
                ?: throw Exception("Failed to link email account")

            Log.d(
                TAG,
                "Email/password linked successfully: ${linkedUser.uid}"
            )

            getUserProfile(linkedUser.uid)

        } catch (e: FirebaseAuthUserCollisionException) {

            Log.e(
                TAG,
                "Email already belongs to another account",
                e
            )

            Result.failure(
                Exception(
                    "This email is already connected to another account."
                )
            )

        } catch (e: FirebaseAuthWeakPasswordException) {

            Result.failure(
                Exception(
                    "Password is too weak. Please use at least 6 characters."
                )
            )

        } catch (e: FirebaseAuthInvalidCredentialsException) {

            Result.failure(
                Exception(
                    "Invalid email address."
                )
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to link email/password",
                e
            )

            Result.failure(
                Exception(
                    "Failed to add email/password: ${e.message}"
                )
            )
        }
    }

    fun getLinkedProviders(): Set<String> {
        val user = auth.currentUser
            ?: return emptySet()

        return user.providerData
            .mapNotNull { it.providerId }
            .filter { it != "firebase" }
            .toSet()
    }
    suspend fun saveProfileImageLocally(
        uid: String,
        imageUri: Uri
    ): Result<String> {
        return try {

            // Create a folder specifically for profile pictures
            val profileImagesDir = File(
                context.filesDir,
                "profile_images"
            )

            if (!profileImagesDir.exists()) {
                profileImagesDir.mkdirs()
            }

            // Each user gets their own folder
            val userDirectory = File(
                profileImagesDir,
                uid
            )

            if (!userDirectory.exists()) {
                userDirectory.mkdirs()
            }

            // Always use the same filename.
            // This means a new profile picture replaces the old one.
            val profileImageFile = File(
                userDirectory,
                "profile.jpg"
            )

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Unable to open selected image")

            inputStream.use { input ->
                profileImageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(
                TAG,
                "Profile image saved locally: ${profileImageFile.absolutePath}"
            )

            Result.success(profileImageFile.absolutePath)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile image locally", e)
            Result.failure(e)
        }
    }

    fun getLocalProfileImage(uid: String): String? {

        val profileImageFile = File(
            context.filesDir,
            "profile_images/$uid/profile.jpg"
        )

        return if (profileImageFile.exists()) {
            profileImageFile.absolutePath
        } else {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): Result<AppUser> {
        val user = auth.currentUser
            ?: return Result.failure(
                Exception("No authenticated user")
            )

        return try {
            val document = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            val appUser = document.toObject(AppUser::class.java)

            if (appUser != null) {
                Result.success(appUser)
            } else {
                Result.failure(
                    Exception("User profile not found")
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Get current user error", e)
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun getUserProfile(uid: String): Result<AppUser> {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val user = document.toObject(AppUser::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        uid: String,
        data: Map<String, Any>): Result<Boolean> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update(data)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}