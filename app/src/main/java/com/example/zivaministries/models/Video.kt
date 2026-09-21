package com.example.zivaministries.models

import androidx.room.Entity

@Entity(
    tableName = "videos",
    primaryKeys = ["userId", "id"]
)
data class Video(
    val userId: String,
    val id: Int,
    val title: String,
    val description: String,
    val speaker: String,
    val duration: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val fileSize: String,

    var isDownloaded: Boolean = false,
    var localFilePath: String? = null,
    var downloadProgress: Int = 0,
    var isFavorite: Boolean = false
)

data class RemoteVideo(
    val id: Int,
    val title: String,
    val description: String,
    val speaker: String,
    val duration: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val fileSize: String
)

data class VideoResponse(
    val videos: List<RemoteVideo>
)