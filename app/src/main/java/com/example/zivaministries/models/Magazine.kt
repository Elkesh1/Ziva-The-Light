package com.example.zivaministries.models

import androidx.room.Entity

@Entity(
    tableName = "magazines",
    primaryKeys = ["userId", "id"]
)
data class Magazine(
    val userId: String,
    val id: Int,
    val title: String,
    val issueDate: String,
    val coverUrl: String,
    val pdfUrl: String,
    val fileSize: String,

    var isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    var downloadProgress: Int = 0,
    var lastPage: Int = 0,
    val pageCount: Int = 0,
    var isFavorite: Boolean = false
)

data class RemoteMagazine(
    val id: Int,
    val title: String,
    val issueDate: String,
    val coverUrl: String,
    val pdfUrl: String,
    val fileSize: String
)

data class MagazineResponse(
    val magazines: List<RemoteMagazine>
)