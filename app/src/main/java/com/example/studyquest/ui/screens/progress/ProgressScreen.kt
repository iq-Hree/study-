package com.example.studyquest.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.studyquest.ui.components.StatCard
import com.example.studyquest.ui.components.XPLevelProgress
import com.example.studyquest.ui.viewmodel.StudyQuestViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    viewModel: StudyQuestViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val studySessions by viewModel.studySessions.collectAsState()
    val topics by viewModel.allTopics.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val pdfs by viewModel.pdfDocuments.collectAsState()
    val highlights by viewModel.allHighlights.collectAsState()

    val totalSeconds = studySessions.sumOf { it.actualSeconds }
    val totalHours = String.format(Locale.getDefault(), "%.1f", totalSeconds / 3600f)
    val completedCount = topics.count { it.status == ItemStatus.COMPLETED }
    val revisionCount = topics.count { it.status == ItemStatus.NEEDS_REVISION }

    val achievements = remember(studySessions, topics, highlights, pdfs) {
        listOf(
            AchievementData("First Step", "Completed your very first study mission", Icons.Default.Flag, isUnlocked = studySessions.isNotEmpty()),
            AchievementData("Bioscience Scholar", "Conquered molecular biology & genetics topics", Icons.Default.Biotech, isUnlocked = true),
            AchievementData("Bookworm", "Added & opened textbooks on your digital bookshelf", Icons.Default.MenuBook, isUnlocked = pdfs.isNotEmpty()),
            AchievementData("Active Annotator", "Created persistent highlights & personal notes", Icons.Default.BorderColor, isUnlocked = highlights.isNotEmpty()),
            AchievementData("Spaced Revisionist", "Flagged and revised challenging concepts", Icons.Default.Autorenew, isUnlocked = revisionCount > 0 || completedCount > 0),
            AchievementData("Grand Scholar", "Reach Level 5 and 1000 XP", Icons.Default.EmojiEvents, isUnlocked = profile.level >= 5)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Progress & Analytics",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Honest metrics and achievements on your learning path",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // XP & Level progress
        item {
            XPLevelProgress(
                level = profile.level,
                currentXp = profile.totalXp
            )
        }

        // Stat Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Focus Time",
                    value = "$totalHours hrs",
                    subtitle = "${studySessions.size} total sessions",
                    icon = Icons.Default.HourglassTop,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Topics Mastered",
                    value = "$completedCount",
                    subtitle = "$revisionCount need revision",
                    icon = Icons.Default.TaskAlt,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Subject Breakdown Progress
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Syllabus Mastery by Subject",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    subjects.forEach { subject ->
                        val subTopics = topics.filter { it.subjectId == subject.id }
                        val done = subTopics.count { it.status == ItemStatus.COMPLETED }
                        val pct = if (subTopics.isNotEmpty()) (done.toFloat() / subTopics.size.toFloat()) else 0f

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = subject.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$done / ${subTopics.size} (${(pct * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(android.graphics.Color.parseColor(subject.colorHex)),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Achievements Badges Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scholar Achievements",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        achievements.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { item ->
                                    Surface(
                                        color = if (item.isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (item.isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    item.icon,
                                                    contentDescription = null,
                                                    tint = if (item.isUnlocked) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                                )
                                                Text(
                                                    text = if (item.isUnlocked) "Unlocked" else "Locked",
                                                    fontSize = 10.sp,
                                                    color = if (item.isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Study Sessions History
        item {
            Text(
                text = "Past Study Missions (${studySessions.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(studySessions.take(5)) { session ->
            val dateStr = remember(session.startTime) {
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                sdf.format(Date(session.startTime))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = session.topicTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+${session.xpEarned} XP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$dateStr • ${session.actualSeconds / 60} mins studied",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "⭐ ${session.understandingRating}/5",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (session.keyTakeaways.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${session.keyTakeaways}\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private data class AchievementData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean
)
