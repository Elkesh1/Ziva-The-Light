package com.example.zivaministries.database

import androidx.room.*
import com.example.zivaministries.models.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("""
        SELECT * FROM videos
        WHERE userId = :userId
        ORDER BY id DESC
    """)
    fun getAllVideos(userId: String): Flow<List<Video>>

    @Query("""
        SELECT * FROM videos
        WHERE userId = :userId
    """)
    suspend fun getAllVideosOnce(userId: String): List<Video>

    @Query("""
        SELECT * FROM videos
        WHERE userId = :userId
        AND id = :id
    """)
    suspend fun getVideoById(
        userId: String,
        id: Int
    ): Video?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<Video>)

    @Update
    suspend fun updateVideo(video: Video)

    @Query("""
        UPDATE videos
        SET userId = :userId
        WHERE userId = ''
    """)
    suspend fun adoptLegacyData(userId: String)
}