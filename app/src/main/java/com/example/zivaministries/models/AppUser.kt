package com.example.zivaministries.models

data class AppUser(
    val uid: String,
    val name: String,
    val email: String,
    val joinedDate: String,
    val totalPagesRead: Int = 0,
    val totalMagazinesDownloaded: Int = 0,
    val profileImageUrl: String? = null
) {
    constructor() : this("", "", "", "", 0, 0)
}

data class UserProfile(
    val name: String,
    val email: String,
    val joinedDate: String,
    val totalPagesRead: Int = 0,
    val totalMagazinesDownloaded: Int = 0
) {
    constructor() : this("", "", "", 0, 0)
}