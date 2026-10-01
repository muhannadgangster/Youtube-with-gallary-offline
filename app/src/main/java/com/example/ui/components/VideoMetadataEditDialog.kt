package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.data.local.AppDatabase
import com.example.data.local.VideoMetadata
import com.example.ui.theme.YtBlue
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Reusable AlertDialog component that binds directly to the VideoMetadata Room entity
 * to allow users to edit and persist custom video titles and hashtag strings directly from the feed.
 */
@Composable
fun VideoMetadataEditDialog(
    metadata: VideoMetadata,
    onDismiss: () -> Unit,
    onSaved: ((VideoMetadata) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getInstance(context).videoMetadataDao() }

    var titleInput by remember { mutableStateOf(metadata.customTitle ?: metadata.originalTitle) }
    var hashtagsInput by remember { mutableStateOf(metadata.hashtags.ifBlank { "#Shorts #Trending #LocalMedia" }) }
    var isSaving by remember { mutableStateOf(false) }

    val suggestedHashtags = listOf(
        "#Shorts", "#Trending", "#Viral", "#Vlog", "#LocalMedia",
        "#Gallery", "#PhotoJourney", "#Cinematic", "#Memories", "#FYP"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = YtSurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Video Metadata",
                    color = YtTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Quick reset to original title button
                if (titleInput != metadata.originalTitle) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { titleInput = metadata.originalTitle }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Title",
                            tint = YtTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            color = YtTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Update the title and hashtags for this item. Persists immediately into the Room database across sessions.",
                    color = YtTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Video Title input
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Video Title") },
                    supportingText = {
                        Text(
                            text = "${titleInput.length}/100",
                            color = YtTextSecondary,
                            fontSize = 11.sp
                        )
                    },
                    singleLine = false,
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YtBlue,
                        unfocusedBorderColor = YtBorder,
                        focusedTextColor = YtTextPrimary,
                        unfocusedTextColor = YtTextPrimary,
                        focusedLabelColor = YtBlue,
                        unfocusedLabelColor = YtTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("metadata_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hashtags input
                OutlinedTextField(
                    value = hashtagsInput,
                    onValueChange = { hashtagsInput = it },
                    label = { Text("Hashtags (#Shorts #Trending ...)") },
                    singleLine = false,
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YtBlue,
                        unfocusedBorderColor = YtBorder,
                        focusedTextColor = YtTextPrimary,
                        unfocusedTextColor = YtTextPrimary,
                        focusedLabelColor = YtBlue,
                        unfocusedLabelColor = YtTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("metadata_hashtags_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Popular Tag Suggestions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestedHashtags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(YtSurfaceVariant)
                                .clickable {
                                    if (!hashtagsInput.contains(tag, ignoreCase = true)) {
                                        hashtagsInput = if (hashtagsInput.isBlank()) tag else "$hashtagsInput $tag"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                color = YtTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving && titleInput.isNotBlank(),
                onClick = {
                    isSaving = true
                    coroutineScope.launch {
                        val cleanedTitle = titleInput.trim()
                        val cleanedTags = hashtagsInput.trim()

                        withContext(Dispatchers.IO) {
                            dao.updateCustomTitleAndHashtags(
                                id = metadata.id,
                                newTitle = cleanedTitle,
                                newHashtags = cleanedTags
                            )
                        }

                        val updated = metadata.copy(
                            customTitle = cleanedTitle,
                            hashtags = cleanedTags
                        )
                        onSaved?.invoke(updated)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("metadata_save_button")
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save Changes",
                    color = YtBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("metadata_cancel_button")
            ) {
                Text(
                    text = "Cancel",
                    color = YtTextSecondary
                )
            }
        }
    )
}

/**
 * Adapter overload for VideoItem interoperability
 */
@Composable
fun VideoMetadataEditDialog(
    video: VideoItem,
    onDismiss: () -> Unit,
    onSave: (newTitle: String, newHashtags: String) -> Unit
) {
    val metadata = remember(video) {
        VideoMetadata(
            id = video.id,
            uriString = video.uriString,
            filePath = video.filePath,
            originalTitle = video.originalTitle,
            customTitle = video.customTitle,
            hashtags = video.hashtags,
            channelName = video.channelName,
            customChannelName = video.customChannelName,
            durationMs = video.durationMs,
            sizeBytes = video.sizeBytes,
            dateAdded = video.dateAdded,
            width = video.width,
            height = video.height,
            isLiked = video.isLiked,
            isDisliked = video.isDisliked,
            isSubscribed = video.isSubscribed,
            isWatchLater = video.isWatchLater,
            isDownloaded = video.isDownloaded,
            watchPositionMs = video.watchPositionMs,
            lastWatchedTimestamp = video.lastWatchedTimestamp,
            userNotes = video.userNotes,
            isPhotoSlideshow = video.isPhotoSlideshow,
            photoUrisString = video.photoUrisString,
            clipStartMs = video.clipStartMs,
            clipEndMs = video.clipEndMs,
            audioUriString = video.audioUriString
        )
    }

    VideoMetadataEditDialog(
        metadata = metadata,
        onDismiss = onDismiss,
        onSaved = { updated ->
            onSave(updated.customTitle ?: updated.originalTitle, updated.hashtags)
        }
    )
}
