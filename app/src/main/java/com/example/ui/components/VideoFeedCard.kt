package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.theme.YtAvatarIndigo
import com.example.ui.theme.YtAvatarOrange
import com.example.ui.theme.YtAvatarPurple
import com.example.ui.theme.YtAvatarTeal
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary
import kotlin.math.abs

@Composable
fun VideoFeedCard(
    video: VideoItem,
    onClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onToggleWatchLater: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onToggleDownload: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = 16.dp)
            .testTag("video_feed_card_${video.id}")
    ) {
        // 16:9 Thumbnail container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .aspectRatio(16f / 9f)
        ) {
            VideoThumbnailView(
                video = video,
                modifier = Modifier.matchParentSize()
            )

            // Duration badge overlay at bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Watch Progress Bar if partially watched
            if (video.watchProgressFraction > 0.02f) {
                LinearProgressIndicator(
                    progress = { video.watchProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = YtRed,
                    trackColor = Color(0x66000000)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Details Row: Avatar + Title + Metadata + 3-dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar Circle
            val avatarColors = listOf(YtAvatarPurple, YtAvatarTeal, YtAvatarOrange, YtAvatarIndigo)
            val colorIndex = abs(video.displayChannelName.hashCode()) % avatarColors.size
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarColors[colorIndex]),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = video.displayChannelName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Channel Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title Row with '+' / Edit Icon next to title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.displayTitle,
                        color = YtTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Small '+' / Pencil icon for custom title renaming
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(YtSurfaceVariant)
                            .clickable { onEditTitleClick() }
                            .testTag("edit_title_button_${video.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Set Custom Title",
                            tint = YtTextPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Channel Name + Verified Badge + Views + Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.displayChannelName,
                        color = YtTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Channel",
                        tint = YtTextSecondary,
                        modifier = Modifier.size(11.dp)
                    )

                    Text(
                        text = " • ${video.viewsCount} • ${video.uploadedAgo}",
                        color = YtTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // 3-dots Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("menu_button_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Video options",
                        tint = YtTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(YtSurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Set Custom Title", color = YtTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = YtTextPrimary)
                        },
                        onClick = {
                            showMenu = false
                            onEditTitleClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (video.isWatchLater) "Remove from Watch Later" else "Save to Watch Later",
                                color = YtTextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.WatchLater, contentDescription = null, tint = YtTextPrimary)
                        },
                        onClick = {
                            showMenu = false
                            onToggleWatchLater()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (video.isSubscribed) "Unsubscribe Channel" else "Subscribe / Favorite Channel",
                                color = YtTextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, tint = YtTextPrimary)
                        },
                        onClick = {
                            showMenu = false
                            onToggleSubscribe()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (video.isDownloaded) "Remove Download" else "Save / Download Offline",
                                color = YtTextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Download, contentDescription = null, tint = YtTextPrimary)
                        },
                        onClick = {
                            showMenu = false
                            onToggleDownload()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Video", color = YtTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Share, contentDescription = null, tint = YtTextPrimary)
                        },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        }
                    )
                }
            }
        }
    }
}
