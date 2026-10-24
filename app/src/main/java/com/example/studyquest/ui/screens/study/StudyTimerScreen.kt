package com.example.studyquest.ui.screens.study

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyquest.ui.viewmodel.AppDestination
import com.example.studyquest.ui.viewmodel.StudyQuestViewModel
import java.util.Locale

@Composable
fun StudyTimerScreen(
    viewModel: StudyQuestViewModel,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsState()
    val topic = timerState.topic
    val associatedPdf = timerState.associatedPdf

    val totalPlannedSeconds = (timerState.plannedMinutes * 60).toLong()
    val elapsedSeconds = timerState.elapsedSeconds
    val remainingSeconds = (totalPlannedSeconds - elapsedSeconds).coerceAtLeast(0)

    val minutesDisplay = (elapsedSeconds / 60)
    val secondsDisplay = (elapsedSeconds % 60)
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutesDisplay, secondsDisplay)

    val progress = if (totalPlannedSeconds > 0) {
        (elapsedSeconds.toFloat() / totalPlannedSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var scratchpadNotes by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_timer")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timerState.isRunning && !timerState.isPaused) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppDestination.HOME) }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (timerState.isPaused) "PAUSED" else "STUDY MISSION ACTIVE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = { viewModel.requestFinishSession() }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Finish Session", tint = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Topic Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = topic?.title ?: "Deep Science Focus Session",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (topic?.description?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = topic.description,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Circular Study Timer
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Goal: ${timerState.plannedMinutes} mins",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Controls (Pause, Resume, Extend)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.extendTimer(5) },
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+5m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            FloatingActionButton(
                onClick = {
                    if (timerState.isPaused) {
                        viewModel.resumeTimer()
                    } else {
                        viewModel.pauseTimer()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (timerState.isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(36.dp)
                )
            }

            Button(
                onClick = { viewModel.requestFinishSession() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = "Finish")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Associated PDF shortcut (Read while studying!)
        if (associatedPdf != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Associated Textbook",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = associatedPdf.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.openPdf(associatedPdf.id) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Open Reader", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scratchpad notes
        OutlinedTextField(
            value = scratchpadNotes,
            onValueChange = { scratchpadNotes = it },
            label = { Text("Session Scratchpad / Quick Notes") },
            placeholder = { Text("Jot down formulas, questions, or key takeaways...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 5
        )
    }

    // Finish Session Dialog
    if (timerState.showFinishDialog) {
        var rating by remember { mutableIntStateOf(5) }
        var keyTakeaways by remember { mutableStateOf(scratchpadNotes) }
        var markCompleted by remember { mutableStateOf(true) }
        var needsRevision by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissFinishDialog() },
            title = {
                Text("Complete Study Mission 🎉", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Studied for ${elapsedSeconds / 60} minutes! How was your comprehension?",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 5-Star Rating
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = keyTakeaways,
                        onValueChange = { keyTakeaways = it },
                        label = { Text("Key Takeaways / Reflection") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Checkboxes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = markCompleted,
                            onCheckedChange = { markCompleted = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mark topic COMPLETED (+25 Bonus XP)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = needsRevision,
                            onCheckedChange = { needsRevision = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Flag topic for Spaced Revision",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeStudySession(
                            rating = rating,
                            keyTakeaways = keyTakeaways,
                            notes = scratchpadNotes,
                            markedComplete = markCompleted,
                            needsRevision = needsRevision
                        )
                    }
                ) {
                    Text("Save & Claim XP")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissFinishDialog() }) {
                    Text("Continue Studying")
                }
            }
        )
    }
}
