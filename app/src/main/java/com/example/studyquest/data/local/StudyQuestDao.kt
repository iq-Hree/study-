package com.example.studyquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.studyquest.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyQuestDao {
    // Subjects
    @Query("SELECT * FROM subjects ORDER BY priority DESC, createdAt ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    // Chapters
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY sortOrder ASC, id ASC")
    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY sortOrder ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    // Topics
    @Query("SELECT * FROM topics ORDER BY sortOrder ASC, id ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY sortOrder ASC")
    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY sortOrder ASC")
    fun getTopicsForChapter(chapterId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    // Subtopics
    @Query("SELECT * FROM subtopics WHERE topicId = :topicId ORDER BY sortOrder ASC")
    fun getSubtopicsForTopic(topicId: Long): Flow<List<SubtopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopic(subtopic: SubtopicEntity): Long

    @Update
    suspend fun updateSubtopic(subtopic: SubtopicEntity)

    @Delete
    suspend fun deleteSubtopic(subtopic: SubtopicEntity)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE startTime >= :startTime ORDER BY startTime DESC")
    fun getStudySessionsSince(startTime: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    // Habits
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    // Habit Logs
    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY timestamp DESC")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteHabitLog(habitId: Long, dateString: String)

    // PDF Documents
    @Query("SELECT * FROM pdf_documents ORDER BY lastOpenedAt DESC")
    fun getAllPdfDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getPdfDocumentById(id: Long): PdfDocumentEntity?

    @Query("SELECT * FROM pdf_documents WHERE subjectId = :subjectId")
    fun getPdfsForSubject(subjectId: Long): Flow<List<PdfDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfDocument(pdf: PdfDocumentEntity): Long

    @Update
    suspend fun updatePdfDocument(pdf: PdfDocumentEntity)

    @Delete
    suspend fun deletePdfDocument(pdf: PdfDocumentEntity)

    // PDF Highlights
    @Query("SELECT * FROM pdf_highlights WHERE pdfId = :pdfId ORDER BY pageNumber ASC, createdAt ASC")
    fun getHighlightsForPdf(pdfId: Long): Flow<List<PdfHighlightEntity>>

    @Query("SELECT * FROM pdf_highlights ORDER BY createdAt DESC")
    fun getAllHighlights(): Flow<List<PdfHighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: PdfHighlightEntity): Long

    @Update
    suspend fun updateHighlight(highlight: PdfHighlightEntity)

    @Delete
    suspend fun deleteHighlight(highlight: PdfHighlightEntity)

    // PDF Bookmarks
    @Query("SELECT * FROM pdf_bookmarks WHERE pdfId = :pdfId ORDER BY pageNumber ASC")
    fun getBookmarksForPdf(pdfId: Long): Flow<List<PdfBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: PdfBookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: PdfBookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: PdfBookmarkEntity)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    // Scheduled Sessions (Routine)
    @Query("SELECT * FROM scheduled_sessions ORDER BY dayOfWeek ASC, startHour ASC, startMinute ASC")
    fun getAllScheduledSessions(): Flow<List<ScheduledSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledSession(session: ScheduledSessionEntity): Long

    @Update
    suspend fun updateScheduledSession(session: ScheduledSessionEntity)

    @Query("DELETE FROM scheduled_sessions")
    suspend fun clearScheduledSessions()

    @Query("DELETE FROM scheduled_sessions WHERE id = :id")
    suspend fun deleteScheduledSessionById(id: Long)
}
