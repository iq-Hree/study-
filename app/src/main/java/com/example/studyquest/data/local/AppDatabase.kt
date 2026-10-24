package com.example.studyquest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studyquest.data.model.*

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        SubtopicEntity::class,
        StudySessionEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        PdfDocumentEntity::class,
        PdfHighlightEntity::class,
        PdfBookmarkEntity::class,
        UserProfileEntity::class,
        ScheduledSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyQuestDao(): StudyQuestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyquest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
