package com.example.studyquest.ui.screens.syllabus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyquest.data.model.*
import com.example.studyquest.ui.components.DifficultyBadge
import com.example.studyquest.ui.components.PriorityBadge
import com.example.studyquest.ui.components.StatusBadge
import com.example.studyquest.ui.viewmodel.StudyQuestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    viewModel: StudyQuestViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.allChapters.collectAsState()
    val topics by viewModel.allTopics.collectAsState()
    val pdfs by viewModel.pdfDocuments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var expandedChapters by remember { mutableStateOf(setOf<Long>()) }

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var topicDialogSubjectId by remember { mutableLongStateOf(0L) }
    var topicDialogChapterId by remember { mutableLongStateOf(0L) }

    // Pre-expand first chapter on initial load
    LaunchedEffect(chapters) {
        if (expandedChapters.isEmpty() && chapters.isNotEmpty()) {
            expandedChapters = setOf(chapters.first().id)
        }
    }

    val filteredSubjects = remember(subjects, selectedSubjectId) {
        if (selectedSubjectId != null) {
            subjects.filter { it.id == selectedSubjectId }
        } else {
            subjects
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Syllabus Manager",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hierarchy: Subject → Chapter → Topic → Subtopics",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddSubjectDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Subject", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search topics, keywords, enzymes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Subject filter pill tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text("All Subjects", fontSize = 12.sp) }
                    )
                    subjects.take(3).forEach { sub ->
                        FilterChip(
                            selected = selectedSubjectId == sub.id,
                            onClick = { selectedSubjectId = if (selectedSubjectId == sub.id) null else sub.id },
                            label = { Text(sub.name.split(":").first(), fontSize = 12.sp, maxLines = 1) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredSubjects, key = { it.id }) { subject ->
                val subjectChapters = chapters.filter { it.subjectId == subject.id }
                val subjectTopics = topics.filter { it.subjectId == subject.id }
                val completedCount = subjectTopics.count { it.status == ItemStatus.COMPLETED }
                val progress = if (subjectTopics.isNotEmpty()) (completedCount.toFloat() / subjectTopics.size.toFloat()) else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Subject Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(subject.colorHex)).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Science,
                                        contentDescription = null,
                                        tint = Color(android.graphics.Color.parseColor(subject.colorHex)),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = subject.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${subjectChapters.size} Chapters • ${subjectTopics.size} Topics",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            PriorityBadge(priority = subject.priority)
                        }

                        // Progress Bar
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress ($completedCount/${subjectTopics.size})",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(android.graphics.Color.parseColor(subject.colorHex)),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Chapters & Topics List
                        Spacer(modifier = Modifier.height(14.dp))
                        subjectChapters.forEach { chapter ->
                            val isExpanded = expandedChapters.contains(chapter.id)
                            val chapterTopics = subjectTopics.filter { it.chapterId == chapter.id }
                                .filter { searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedChapters = if (isExpanded) {
                                                    expandedChapters - chapter.id
                                                } else {
                                                    expandedChapters + chapter.id
                                                }
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = chapter.title,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (chapter.description.isNotBlank()) {
                                                    Text(
                                                        text = chapter.description,
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${chapterTopics.size} topics",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Topic Items inside Chapter
                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            chapterTopics.forEach { topic ->
                                                TopicItemCard(
                                                    topic = topic,
                                                    associatedPdf = pdfs.firstOrNull { it.topicId == topic.id || it.subjectId == topic.subjectId },
                                                    onStartStudy = {
                                                        viewModel.startStudySession(topic, topic.estimatedMinutes)
                                                    },
                                                    onStatusChange = { newStatus ->
                                                        viewModel.updateTopicStatus(topic, newStatus)
                                                    },
                                                    onOpenPdf = { pdfId ->
                                                        viewModel.openPdf(pdfId)
                                                    }
                                                )
                                            }

                                            // Add Topic button inside chapter
                                            OutlinedButton(
                                                onClick = {
                                                    topicDialogSubjectId = subject.id
                                                    topicDialogChapterId = chapter.id
                                                    showAddTopicDialog = true
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add Topic to Chapter", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var newSubName by remember { mutableStateOf("") }
        var newSubNotes by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#7E57C2") }
        val colorPalette = listOf("#7E57C2", "#26A69A", "#FF7043", "#42A5F5", "#AB47BC", "#2E7D32")

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add New Subject", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newSubName,
                        onValueChange = { newSubName = it },
                        label = { Text("Subject Name (e.g. Immunology)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSubNotes,
                        onValueChange = { newSubNotes = it },
                        label = { Text("Key objectives or exam date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Subject Accent Color:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorPalette.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == hex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubName.isNotBlank()) {
                            viewModel.addSubject(newSubName, selectedColor, "science", Priority.HIGH, newSubNotes)
                            showAddSubjectDialog = false
                        }
                    }
                ) {
                    Text("Create Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Topic Dialog
    if (showAddTopicDialog) {
        var topicTitle by remember { mutableStateOf("") }
        var topicDesc by remember { mutableStateOf("") }
        var estimatedMins by remember { mutableStateOf("45") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Add Topic to Syllabus", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Topic Title (e.g. Glycolysis Regulation)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = topicDesc,
                        onValueChange = { topicDesc = it },
                        label = { Text("Topic Description / Objectives") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estimatedMins,
                        onValueChange = { estimatedMins = it },
                        label = { Text("Estimated Study Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicTitle.isNotBlank()) {
                            val mins = estimatedMins.toIntOrNull() ?: 45
                            viewModel.addTopic(
                                chapterId = topicDialogChapterId,
                                subjectId = topicDialogSubjectId,
                                title = topicTitle,
                                description = topicDesc,
                                estimatedMinutes = mins,
                                difficulty = Difficulty.MEDIUM,
                                priority = Priority.HIGH
                            )
                            showAddTopicDialog = false
                        }
                    }
                ) {
                    Text("Add Topic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TopicItemCard(
    topic: TopicEntity,
    associatedPdf: PdfDocumentEntity?,
    onStartStudy: () -> Unit,
    onStatusChange: (ItemStatus) -> Unit,
    onOpenPdf: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topic.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = topic.status)
            }

            if (topic.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyBadge(difficulty = topic.difficulty)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⏱ ${topic.estimatedMinutes}m",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick Status Menu Toggles
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (topic.status != ItemStatus.COMPLETED) {
                        IconButton(
                            onClick = { onStatusChange(ItemStatus.COMPLETED) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = "Mark Complete", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (topic.status != ItemStatus.NEEDS_REVISION) {
                        IconButton(
                            onClick = { onStatusChange(ItemStatus.NEEDS_REVISION) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Update, contentDescription = "Needs Revision", tint = Color(0xFFEF6C00), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartStudy,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Study Now", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                }

                if (associatedPdf != null) {
                    OutlinedButton(
                        onClick = { onOpenPdf(associatedPdf.id) },
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
