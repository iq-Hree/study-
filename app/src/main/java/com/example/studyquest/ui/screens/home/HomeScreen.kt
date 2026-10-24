package com.example.studyquest.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.studyquest.data.model.ItemStatus
import com.example.studyquest.data.model.TopicEntity
import com.example.studyquest.ui.components.*
import com.example.studyquest.ui.viewmodel.AppDestination
import com.example.studyquest.ui.viewmodel.StudyQuestViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: StudyQuestViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val topics by viewModel.allTopics.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val todayHabitLogs by viewModel.todayHabitLogs.collectAsState()
    val pdfs by viewModel.pdfDocuments.collectAsState()
    val studySessions by viewModel.studySessions.collectAsState()

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    // Next topic to study
    val nextTopic = remember(topics) {
        topics.firstOrNull { it.status == ItemStatus.IN_PROGRESS }
            ?: topics.firstOrNull { it.status == ItemStatus.NEEDS_REVISION }
            ?: topics.firstOrNull { it.status == ItemStatus.NOT_STARTED }
    }

    // Stats calculations
    val totalTopics = topics.size
    val completedTopics = topics.count { it.status == ItemStatus.COMPLETED }
    val syllabusPercent = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics.toFloat()) * 100).toInt() else 0

    // Today's study time
    val todayMidnight = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.timeInMillis
    }
    val todayStudySeconds = studySessions
        .filter { it.startTime >= todayMidnight }
        .sumOf { it.actualSeconds }
    val todayStudyMinutes = (todayStudySeconds / 60).toInt()

    val continuePdf = remember(pdfs) {
        pdfs.maxByOrNull { it.lastOpenedAt }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Welcome back, ${profile.userName}!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val currentThemeMode by viewModel.themeMode.collectAsState()
                val isDark = when (currentThemeMode) {
                    "DARK" -> true
                    "LIGHT" -> false
                    else -> androidx.compose.foundation.isSystemInDarkTheme()
                }

                AnimatedThemeToggle(
                    isDark = isDark,
                    onToggle = { viewModel.toggleTheme(isDark) }
                )
            }
        }

        // Companion Speech Card
        item {
            CompanionBanner(
                companionType = profile.companion,
                userName = profile.userName,
                level = profile.level,
                streakDays = 3,
                onCompanionClick = {
                    viewModel.navigateTo(AppDestination.SETTINGS)
                }
            )
        }

        // Level & XP Progress
        item {
            XPLevelProgress(
                level = profile.level,
                currentXp = profile.totalXp
            )
        }

        // Prominent "Start Studying" Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "NEXT STUDY MISSION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (nextTopic != null) {
                            PriorityBadge(priority = nextTopic.priority)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = nextTopic?.title ?: "Free Exploration & Science Review",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val subjectName = subjects.firstOrNull { it.id == nextTopic?.subjectId }?.name ?: "Bioscience"
                    Text(
                        text = "$subjectName • ${nextTopic?.estimatedMinutes ?: 45} mins planned",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.startStudySession(nextTopic, nextTopic?.estimatedMinutes ?: 45)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Studying Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today's Study",
                    value = "$todayStudyMinutes min",
                    subtitle = "Target: ${profile.dailyStudyTargetMinutes} min",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Syllabus Mastery",
                    value = "$syllabusPercent%",
                    subtitle = "$completedTopics of $totalTopics topics done",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Continue Reading PDF shortcut
        if (continuePdf != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.openPdf(continuePdf.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Continue Reading",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = continuePdf.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Page ${continuePdf.lastReadPage} of ${continuePdf.pageCount} (${continuePdf.readingProgressPercent}%)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            Icons.Default.ArrowForwardIos,
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Today's Habits Quick Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Study Habits",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = { viewModel.navigateTo(AppDestination.HABITS) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("View All", fontSize = 12.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (habits.isEmpty()) {
                        Text(
                            text = "No active habits yet. Add your first study routine!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        habits.take(3).forEach { habit ->
                            val isCompletedToday = todayHabitLogs.any { it.habitId == habit.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.toggleHabitToday(habit.id, isCompletedToday)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isCompletedToday,
                                    onCheckedChange = {
                                        viewModel.toggleHabitToday(habit.id, isCompletedToday)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = habit.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isCompletedToday) FontWeight.Normal else FontWeight.Medium,
                                    color = if (isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "🔥 ${habit.currentStreak}d",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
