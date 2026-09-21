package com.example.zivaministries.database

import androidx.room.*
import com.example.zivaministries.models.Magazine
import kotlinx.coroutines.flow.Flow

@Dao
interface MagazineDao {

    @Query("""
        SELECT * FROM magazines
        WHERE userId = :userId
        ORDER BY id DESC
    """)
    fun getAllMagazines(userId: String): Flow<List<Magazine>>

    @Query("""
        SELECT * FROM magazines
        WHERE userId = :userId
    """)
    suspend fun getAllMagazinesOnce(userId: String): List<Magazine>

    @Query("""
        SELECT * FROM magazines
        WHERE userId = :userId
        AND id = :id
    """)
    suspend fun getMagazineById(
        userId: String,
        id: Int
    ): Magazine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(magazines: List<Magazine>)

    @Update
    suspend fun updateMagazine(magazine: Magazine)

    @Query("""
        UPDATE magazines
        SET lastPage = :lastPage
        WHERE userId = :userId
        AND id = :magazineId
    """)
    suspend fun updateLastPage(
        userId: String,
        magazineId: Int,
        lastPage: Int
    )

    @Query("""
        UPDATE magazines
        SET pageCount = :pageCount
        WHERE userId = :userId
        AND id = :magazineId
    """)
    suspend fun updatePageCount(
        userId: String,
        magazineId: Int,
        pageCount: Int
    )

    @Query("""
        UPDATE magazines
        SET userId = :userId
        WHERE userId = ''
    """)
    suspend fun adoptLegacyData(userId: String)
}