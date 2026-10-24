package com.example.studyquest.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyquest.data.local.AppDatabase
import com.example.studyquest.data.model.*
import com.example.studyquest.data.repository.StudyQuestRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class AppDestination {
    HOME,
    SYLLABUS,
    STUDY_TIMER,
    LIBRARY,
    HABITS,
    PROGRESS,
    ROUTINE,
    SETTINGS,
    PDF_READER
}

data class TimerUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0,
    val plannedMinutes: Int = 45,
    val topic: TopicEntity? = null,
    val associatedPdf: PdfDocumentEntity? = null,
    val showFinishDialog: Boolean = false
)

class StudyQuestViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = StudyQuestRepository(database.studyQuestDao(), application)

    // Navigation state
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Active PDF reading state
    private val _activePdfId = MutableStateFlow<Long?>(null)
    val activePdfId: StateFlow<Long?> = _activePdfId.asStateFlow()

    // Study Timer State
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // Search and filter states
    val syllabusSearchQuery = MutableStateFlow("")
    val librarySearchQuery = MutableStateFlow("")
    val selectedSubjectFilter = MutableStateFlow<Long?>(null)

    // Database reactive streams
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getHabitLogsForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitLogs: StateFlow<List<HabitLogEntity>> = repository.allHabitLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pdfDocuments: StateFlow<List<PdfDocumentEntity>> = repository.allPdfDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHighlights: StateFlow<List<PdfHighlightEntity>> = repository.allHighlights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .map { it ?: UserProfileEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    val scheduledSessions: StateFlow<List<ScheduledSessionEntity>> = repository.scheduledSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Theme Mode: "SYSTEM", "LIGHT", "DARK"
    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Cute celebration and XP animations
    private val _celebrationActive = MutableStateFlow(false)
    val celebrationActive: StateFlow<Boolean> = _celebrationActive.asStateFlow()

    private val _recentXpGain = MutableStateFlow<Int?>(null)
    val recentXpGain: StateFlow<Int?> = _recentXpGain.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        _themeMode.value = if (isCurrentlyDark) "LIGHT" else "DARK"
    }

    fun triggerCelebration() {
        _celebrationActive.value = true
    }

    fun dismissCelebration() {
        _celebrationActive.value = false
    }

    fun triggerXpGain(amount: Int) {
        _recentXpGain.value = amount
    }

    fun dismissXpGain() {
        _recentXpGain.value = null
    }

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    // --- Study Timer Actions ---
    fun startStudySession(topic: TopicEntity?, plannedMinutes: Int = 45) {
        timerJob?.cancel()
        viewModelScope.launch {
            val associatedPdf = if (topic != null) {
                pdfDocuments.value.firstOrNull { it.topicId == topic.id || it.subjectId == topic.subjectId }
            } else null

            _timerState.value = TimerUiState(
                isRunning = true,
                isPaused = false,
                elapsedSeconds = 0,
                plannedMinutes = plannedMinutes,
                topic = topic,
                associatedPdf = associatedPdf
            )
            _currentDestination.value = AppDestination.STUDY_TIMER
            runTimer()
        }
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timerState.value.isRunning && !_timerState.value.isPaused) {
                delay(1000)
                _timerState.value = _timerState.value.copy(
                    elapsedSeconds = _timerState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isPaused = true)
        timerJob?.cancel()
    }

    fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isPaused = false)
        runTimer()
    }

    fun extendTimer(additionalMinutes: Int) {
        val current = _timerState.value.plannedMinutes
        _timerState.value = _timerState.value.copy(plannedMinutes = current + additionalMinutes)
        _toastMessage.value = "+$additionalMinutes min added to study quest!"
    }

    fun requestFinishSession() {
        pauseTimer()
        _timerState.value = _timerState.value.copy(showFinishDialog = true)
    }

    fun dismissFinishDialog() {
        _timerState.value = _timerState.value.copy(showFinishDialog = false)
    }

    fun completeStudySession(
        rating: Int,
        keyTakeaways: String,
        notes: String,
        markedComplete: Boolean,
        needsRevision: Boolean
    ) {
        val state = _timerState.value
        val actualSeconds = state.elapsedSeconds
        val topic = state.topic
        val earnedXp = (actualSeconds / 60 * 2).toInt().coerceAtLeast(15)

        viewModelScope.launch {
            val session = StudySessionEntity(
                subjectId = topic?.subjectId,
                chapterId = topic?.chapterId,
                topicId = topic?.id,
                topicTitle = topic?.title ?: "Freeform Bioscience Study",
                plannedMinutes = state.plannedMinutes,
                actualSeconds = actualSeconds,
                startTime = System.currentTimeMillis() - (actualSeconds * 1000),
                endTime = System.currentTimeMillis(),
                understandingRating = rating,
                keyTakeaways = keyTakeaways,
                personalNotes = notes,
                markedNeedsRevision = needsRevision,
                markedTopicCompleted = markedComplete,
                associatedPdfTitle = state.associatedPdf?.title,
                xpEarned = earnedXp
            )
            repository.recordStudySession(session)
            _timerState.value = TimerUiState()
            _recentXpGain.value = earnedXp
            _celebrationActive.value = true
            _toastMessage.value = "Great job! +$earnedXp XP earned!"
            _currentDestination.value = AppDestination.HOME
        }
    }

    // --- PDF Reading & Annotation ---
    fun openPdf(pdfId: Long) {
        _activePdfId.value = pdfId
        _currentDestination.value = AppDestination.PDF_READER
    }

    fun getHighlightsForPdf(pdfId: Long): Flow<List<PdfHighlightEntity>> =
        repository.getHighlightsForPdf(pdfId)

    fun getBookmarksForPdf(pdfId: Long): Flow<List<PdfBookmarkEntity>> =
        repository.getBookmarksForPdf(pdfId)

    fun updatePdfReadingProgress(pdfId: Long, page: Int, totalPages: Int) {
        viewModelScope.launch {
            repository.updatePdfReadingProgress(pdfId, page, totalPages)
        }
    }

    fun addHighlight(
        pdfId: Long,
        page: Int,
        colorHex: String,
        text: String,
        note: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        viewModelScope.launch {
            repository.addHighlight(pdfId, page, colorHex, text, note, x, y, w, h)
            _toastMessage.value = "Highlight saved! (+5 XP)"
        }
    }

    fun deleteHighlight(highlight: PdfHighlightEntity) {
        viewModelScope.launch {
            repository.deleteHighlight(highlight)
            _toastMessage.value = "Highlight deleted"
        }
    }

    fun addBookmark(pdfId: Long, page: Int, title: String, note: String) {
        viewModelScope.launch {
            repository.addBookmark(pdfId, page, title, note)
            _toastMessage.value = "Page $page bookmarked!"
        }
    }

    fun deleteBookmark(bookmark: PdfBookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
            _toastMessage.value = "Bookmark removed"
        }
    }

    fun deletePdf(pdf: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.deletePdf(pdf)
            _toastMessage.value = "Document removed from bookshelf"
        }
    }

    fun importPdfFromUri(uri: Uri, context: Context, title: String, subjectId: Long?, description: String) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val displayName = "imported_${System.currentTimeMillis()}.pdf"
                val destFile = File(context.filesDir, displayName)

                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists() && destFile.length() > 0) {
                    val pageCount = try {
                        val pfd = android.os.ParcelFileDescriptor.open(destFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = android.graphics.pdf.PdfRenderer(pfd)
                        val count = renderer.pageCount
                        renderer.close()
                        pfd.close()
                        count
                    } catch (e: Exception) {
                        1
                    }

                    repository.addPdfDocument(
                        title = title.ifBlank { "Imported Study Guide" },
                        originalFileName = displayName,
                        localFilePath = destFile.absolutePath,
                        sizeBytes = destFile.length(),
                        pageCount = pageCount,
                        subjectId = subjectId,
                        topicId = null,
                        description = description
                    )
                    _toastMessage.value = "PDF successfully imported to bookshelf!"
                } else {
                    _toastMessage.value = "Failed to copy PDF file"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.value = "Error importing PDF: ${e.message}"
            }
        }
    }

    // --- Habits ---
    fun toggleHabitToday(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, repository.getTodayDateString(), isDone)
            if (!isDone) {
                _recentXpGain.value = 10
            }
        }
    }

    fun addHabit(title: String, icon: String, colorHex: String, targetDays: Int) {
        viewModelScope.launch {
            repository.addHabit(title, icon, colorHex, targetDays)
            _toastMessage.value = "Habit created!"
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            _toastMessage.value = "Habit removed"
        }
    }

    // --- Syllabus Actions ---
    fun addSubject(name: String, colorHex: String, iconName: String, priority: Priority, notes: String) {
        viewModelScope.launch {
            repository.addSubject(name, colorHex, iconName, priority, notes)
            _toastMessage.value = "Subject added!"
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _toastMessage.value = "Subject removed"
        }
    }

    fun addChapter(subjectId: Long, title: String, description: String) {
        viewModelScope.launch {
            repository.addChapter(subjectId, title, description)
            _toastMessage.value = "Chapter created!"
        }
    }

    fun addTopic(chapterId: Long, subjectId: Long, title: String, description: String, estimatedMinutes: Int, difficulty: Difficulty, priority: Priority) {
        viewModelScope.launch {
            repository.addTopic(chapterId, subjectId, title, description, estimatedMinutes, difficulty, priority)
            _toastMessage.value = "Topic added to syllabus!"
        }
    }

    fun updateTopicStatus(topic: TopicEntity, newStatus: ItemStatus) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(status = newStatus))
            if (newStatus == ItemStatus.COMPLETED) {
                repository.awardXp(30)
                _recentXpGain.value = 30
                _celebrationActive.value = true
                _toastMessage.value = "Topic completed! +30 XP"
            }
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            _toastMessage.value = "Topic deleted"
        }
    }

    fun addSubtopic(topicId: Long, title: String) {
        viewModelScope.launch {
            repository.addSubtopic(topicId, title)
        }
    }

    fun toggleSubtopic(subtopic: SubtopicEntity) {
        viewModelScope.launch {
            repository.toggleSubtopic(subtopic)
        }
    }

    fun deleteSubtopic(subtopic: SubtopicEntity) {
        viewModelScope.launch {
            repository.deleteSubtopic(subtopic)
        }
    }

    // --- Routine Actions ---
    fun generateSmartRoutine() {
        viewModelScope.launch {
            repository.generateSmartRoutine()
            _toastMessage.value = "Smart study routine generated!"
        }
    }

    // --- Profile & Companion ---
    fun updateCompanion(companion: CompanionType) {
        viewModelScope.launch {
            repository.updateCompanion(companion)
            _toastMessage.value = "Companion updated to ${companion.name}!"
        }
    }

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            repository.updateProfileName(name)
            _toastMessage.value = "Name updated!"
        }
    }

    // --- Export & Reset ---
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataToJson()
            onResult(json)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            _toastMessage.value = "Database reset to starter science templates"
        }
    }
}
