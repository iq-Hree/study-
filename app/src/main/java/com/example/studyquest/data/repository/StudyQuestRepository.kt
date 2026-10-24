package com.example.studyquest.data.repository

import android.content.Context
import com.example.studyquest.data.local.StudyQuestDao
import com.example.studyquest.data.model.*
import com.example.studyquest.data.sample.PdfSampleGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class StudyQuestRepository(
    private val dao: StudyQuestDao,
    private val context: Context
) {
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allTopics: Flow<List<TopicEntity>> = dao.getAllTopics()
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allStudySessions: Flow<List<StudySessionEntity>> = dao.getAllStudySessions()
    val activeHabits: Flow<List<HabitEntity>> = dao.getActiveHabits()
    val allHabits: Flow<List<HabitEntity>> = dao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLogEntity>> = dao.getAllHabitLogs()
    val allPdfDocuments: Flow<List<PdfDocumentEntity>> = dao.getAllPdfDocuments()
    val allHighlights: Flow<List<PdfHighlightEntity>> = dao.getAllHighlights()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val scheduledSessions: Flow<List<ScheduledSessionEntity>> = dao.getAllScheduledSessions()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getHabitLogsForToday(): Flow<List<HabitLogEntity>> {
        return dao.getHabitLogsForDate(getTodayDateString())
    }

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        dao.getChaptersForSubject(subjectId)

    fun getTopicsForChapter(chapterId: Long): Flow<List<TopicEntity>> =
        dao.getTopicsForChapter(chapterId)

    fun getSubtopicsForTopic(topicId: Long): Flow<List<SubtopicEntity>> =
        dao.getSubtopicsForTopic(topicId)

    fun getHighlightsForPdf(pdfId: Long): Flow<List<PdfHighlightEntity>> =
        dao.getHighlightsForPdf(pdfId)

    fun getBookmarksForPdf(pdfId: Long): Flow<List<PdfBookmarkEntity>> =
        dao.getBookmarksForPdf(pdfId)

    suspend fun getPdfById(id: Long): PdfDocumentEntity? = dao.getPdfDocumentById(id)

    suspend fun getTopicById(id: Long): TopicEntity? = dao.getTopicById(id)

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfile = dao.getUserProfileOnce()
        if (existingProfile == null) {
            dao.insertUserProfile(
                UserProfileEntity(
                    id = 1,
                    userName = "Cadet Scholar",
                    companion = CompanionType.CAT,
                    totalXp = 80,
                    level = 1,
                    dailyStudyTargetMinutes = 90,
                    preferredSessionMinutes = 45,
                    breakMinutes = 10
                )
            )
        }

        val subjects = dao.getAllSubjects().firstOrNull() ?: emptyList()
        if (subjects.isEmpty()) {
            // Seed Subject 1: Molecular Biology & Genetics
            val sub1Id = dao.insertSubject(
                SubjectEntity(
                    name = "Bioscience: Molecular Biology & Genetics",
                    colorHex = "#7E57C2",
                    iconName = "biotech",
                    priority = Priority.HIGH,
                    notes = "Key focus on replication enzymes, transcription factors, and gene expression."
                )
            )
            val chap1Id = dao.insertChapter(
                ChapterEntity(
                    subjectId = sub1Id,
                    title = "DNA Structure and Replication Machinery",
                    description = "Double helix architecture, replication fork enzymes, and proofreading.",
                    sortOrder = 1
                )
            )
            val top1Id = dao.insertTopic(
                TopicEntity(
                    chapterId = chap1Id,
                    subjectId = sub1Id,
                    title = "Replication Fork Enzymology",
                    description = "Helicase, topoisomerase, single-stranded binding proteins, and primase.",
                    estimatedMinutes = 45,
                    difficulty = Difficulty.MEDIUM,
                    priority = Priority.HIGH,
                    status = ItemStatus.IN_PROGRESS,
                    sortOrder = 1
                )
            )
            dao.insertSubtopic(SubtopicEntity(topicId = top1Id, title = "Helicase unwinding & SSB stabilization", isCompleted = true, sortOrder = 1))
            dao.insertSubtopic(SubtopicEntity(topicId = top1Id, title = "DNA Polymerase III vs Polymerase I", isCompleted = false, sortOrder = 2))
            dao.insertSubtopic(SubtopicEntity(topicId = top1Id, title = "Okazaki fragment ligation with DNA Ligase", isCompleted = false, sortOrder = 3))

            val top2Id = dao.insertTopic(
                TopicEntity(
                    chapterId = chap1Id,
                    subjectId = sub1Id,
                    title = "Transcription & Pre-mRNA Splicing",
                    description = "RNA Polymerase II, promoter consensus, intron removal, and snRNPs.",
                    estimatedMinutes = 60,
                    difficulty = Difficulty.HARD,
                    priority = Priority.HIGH,
                    status = ItemStatus.NOT_STARTED,
                    sortOrder = 2
                )
            )
            dao.insertSubtopic(SubtopicEntity(topicId = top2Id, title = "TATA box recognition & transcription factors", isCompleted = false, sortOrder = 1))
            dao.insertSubtopic(SubtopicEntity(topicId = top2Id, title = "Spliceosome mechanics & alternative splicing", isCompleted = false, sortOrder = 2))

            // Seed Subject 2: Human Physiology
            val sub2Id = dao.insertSubject(
                SubjectEntity(
                    name = "Human Physiology & Neurobiology",
                    colorHex = "#26A69A",
                    iconName = "psychology",
                    priority = Priority.HIGH,
                    notes = "Action potential electrophysiology and autonomic regulation."
                )
            )
            val chap2Id = dao.insertChapter(
                ChapterEntity(
                    subjectId = sub2Id,
                    title = "Cellular Neurophysiology",
                    description = "Membrane potentials, ion channel gating, and synaptic transmission.",
                    sortOrder = 1
                )
            )
            dao.insertTopic(
                TopicEntity(
                    chapterId = chap2Id,
                    subjectId = sub2Id,
                    title = "Action Potential Generation & Saltatory Conduction",
                    description = "Voltage-gated sodium/potassium dynamics and myelin sheath function.",
                    estimatedMinutes = 50,
                    difficulty = Difficulty.MEDIUM,
                    priority = Priority.HIGH,
                    status = ItemStatus.NOT_STARTED,
                    sortOrder = 1
                )
            )

            // Seed Subject 3: Organic Biochemistry
            val sub3Id = dao.insertSubject(
                SubjectEntity(
                    name = "Biochemistry & Enzyme Kinetics",
                    colorHex = "#FF7043",
                    iconName = "science",
                    priority = Priority.MEDIUM,
                    notes = "Thermodynamics of glycolysis, TCA cycle, and Michaelis-Menten kinetics."
                )
            )
            val chap3Id = dao.insertChapter(
                ChapterEntity(
                    subjectId = sub3Id,
                    title = "Enzymology and Metabolic Regulation",
                    description = "Km, Vmax, allosteric inhibition, and feedback loops.",
                    sortOrder = 1
                )
            )
            dao.insertTopic(
                TopicEntity(
                    chapterId = chap3Id,
                    subjectId = sub3Id,
                    title = "Michaelis-Menten Kinetics & Lineweaver-Burk Plots",
                    description = "Derivation of rate equations and competitive vs non-competitive inhibitors.",
                    estimatedMinutes = 40,
                    difficulty = Difficulty.HARD,
                    priority = Priority.MEDIUM,
                    status = ItemStatus.NOT_STARTED,
                    sortOrder = 1
                )
            )

            // Seed Starter Habits
            dao.insertHabit(HabitEntity(title = "Daily 45-min Deep Study", icon = "timer", colorHex = "#7E57C2", targetDaysPerWeek = 7, currentStreak = 2, bestStreak = 5))
            dao.insertHabit(HabitEntity(title = "Read 10 Textbook Pages", icon = "menu_book", colorHex = "#42A5F5", targetDaysPerWeek = 6, currentStreak = 3, bestStreak = 7))
            dao.insertHabit(HabitEntity(title = "Active Recall & Flashcard Review", icon = "lightbulb", colorHex = "#FFA726", targetDaysPerWeek = 5, currentStreak = 1, bestStreak = 4))
            dao.insertHabit(HabitEntity(title = "Review Formula Sheet & Diagrams", icon = "draw", colorHex = "#66BB6A", targetDaysPerWeek = 5, currentStreak = 2, bestStreak = 6))

            // Seed starter sample PDFs
            try {
                val pdf1 = PdfSampleGenerator.generateMolecularBiologyPdf(context)
                val pdfDoc1Id = dao.insertPdfDocument(
                    PdfDocumentEntity(
                        title = "Molecular Biology & Genetics Core Textbook",
                        originalFileName = pdf1.name,
                        localFilePath = pdf1.absolutePath,
                        fileSizeBytes = pdf1.length(),
                        pageCount = 3,
                        lastReadPage = 1,
                        readingProgressPercent = 33,
                        subjectId = sub1Id,
                        topicId = top1Id,
                        isAvailableOffline = true,
                        description = "Complete guide to DNA replication, transcription, and translation mechanics."
                    )
                )

                // Add starter highlights and bookmark for testability
                dao.insertHighlight(
                    PdfHighlightEntity(
                        pdfId = pdfDoc1Id,
                        pageNumber = 1,
                        colorHex = "#FFF176",
                        selectedText = "Erwin Chargaff's rules dictate that the quantity of Adenine equals Thymine (A = T) and Guanine equals Cytosine (G = C).",
                        note = "Crucial rule for exam base-pairing calculation questions!",
                        normX = 0.08f,
                        normY = 0.22f,
                        normWidth = 0.84f,
                        normHeight = 0.05f
                    )
                )
                dao.insertBookmark(
                    PdfBookmarkEntity(
                        pdfId = pdfDoc1Id,
                        pageNumber = 1,
                        title = "Replication Machinery Overview",
                        note = "Table of enzymes from Helicase to Ligase."
                    )
                )

                val pdf2 = PdfSampleGenerator.generatePhysiologyPrimerPdf(context)
                dao.insertPdfDocument(
                    PdfDocumentEntity(
                        title = "Human Physiology & Neurobiology Essentials",
                        originalFileName = pdf2.name,
                        localFilePath = pdf2.absolutePath,
                        fileSizeBytes = pdf2.length(),
                        pageCount = 2,
                        lastReadPage = 1,
                        readingProgressPercent = 0,
                        subjectId = sub2Id,
                        isAvailableOffline = true,
                        description = "Resting membrane potential, action potential ion currents, and synaptic vesicles."
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Seed a starter past study session
            dao.insertStudySession(
                StudySessionEntity(
                    subjectId = sub1Id,
                    chapterId = chap1Id,
                    topicId = top1Id,
                    topicTitle = "Replication Fork Enzymology",
                    plannedMinutes = 45,
                    actualSeconds = 2700,
                    startTime = System.currentTimeMillis() - 86400000L,
                    endTime = System.currentTimeMillis() - 86400000L + 2700000L,
                    understandingRating = 5,
                    keyTakeaways = "SSBs prevent secondary structure hairpin loops during unwinding.",
                    personalNotes = "Mastered leading vs lagging strand synthesis.",
                    markedTopicCompleted = false,
                    associatedPdfTitle = "Molecular Biology & Genetics Core Textbook",
                    xpEarned = 35
                )
            )

            // Seed weekly routine sessions
            dao.insertScheduledSession(ScheduledSessionEntity(topicId = top1Id, subjectId = sub1Id, dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK), startHour = 16, startMinute = 0, durationMinutes = 45))
        }
    }

    // Subject operations
    suspend fun addSubject(name: String, colorHex: String, iconName: String, priority: Priority, notes: String): Long {
        return dao.insertSubject(SubjectEntity(name = name, colorHex = colorHex, iconName = iconName, priority = priority, notes = notes))
    }
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    // Chapter operations
    suspend fun addChapter(subjectId: Long, title: String, description: String): Long {
        return dao.insertChapter(ChapterEntity(subjectId = subjectId, title = title, description = description))
    }
    suspend fun updateChapter(chapter: ChapterEntity) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapter: ChapterEntity) = dao.deleteChapter(chapter)

    // Topic operations
    suspend fun addTopic(chapterId: Long, subjectId: Long, title: String, description: String, estimatedMinutes: Int, difficulty: Difficulty, priority: Priority): Long {
        return dao.insertTopic(TopicEntity(chapterId = chapterId, subjectId = subjectId, title = title, description = description, estimatedMinutes = estimatedMinutes, difficulty = difficulty, priority = priority))
    }
    suspend fun updateTopic(topic: TopicEntity) = dao.updateTopic(topic)
    suspend fun deleteTopic(topic: TopicEntity) = dao.deleteTopic(topic)

    // Subtopic operations
    suspend fun addSubtopic(topicId: Long, title: String): Long {
        return dao.insertSubtopic(SubtopicEntity(topicId = topicId, title = title))
    }
    suspend fun toggleSubtopic(subtopic: SubtopicEntity) {
        dao.updateSubtopic(subtopic.copy(isCompleted = !subtopic.isCompleted))
    }
    suspend fun deleteSubtopic(subtopic: SubtopicEntity) = dao.deleteSubtopic(subtopic)

    // Study sessions and XP
    suspend fun recordStudySession(session: StudySessionEntity): Long {
        val id = dao.insertStudySession(session)
        awardXp(session.xpEarned)

        // If marked topic completed, update topic status
        if (session.markedTopicCompleted && session.topicId != null) {
            val topic = dao.getTopicById(session.topicId)
            if (topic != null) {
                dao.updateTopic(topic.copy(status = ItemStatus.COMPLETED))
                awardXp(25) // Completion bonus
            }
        } else if (session.markedNeedsRevision && session.topicId != null) {
            val topic = dao.getTopicById(session.topicId)
            if (topic != null) {
                dao.updateTopic(topic.copy(status = ItemStatus.NEEDS_REVISION))
            }
        }
        return id
    }

    suspend fun awardXp(amount: Int) {
        val currentProfile = dao.getUserProfileOnce() ?: UserProfileEntity()
        val newXp = currentProfile.totalXp + amount
        val newLevel = when {
            newXp >= 1000 -> 5
            newXp >= 600 -> 4
            newXp >= 300 -> 3
            newXp >= 120 -> 2
            else -> 1
        }
        dao.insertUserProfile(currentProfile.copy(totalXp = newXp, level = newLevel))
    }

    suspend fun updateCompanion(companion: CompanionType) {
        val profile = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertUserProfile(profile.copy(companion = companion))
    }

    suspend fun updateProfileName(name: String) {
        val profile = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertUserProfile(profile.copy(userName = name))
    }

    // Habit operations
    suspend fun addHabit(title: String, icon: String, colorHex: String, targetDays: Int): Long {
        return dao.insertHabit(HabitEntity(title = title, icon = icon, colorHex = colorHex, targetDaysPerWeek = targetDays))
    }

    suspend fun toggleHabitForDate(habitId: Long, dateString: String, currentlyCompleted: Boolean) {
        if (currentlyCompleted) {
            dao.deleteHabitLog(habitId, dateString)
        } else {
            dao.insertHabitLog(HabitLogEntity(habitId = habitId, dateString = dateString, completed = true))
            awardXp(10)
        }
    }

    suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)

    // PDF operations
    suspend fun addPdfDocument(title: String, originalFileName: String, localFilePath: String, sizeBytes: Long, pageCount: Int, subjectId: Long?, topicId: Long?, description: String): Long {
        return dao.insertPdfDocument(
            PdfDocumentEntity(
                title = title,
                originalFileName = originalFileName,
                localFilePath = localFilePath,
                fileSizeBytes = sizeBytes,
                pageCount = pageCount,
                lastReadPage = 1,
                readingProgressPercent = 0,
                subjectId = subjectId,
                topicId = topicId,
                isAvailableOffline = true,
                description = description
            )
        )
    }

    suspend fun updatePdfReadingProgress(pdfId: Long, currentPage: Int, totalPages: Int) {
        val doc = dao.getPdfDocumentById(pdfId) ?: return
        val percent = if (totalPages > 0) ((currentPage.toFloat() / totalPages.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
        dao.updatePdfDocument(
            doc.copy(
                lastReadPage = currentPage,
                pageCount = totalPages,
                readingProgressPercent = percent,
                lastOpenedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePdf(pdf: PdfDocumentEntity) {
        // delete local file if in private directory
        try {
            val file = File(pdf.localFilePath)
            if (file.exists() && file.parentFile?.absolutePath?.startsWith(context.filesDir.absolutePath) == true) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dao.deletePdfDocument(pdf)
    }

    // Highlights
    suspend fun addHighlight(pdfId: Long, pageNumber: Int, colorHex: String, selectedText: String, note: String, x: Float, y: Float, w: Float, h: Float): Long {
        val id = dao.insertHighlight(
            PdfHighlightEntity(
                pdfId = pdfId,
                pageNumber = pageNumber,
                colorHex = colorHex,
                selectedText = selectedText,
                note = note,
                normX = x,
                normY = y,
                normWidth = w,
                normHeight = h
            )
        )
        awardXp(5)
        return id
    }

    suspend fun deleteHighlight(highlight: PdfHighlightEntity) = dao.deleteHighlight(highlight)

    // Bookmarks
    suspend fun addBookmark(pdfId: Long, pageNumber: Int, title: String, note: String): Long {
        return dao.insertBookmark(
            PdfBookmarkEntity(
                pdfId = pdfId,
                pageNumber = pageNumber,
                title = title,
                note = note
            )
        )
    }

    suspend fun deleteBookmark(bookmark: PdfBookmarkEntity) = dao.deleteBookmark(bookmark)

    // Routine generator
    suspend fun generateSmartRoutine() = withContext(Dispatchers.IO) {
        dao.clearScheduledSessions()
        val allTopicsList = dao.getAllTopics().firstOrNull() ?: emptyList()
        val pendingTopics = allTopicsList.filter { it.status != ItemStatus.COMPLETED }
            .sortedWith(compareByDescending<TopicEntity> { it.priority.ordinal }.thenBy { it.difficulty.ordinal })

        if (pendingTopics.isEmpty()) return@withContext

        var topicIndex = 0
        // Distribute across days Monday (2) to Sunday (1 in Calendar format, or 1 to 7)
        for (day in 1..7) {
            if (topicIndex >= pendingTopics.size) break
            val topic = pendingTopics[topicIndex]
            val startHour = when (day) {
                1, 7 -> 11 // weekends
                else -> 17 // weekdays
            }
            dao.insertScheduledSession(
                ScheduledSessionEntity(
                    topicId = topic.id,
                    subjectId = topic.subjectId,
                    dayOfWeek = day,
                    startHour = startHour,
                    startMinute = 0,
                    durationMinutes = topic.estimatedMinutes
                )
            )
            topicIndex++
        }
    }

    // Export & Backup
    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val subjectsArr = JSONArray()
        dao.getAllSubjects().firstOrNull()?.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("colorHex", s.colorHex)
            obj.put("priority", s.priority.name)
            obj.put("notes", s.notes)
            subjectsArr.put(obj)
        }
        root.put("subjects", subjectsArr)

        val topicsArr = JSONArray()
        dao.getAllTopics().firstOrNull()?.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("chapterId", t.chapterId)
            obj.put("subjectId", t.subjectId)
            obj.put("title", t.title)
            obj.put("status", t.status.name)
            obj.put("priority", t.priority.name)
            topicsArr.put(obj)
        }
        root.put("topics", topicsArr)

        val sessionsArr = JSONArray()
        dao.getAllStudySessions().firstOrNull()?.forEach { ss ->
            val obj = JSONObject()
            obj.put("topicTitle", ss.topicTitle)
            obj.put("actualSeconds", ss.actualSeconds)
            obj.put("startTime", ss.startTime)
            obj.put("rating", ss.understandingRating)
            obj.put("notes", ss.personalNotes)
            sessionsArr.put(obj)
        }
        root.put("studySessions", sessionsArr)

        return@withContext root.toString(2)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        // re-seed fresh database
        dao.clearScheduledSessions()
        dao.getAllSubjects().firstOrNull()?.forEach { dao.deleteSubject(it) }
        dao.getAllHabits().firstOrNull()?.forEach { dao.deleteHabit(it) }
        dao.getAllPdfDocuments().firstOrNull()?.forEach { dao.deletePdfDocument(it) }
        initializeDefaultDataIfEmpty()
    }
}
