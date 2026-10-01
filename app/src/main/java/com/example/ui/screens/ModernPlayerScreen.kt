package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.VideoItem
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.YtAvatarPurple
import com.example.ui.theme.YtAvatarTeal
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtPillActive
import com.example.ui.theme.YtPillActiveText
import com.example.ui.theme.YtPillBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceDark
import com.example.ui.theme.YtSurfaceHigher
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPlayerScreen(
    video: VideoItem,
    upNextVideos: List<VideoItem>,
    onClose: () -> Unit,
    onEditTitleClick: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onToggleDownload: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onSelectUpNext: (VideoItem) -> Unit,
    onProgressUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(video.watchPositionMs) }
    var totalDurationMs by remember { mutableLongStateOf(video.durationMs.coerceAtLeast(60000L)) }
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Double tap feedback
    var skipFeedbackText by remember { mutableStateOf<String?>(null) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Handle back press
    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onProgressUpdate(currentPositionMs)
            onClose()
        }
    }

    // Timer to update current position
    LaunchedEffect(isPlaying, video.id) {
        while (true) {
            delay(500)
            if (isPlaying) {
                if (videoViewRef != null && mediaPlayerRef != null) {
                    try {
                        val pos = videoViewRef!!.currentPosition.toLong()
                        if (pos > 0) {
                            currentPositionMs = pos
                            val dur = videoViewRef!!.duration.toLong()
                            if (dur > 0) totalDurationMs = dur
                        }
                    } catch (_: Exception) {}
                } else if (video.id.startsWith("sample_")) {
                    currentPositionMs = (currentPositionMs + 500).let {
                        if (it >= totalDurationMs) 0L else it
                    }
                }
            }
        }
    }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls && isPlaying && !isLocked) {
            delay(3500)
            showControls = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        // Top 16:9 Video Player Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFullscreen) Modifier.fillMaxSize()
                    else Modifier
                        .statusBarsPadding()
                        .aspectRatio(16f / 9f)
                )
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        },
                        onDoubleTap = { offset ->
                            val isRightSide = offset.x > size.width / 2
                            if (isRightSide) {
                                // Skip forward 10s
                                val newPos = (currentPositionMs + 10000).coerceAtMost(totalDurationMs)
                                currentPositionMs = newPos
                                try { videoViewRef?.seekTo(newPos.toInt()) } catch (_: Exception) {}
                                skipFeedbackText = "+10s"
                            } else {
                                // Skip backward 10s
                                val newPos = (currentPositionMs - 10000).coerceAtLeast(0L)
                                currentPositionMs = newPos
                                try { videoViewRef?.seekTo(newPos.toInt()) } catch (_: Exception) {}
                                skipFeedbackText = "-10s"
                            }
                        }
                    )
                }
                .testTag("modern_video_player_box")
        ) {
            // AndroidView wrapping native VideoView
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        try {
                            setOnErrorListener { _, _, _ -> true }
                            if (!video.id.startsWith("sample_")) {
                                setVideoURI(Uri.parse(video.uriString))
                            }
                            setOnPreparedListener { mp ->
                                mediaPlayerRef = mp
                                mp.isLooping = true
                                val dur = mp.duration.toLong()
                                if (dur > 0) totalDurationMs = dur
                                if (video.watchPositionMs > 1000L) {
                                    seekTo(video.watchPositionMs.toInt())
                                }
                                if (isPlaying) start()
                            }
                        } catch (_: Exception) {}
                        videoViewRef = this
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                },
                modifier = Modifier.fillMaxSize()
            )

            // Backdrop poster if sample or video not yet rendered
            if (mediaPlayerRef == null || video.id.startsWith("sample_")) {
                VideoThumbnailView(
                    video = video,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Double Tap Ripple Animation Indicator
            LaunchedEffect(skipFeedbackText) {
                if (skipFeedbackText != null) {
                    delay(800)
                    skipFeedbackText = null
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = skipFeedbackText != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = skipFeedbackText ?: "",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x77000000))
                ) {
                    // Top Player Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onProgressUpdate(currentPositionMs)
                                onClose()
                            },
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Minimize player",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Lock Button
                        IconButton(
                            onClick = { isLocked = !isLocked },
                            modifier = Modifier.testTag("player_lock_button")
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock controls",
                                tint = if (isLocked) YtRed else Color.White
                            )
                        }

                        // Speed Button
                        Box {
                            IconButton(onClick = { showSpeedMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Playback speed",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(YtSurfaceDark)
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${speed}x",
                                                color = if (playbackSpeed == speed) YtRed else YtTextPrimary
                                            )
                                        },
                                        onClick = {
                                            playbackSpeed = speed
                                            try {
                                                mediaPlayerRef?.playbackParams =
                                                    mediaPlayerRef?.playbackParams?.setSpeed(speed) ?: android.media.PlaybackParams().setSpeed(speed)
                                            } catch (_: Exception) {}
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Fullscreen Toggle
                        IconButton(
                            onClick = { isFullscreen = !isFullscreen },
                            modifier = Modifier.testTag("player_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White
                            )
                        }
                    }

                    if (!isLocked) {
                        // Center Play / Pause and 10s Skip Buttons
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(36.dp)
                        ) {
                            // -10s
                            IconButton(
                                onClick = {
                                    val newPos = (currentPositionMs - 10000).coerceAtLeast(0L)
                                    currentPositionMs = newPos
                                    try { videoViewRef?.seekTo(newPos.toInt()) } catch (_: Exception) {}
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Play/Pause
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                                    .clickable {
                                        isPlaying = !isPlaying
                                        if (isPlaying) {
                                            videoViewRef?.start()
                                        } else {
                                            videoViewRef?.pause()
                                        }
                                    }
                                    .testTag("player_play_pause_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // +10s
                            IconButton(
                                onClick = {
                                    val newPos = (currentPositionMs + 10000).coerceAtMost(totalDurationMs)
                                    currentPositionMs = newPos
                                    try { videoViewRef?.seekTo(newPos.toInt()) } catch (_: Exception) {}
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Bottom Player Bar: Time and Seekbar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${formatTime(currentPositionMs)} / ${formatTime(totalDurationMs)}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (playbackSpeed != 1.0f) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = YtRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Slider(
                                value = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                                onValueChange = { frac ->
                                    val target = (frac * totalDurationMs).toLong()
                                    currentPositionMs = target
                                    try { videoViewRef?.seekTo(target.toInt()) } catch (_: Exception) {}
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = YtRed,
                                    activeTrackColor = YtRed,
                                    inactiveTrackColor = Color(0x66FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .testTag("player_seekbar")
                            )
                        }
                    }
                }
            }
        }

        // When fullscreen, don't show the bottom scroll content
        if (!isFullscreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("player_details_column"),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
            ) {
                // Video Title with '+' / Edit Icon right next to it! (Screenshot 3 & Requirement 3)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = video.displayTitle,
                            color = YtTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Custom Title Edit / Plus button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(YtSurfaceVariant)
                                .clickable { onEditTitleClick() }
                                .testTag("player_edit_title_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Set Custom Title",
                                tint = YtTextPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Subtitle Line (Screenshot 2: "@Channel • 25k likes • 979k views • 1 day ago • #prank ...more")
                item {
                    Text(
                        text = "@${video.displayChannelName.replace(" ", "")} • 25k likes • ${video.viewsCount} • ${video.uploadedAgo} • #offline ...more",
                        color = YtTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Channel Row with Subscribe Pill Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(YtAvatarPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = video.displayChannelName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = video.displayChannelName,
                                    color = YtTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = YtTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "128K subscribers",
                                color = YtTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Subscribe Pill (Screenshot 2 style)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (video.isSubscribed) YtSurfaceVariant else YtPillActive)
                                .clickable { onToggleSubscribe() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("player_subscribe_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (video.isSubscribed) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = YtTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Subscribed",
                                    color = YtTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Subscribe",
                                    color = YtPillActiveText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Modern Pill Action Buttons Row (Like/Dislike, Share, Download, Clip, More)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Combined Like / Dislike Pill (Screenshot 2)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(YtSurfaceVariant)
                                .height(36.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onToggleLike() }
                                    .testTag("player_like_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (video.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (video.isLiked) YtRed else YtTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (video.isLiked) "Liked" else "25K",
                                    color = YtTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(YtBorder)
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .clickable { onToggleDislike() }
                                    .testTag("player_dislike_button")
                            ) {
                                Icon(
                                    imageVector = if (video.isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike",
                                    tint = YtTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 2. Share Pill
                        PlayerActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            testTag = "player_share_button",
                            onClick = onShare
                        )

                        // 3. Download / Saved Pill
                        PlayerActionButton(
                            icon = Icons.Default.Download,
                            label = if (video.isDownloaded) "Downloaded" else "Download",
                            active = video.isDownloaded,
                            testTag = "player_download_button",
                            onClick = onToggleDownload
                        )

                        // 4. Clip Pill
                        PlayerActionButton(
                            icon = Icons.Default.ContentCut,
                            label = "Clip",
                            testTag = "player_clip_button",
                            onClick = { /* Clip */ }
                        )

                        // 5. 3-dots Pill
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(YtSurfaceVariant)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = YtTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Comments Preview Card (Screenshot 2)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(YtSurfaceVariant)
                            .clickable { onOpenComments() }
                            .padding(12.dp)
                            .testTag("player_comments_card")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comments",
                                    color = YtTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "8.1k",
                                    color = YtTextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("B", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (video.userNotes.isNotBlank()) video.userNotes else "Vote for moni di bf face reveal 💖",
                                    color = YtTextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // "Up Next" Header
                item {
                    Text(
                        text = "Up Next",
                        color = YtTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                // Up Next Recommendations List
                items(upNextVideos.filter { it.id != video.id }, key = { it.id }) { nextVideo ->
                    UpNextVideoCard(
                        video = nextVideo,
                        onClick = { onSelectUpNext(nextVideo) }
                    )
                }
            }
        }
    }

    DisposableEffect(video.id) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }
}

@Composable
private fun PlayerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Color(0xFF383838) else YtSurfaceVariant)
            .height(36.dp)
            .clickable { onClick() }
            .padding(horizontal = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) YtRed else YtTextPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = YtTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun UpNextVideoCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("up_next_card_${video.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Thumbnail 16:9
        Box(
            modifier = Modifier
                .width(130.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            VideoThumbnailView(
                video = video,
                modifier = Modifier.matchParentSize()
            )

            // Duration badge overlay at bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.displayTitle,
                color = YtTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${video.displayChannelName} • ${video.viewsCount}",
                color = YtTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = YtTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSec = millis / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val hr = min / 60
    return if (hr > 0) {
        String.format("%d:%02d:%02d", hr, min % 60, sec)
    } else {
        String.format("%d:%02d", min, sec)
    }
}
