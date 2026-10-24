package com.example.studyquest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { LOW, MEDIUM, HIGH }
enum class ItemStatus { NOT_STARTED, IN_PROGRESS, COMPLETED, NEEDS_REVISION }
enum class Difficulty { EASY, MEDIUM, HARD }
enum class CompanionType { CAT, BUNNY, FROG }

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#7E57C2",
    val iconName: String = "science",
    val examDate: Long? = null,
    val priority: Priority = Priority.HIGH,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val description: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val subjectId: Long,
    val title: String,
    val description: String = "",
    val estimatedMinutes: Int = 45,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val priority: Priority = Priority.MEDIUM,
    val status: ItemStatus = ItemStatus.NOT_STARTED,
    val deadline: Long? = null,
    val nextRevisionDate: Long? = null,
    val notes: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "subtopics")
data class SubtopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val chapterId: Long?,
    val topicId: Long?,
    val topicTitle: String,
    val plannedMinutes: Int,
    val actualSeconds: Long,
    val startTime: Long,
    val endTime: Long,
    val understandingRating: Int = 4, // 1 to 5
    val keyTakeaways: String = "",
    val personalNotes: String = "",
    val markedNeedsRevision: Boolean = false,
    val markedTopicCompleted: Boolean = false,
    val associatedPdfTitle: String? = null,
    val xpEarned: Int = 25
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String = "book",
    val colorHex: String = "#81C784",
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateString: String, // YYYY-MM-DD
    val completed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val originalFileName: String,
    val localFilePath: String,
    val fileSizeBytes: Long = 0,
    val pageCount: Int = 1,
    val lastReadPage: Int = 1,
    val readingProgressPercent: Int = 0,
    val subjectId: Long? = null,
    val topicId: Long? = null,
    val isAvailableOffline: Boolean = true,
    val addedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val description: String = ""
)

@Entity(tableName = "pdf_highlights")
data class PdfHighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pdfId: Long,
    val pageNumber: Int,
    val colorHex: String = "#FFF176", // yellow, pink, sage, blue
    val selectedText: String = "",
    val note: String = "",
    val normX: Float = 0.1f, // Normalized coordinates (0.0 to 1.0)
    val normY: Float = 0.1f,
    val normWidth: Float = 0.8f,
    val normHeight: Float = 0.05f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pdf_bookmarks")
data class PdfBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pdfId: Long,
    val pageNumber: Int,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Scholar",
    val companion: CompanionType = CompanionType.CAT,
    val totalXp: Int = 50,
    val level: Int = 1,
    val dailyStudyTargetMinutes: Int = 120,
    val preferredSessionMinutes: Int = 45,
    val breakMinutes: Int = 10,
    val availableHoursPerDay: String = "2.0,2.0,2.5,2.0,2.5,4.0,3.0" // Mon-Sun
)

@Entity(tableName = "scheduled_sessions")
data class ScheduledSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val subjectId: Long,
    val dayOfWeek: Int, // 1 = Monday ... 7 = Sunday
    val startHour: Int = 16,
    val startMinute: Int = 0,
    val durationMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false
)
