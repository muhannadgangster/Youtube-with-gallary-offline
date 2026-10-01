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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.ui.components.VideoFeedCard
import com.example.ui.theme.YtBorder
import com.example.ui.theme.YtDarkBackground
import com.example.ui.theme.YtPillActive
import com.example.ui.theme.YtPillActiveText
import com.example.ui.theme.YtPillBackground
import com.example.ui.theme.YtRed
import com.example.ui.theme.YtSurfaceVariant
import com.example.ui.theme.YtTextPrimary
import com.example.ui.theme.YtTextSecondary

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onEditTitleClick: (VideoItem) -> Unit,
    onToggleWatchLater: (VideoItem) -> Unit,
    onToggleSubscribe: (VideoItem) -> Unit,
    onToggleDownload: (VideoItem) -> Unit,
    onShareClick: (VideoItem) -> Unit,
    onScanStorageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filterChips = listOf("All", "Videos", "Shorts", "Downloaded", "Favorites")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        // Filter Chips Row (Screenshot 6 style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Explore Compass Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(YtSurfaceVariant)
                    .clickable { onFilterSelected("All") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("filter_chip_explore"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Explore",
                    tint = YtTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            filterChips.forEach { chip ->
                val isSelected = selectedFilter == chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) YtPillActive else YtPillBackground)
                        .clickable { onFilterSelected(chip) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("filter_chip_$chip"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chip,
                        color = if (isSelected) YtPillActiveText else YtTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        if (videos.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = YtTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No local videos found in this filter",
                        color = YtTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add videos using the (+) button or scan storage.",
                        color = YtTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onScanStorageClick,
                        colors = ButtonDefaults.buttonColors(containerColor = YtSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = YtTextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Storage", color = YtTextPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_videos_list"),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                items(videos, key = { it.id }) { video ->
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
