package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.YtAvatarIndigo
import com.example.ui.theme.YtAvatarOrange
import com.example.ui.theme.YtAvatarPurple
import com.example.ui.theme.YtAvatarTeal
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtPillActive
import com.example.ui.theme.YtPillActiveText
import com.example.ui.theme.YtPillBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary
import kotlin.math.abs

@Composable
fun SubscriptionsScreen(
    subscribedVideos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onEditTitleClick: (VideoItem) -> Unit,
    onToggleWatchLater: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onToggleDownload: (VideoItem) -> Unit,
    onShareClick: (VideoItem) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val channels = subscribedVideos.map { it.displayChannelName }.distinct()
    val avatarColors = listOf(YtAvatarPurple, YtAvatarTeal, YtAvatarOrange, YtAvatarIndigo)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        if (channels.isNotEmpty()) {
            // Horizontal row of subscribed channels (circles with names)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                channels.forEach { channelName ->
                    val colorIndex = abs(channelName.hashCode()) % avatarColors.size
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(62.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(avatarColors[colorIndex]),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = channelName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = channelName,
                            color = YtTextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            HorizontalDivider(color = YtBorder, thickness = 0.5.dp)
        }

        if (subscribedVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Subscriptions,
                        contentDescription = null,
                        tint = YtTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Don't miss new videos",
                        color = YtTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Subscribe to channels from the Home or Player screen to see their local videos here.",
                        color = YtTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = YtPillActive),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Explore Videos", color = YtPillActiveText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("subscriptions_feed_list"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(subscribedVideos, key = { it.id }) { video ->
                    VideoFeedCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onEditTitleClick = { onEditTitleClick(video) },
                        onToggleWatchLater = { onToggleWatchLater(video) },
                        onToggleSubscribe = { onToggleSubscribe(video) },
                        onToggleDownload = { onToggleDownload(video) },
                        onShareClick = { onShareClick(video) }
                    )
                }
            }
        }
    }
}
