package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.VideoItem
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.YtAvatarTeal
import com.example.ui.theme.YtRed
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun ShortsScreen(
    shortsList: List<VideoItem>,
    onToggleLike: (VideoItem) -> Unit,
    onToggleDislike: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onOpenComments: (VideoItem) -> Unit,
    onEditTitle: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (shortsList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No shorts found. Tap '+' to create or scan gallery media!", color = Color.White)
        }
        return
    }

    // Unlimited infinite scrolling for YouTube Shorts feed
    val infinitePageCount = 1_000_000
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { infinitePageCount }
    )

    VerticalPager(
        state = pagerState,
        key = { page -> "short_page_${page}_${shortsList[page % shortsList.size].id}" },
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_vertical_pager")
    ) { page ->
        val video = shortsList[page % shortsList.size]
        val isCurrentPage = pagerState.currentPage == page

        ShortsItemPage(
            video = video,
            pageIndex = page,
            isActive = isCurrentPage,
            onToggleLike = { onToggleLike(video) },
            onToggleDislike = { onToggleDislike(video) },
            onToggleSubscribe = { onToggleSubscribe(video) },
            onOpenComments = { onOpenComments(video) },
            onEditTitle = { onEditTitle(video) },
            onShare = { onShare(video) }
        )
    }
}

@Composable
private fun ShortsItemPage(
    video: VideoItem,
    pageIndex: Int,
    isActive: Boolean,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onOpenComments: () -> Unit,
    onEditTitle: () -> Unit,
    onShare: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showPauseOverlay by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // Spinning disc animation for sound
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPlaying = !isPlaying
                if (video.isPhotoSlideshow) {
                    // Handled inside KenBurnsPhotoSlideshow via isPlaying
                } else {
                    if (isPlaying) {
                        videoViewRef?.start()
                    } else {
                        videoViewRef?.pause()
                    }
                }
                showPauseOverlay = true
            }
    ) {
        if (video.isPhotoSlideshow) {
            // PHOTO-TO-VIDEO ANIMATION ENGINE (Ken Burns zoom/pan effect)
            KenBurnsPhotoSlideshow(
                video = video,
                isActive = isActive,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // VIDEO PLAYER (Native VideoView with 5-10s Auto-Crop Loop)
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        try {
                            setOnErrorListener { _, _, _ -> true }
                            if (!video.id.startsWith("sample_")) {
                                setVideoURI(Uri.parse(video.uriString))
                            }
                            setOnPreparedListener { mp ->
                                mp.isLooping = (video.clipEndMs <= 0 || video.clipEndMs >= video.durationMs)
                                if (video.clipStartMs > 0) {
                                    seekTo(video.clipStartMs.toInt())
                                }
                                if (isActive && isPlaying) {
                                    start()
                                }
                            }
                        } catch (_: Exception) {}
                        videoViewRef = this
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                    if (isActive && isPlaying) {
                        if (!vv.isPlaying) {
                            try { vv.start() } catch (_: Exception) {}
                        }
                    } else {
                        if (vv.isPlaying) {
                            try { vv.pause() } catch (_: Exception) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loop 5-10s Auto-Cropped Highlight snippet
            if (video.clipEndMs > video.clipStartMs && video.clipEndMs < video.durationMs) {
                LaunchedEffect(isActive, isPlaying) {
                    while (isActive && isPlaying) {
                        delay(250)
                        val pos = videoViewRef?.currentPosition?.toLong() ?: 0L
                        if (pos >= video.clipEndMs) {
                            videoViewRef?.seekTo(video.clipStartMs.toInt())
                        }
                    }
                }
            }

            // Fallback poster when paused or sample
            if (!isPlaying || video.id.startsWith("sample_")) {
                VideoThumbnailView(
                    video = video,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Tap Play/Pause Feedback Overlay
        LaunchedEffect(showPauseOverlay) {
            if (showPauseOverlay) {
                delay(700)
                showPauseOverlay = false
            }
        }

        AnimatedVisibility(
            visible = showPauseOverlay,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Dark gradient overlay for bottom readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE000000))
                    )
                )
        )

        // Bottom Left Info: Channel, Title with '+' Edit button, Hashtags & Audio
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 80.dp)
        ) {
            // Channel row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(YtAvatarTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.displayChannelName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "@${video.displayChannelName.replace(" ", "")}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Subscribe Button Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (video.isSubscribed) Color(0x55FFFFFF) else YtRed)
                        .clickable { onToggleSubscribe() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("shorts_subscribe_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (video.isSubscribed) "Subscribed" else "Subscribe",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title Row with Working '+' / Edit Icon!
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = video.displayTitle,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Working '+' / Edit Icon for Title & Hashtags
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0x66FFFFFF))
                        .clickable { onEditTitle() }
                        .testTag("shorts_edit_title_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Set Custom Title & Hashtags",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Hashtags
            Text(
                text = video.hashtags,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sound Pill
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (video.isPhotoSlideshow) "Original audio - Photo Showcase Beat" else "Original audio - ${video.displayChannelName}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right Action Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 76.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            ShortsActionButton(
                icon = if (video.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                label = if (video.isLiked) "Liked" else "Like",
                tint = if (video.isLiked) YtRed else Color.White,
                testTag = "shorts_like_button",
                onClick = onToggleLike
            )

            // Dislike
            ShortsActionButton(
                icon = if (video.isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                label = "Dislike",
                tint = if (video.isDisliked) Color.White else Color.White.copy(alpha = 0.85f),
                testTag = "shorts_dislike_button",
                onClick = onToggleDislike
            )

            // Comments / Notes
            ShortsActionButton(
                icon = Icons.Default.Comment,
                label = if (video.userNotes.isNotBlank()) "1" else "Notes",
                tint = Color.White,
                testTag = "shorts_comment_button",
                onClick = onOpenComments
            )

            // Share
            ShortsActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = Color.White,
                testTag = "shorts_share_button",
                onClick = onShare
            )

            // Music Disc Spinning
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF333333), CircleShape)
                    .background(Color(0xFF1E1E1E))
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(YtRed)
                )
            }
        }
    }

    DisposableEffect(pageIndex, video.id) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }
}

/**
 * PHOTO ANIMATOR / SLIDESHOW ENGINE:
 * Automatically groups local photos and animates them with smooth Ken Burns zoom & pan transitions.
 */
@Composable
private fun KenBurnsPhotoSlideshow(
    video: VideoItem,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoList = remember(video.photoUris, video.uriString) {
        if (video.photoUris.isNotEmpty()) video.photoUris else listOf(video.uriString)
    }

    var currentPhotoIndex by remember { mutableIntStateOf(0) }

    // Cycle photos every 2.5 seconds when active and playing
    LaunchedEffect(isActive, isPlaying, photoList.size) {
        if (photoList.size > 1 && isActive && isPlaying) {
            while (true) {
                delay(2500)
                currentPhotoIndex = (currentPhotoIndex + 1) % photoList.size
            }
        }
    }

    // Ken Burns Zoom/Pan Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ken_burns_zoom")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ken_burns_scale"
    )

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Story progress indicators at top
        if (photoList.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                photoList.forEachIndexed { idx, _ ->
                    val progress = when {
                        idx < currentPhotoIndex -> 1f
                        idx == currentPhotoIndex -> 0.7f
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color(0x55FFFFFF)
                    )
                }
            }
        }

        // Active Photo with Ken Burns Effect
        val currentPhotoUri = photoList.getOrNull(currentPhotoIndex) ?: video.uriString

        AnimatedContent(
            targetState = currentPhotoUri,
            transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
            label = "photo_crossfade"
        ) { photoUri ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = video.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

@Composable
private fun ShortsActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x33000000)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
