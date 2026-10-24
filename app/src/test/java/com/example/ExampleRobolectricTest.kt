package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.studyquest.data.local.AppDatabase
import com.example.studyquest.data.model.*
import com.example.studyquest.data.repository.StudyQuestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StudyQuest", appName)
    }

    @Test
    fun testRepositoryAndXpProgression() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = AppDatabase.getDatabase(context)
        val dao = database.studyQuestDao()
        val repo = StudyQuestRepository(dao, context)

        // Seed initial profile
        val profileBefore = repo.userProfile.first()
        val initialXp = profileBefore?.totalXp ?: 0

        // Test Subject insertion
        val subjectId = repo.addSubject(
            name = "Bioscience: Molecular Genetics",
            colorHex = "#7E57C2",
            iconName = "biotech",
            priority = Priority.HIGH,
            notes = "Focus on DNA replication enzymes and helicase."
        )
        assertTrue(subjectId > 0)

        val subjects = repo.allSubjects.first()
        assertTrue(subjects.any { it.name.contains("Molecular Genetics") })

        // Test Chapter and Topic creation
        val chapterId = repo.addChapter(subjectId, "DNA Replication", "Double helix unwinding")
        val topicId = repo.addTopic(
            chapterId = chapterId,
            subjectId = subjectId,
            title = "Replication Fork Enzymology",
            description = "Helicase, topoisomerase, DNA Pol III",
            estimatedMinutes = 45,
            difficulty = Difficulty.HARD,
            priority = Priority.HIGH
        )
        assertTrue(topicId > 0)

        // Record a completed study session
        repo.recordStudySession(
            StudySessionEntity(
                subjectId = subjectId,
                chapterId = chapterId,
                topicId = topicId,
                topicTitle = "Replication Fork Enzymology",
                plannedMinutes = 45,
                actualSeconds = 2700,
                startTime = System.currentTimeMillis() - 2700000L,
                endTime = System.currentTimeMillis(),
                understandingRating = 5,
                keyTakeaways = "SSBs stabilize single strands to prevent secondary structures.",
                markedTopicCompleted = true,
                xpEarned = 35
            )
        )

        // Verify XP increase and topic completion
        val profileAfter = repo.userProfile.first()
        assertTrue("XP should increase after completing study session", (profileAfter?.totalXp ?: 0) > initialXp)

        val updatedTopic = repo.getTopicById(topicId)
        assertNotNull(updatedTopic)
        assertEquals(ItemStatus.COMPLETED, updatedTopic?.status)

        // Test Habit toggle
        val habitId = repo.addHabit("Daily 45m Deep Study", "book", "#81C784", 7)
        assertTrue(habitId > 0)
        repo.toggleHabitForDate(habitId, repo.getTodayDateString(), currentlyCompleted = false)

        val habitLogs = repo.getHabitLogsForToday().first()
        assertTrue(habitLogs.any { it.habitId == habitId })
    }
}
