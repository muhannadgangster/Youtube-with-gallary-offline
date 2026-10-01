package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.theme.YtBlue
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtPillActive
import com.example.ui.theme.YtPillActiveText
import com.example.ui.theme.YtPillBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

@Composable
fun CreateClipDialog(
    videos: List<VideoItem>,
    onDismiss: () -> Unit,
    onSaveClip: (selectedVideo: VideoItem, clipTitle: String, durationSec: Int) -> Unit
) {
    var selectedVideo by remember { mutableStateOf(videos.firstOrNull()) }
    var clipTitle by remember { mutableStateOf(selectedVideo?.let { "${it.displayTitle} #Shorts" } ?: "New Short Clip") }
    var selectedDurationSec by remember { mutableIntStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YtSurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Create Shorts Clip",
                color = YtTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select a local video to convert into a Shorts clip:",
                    color = YtTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Video Selector dropdown/list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(YtSurfaceVariant)
                        .padding(8.dp)
                ) {
                    LazyColumn {
                        items(videos) { vid ->
                            val isChosen = selectedVideo?.id == vid.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) Color(0x33FF0033) else Color.Transparent)
                                    .clickable {
                                        selectedVideo = vid
                                        clipTitle = "${vid.displayTitle} #Shorts"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = vid.displayTitle,
                                    color = if (isChosen) YtRed else YtTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = vid.formattedDuration,
                                    color = YtTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Selector
                Text(
                    text = "Clip Duration:",
                    color = YtTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 60).forEach { sec ->
                        val isChosen = selectedDurationSec == sec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isChosen) YtPillActive else YtPillBackground)
                                .clickable { selectedDurationSec = sec }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sec}s",
                                color = if (isChosen) YtPillActiveText else YtTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Title
                OutlinedTextField(
                    value = clipTitle,
                    onValueChange = { clipTitle = it },
                    label = { Text("Shorts Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YtBlue,
                        unfocusedBorderColor = YtBorder,
                        focusedTextColor = YtTextPrimary,
                        unfocusedTextColor = YtTextPrimary,
                        focusedLabelColor = YtBlue,
                        unfocusedLabelColor = YtTextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val vid = selectedVideo
                    if (vid != null && clipTitle.isNotBlank()) {
                        onSaveClip(vid, clipTitle.trim(), selectedDurationSec)
                    }
                },
                modifier = Modifier.testTag("clip_create_confirm")
            ) {
                Text(
                    text = "Create Short",
                    color = YtRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YtTextSecondary)
            }
        }
    )
}
