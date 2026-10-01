package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusModalSheet(
    onDismiss: () -> Unit,
    onVideoSelected: (Uri) -> Unit,
    onShortMediaSelected: (Uri) -> Unit,
    onPhotosSelected: (List<Uri>) -> Unit,
    onScanStorage: () -> Unit,
    onCreateShortsClip: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Picker for "Upload a video"
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onVideoSelected(uri)
        }
    }

    // Picker for "Create a Short"
    val shortMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onShortMediaSelected(uri)
        }
    }

    // Picker for "Photo to Video Story"
    val photoSlideshowLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onPhotosSelected(uris)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = YtSurfaceDark,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create & Import",
                    color = YtTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = YtTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Option 1: Create a Short
            PlusActionItem(
                icon = Icons.Default.Movie,
                title = "Create a Short",
                subtitle = "Pick gallery media -> Auto-crops 5-10s clip into Shorts feed",
                iconBackground = YtRed,
                testTag = "plus_create_short",
                onClick = {
                    onDismiss()
                    shortMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            // Option 2: Upload a video
            PlusActionItem(
                icon = Icons.Default.UploadFile,
                title = "Upload a video",
                subtitle = "Pick any gallery video (.mp4, .mkv) with auto-generated hashtags",
                iconBackground = Color(0xFF1E88E5),
                testTag = "plus_import_gallery",
                onClick = {
                    onDismiss()
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                }
            )

            // Option 3: Photo to Video Story (Ken Burns animation)
            PlusActionItem(
                icon = Icons.Default.Collections,
                title = "Photo to Video Story",
                subtitle = "Pick multiple gallery photos -> Ken Burns zoom/pan animated slideshow",
                iconBackground = Color(0xFF8E24AA),
                testTag = "plus_photo_slideshow",
                onClick = {
                    onDismiss()
                    photoSlideshowLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            // Option 4: Create Clip from existing library
            PlusActionItem(
                icon = Icons.Default.ContentCut,
                title = "Create Clip",
                subtitle = "Select start & end timestamps (5-10s) from any local video",
                iconBackground = Color(0xFFFB8C00),
                testTag = "plus_create_clip",
                onClick = {
                    onDismiss()
                    onCreateShortsClip()
                }
            )

            // Option 5: Scan Device Storage
            PlusActionItem(
                icon = Icons.Default.Refresh,
                title = "Scan Device Storage",
                subtitle = "Re-scan Movies, DCIM, Camera & Downloads for new files",
                iconBackground = Color(0xFF43A047),
                testTag = "plus_scan_storage",
                onClick = {
                    onScanStorage()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlusActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBackground: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = YtTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = YtTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
