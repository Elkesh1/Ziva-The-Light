package com.example.zivaministries.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.zivaministries.models.Magazine
import com.example.zivaministries.models.Video

@Database(
    entities = [Magazine::class, Video::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MagazineDatabase : RoomDatabase() {

    abstract fun magazineDao(): MagazineDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: MagazineDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS videos (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        speaker TEXT NOT NULL,
                        duration TEXT NOT NULL,
                        thumbnailUrl TEXT NOT NULL,
                        videoUrl TEXT NOT NULL,
                        fileSize TEXT NOT NULL,
                        isDownloaded INTEGER NOT NULL DEFAULT 0,
                        localFilePath TEXT,
                        downloadProgress INTEGER NOT NULL DEFAULT 0,
                        isFavorite INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    ALTER TABLE magazines
                    ADD COLUMN pageCount INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }
        fun getInstance(context: Context): MagazineDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MagazineDatabase::class.java,
                    "ziva_database"
                )
                    .addMigrations(
                        MIGRATION_4_5,
                        MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}