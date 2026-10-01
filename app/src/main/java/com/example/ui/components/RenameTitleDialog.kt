package com.example.ui.components

import androidx.compose.runtime.Composable
import com.example.data.VideoItem

/**
 * Legacy RenameTitleDialog alias that forwards to the reusable VideoMetadataEditDialog
 */
@Composable
fun RenameTitleDialog(
    video: VideoItem,
    onDismiss: () -> Unit,
    onSave: (newTitle: String, newHashtags: String) -> Unit
) {
    VideoMetadataEditDialog(
        video = video,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
