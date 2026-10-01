package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoItem
import com.example.util.ThumbnailHelper
import kotlin.math.abs

@Composable
fun VideoThumbnailView(
    video: VideoItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var bitmap by remember(video.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(video.id) {
        val uri = try {
            Uri.parse(video.uriString)
        } catch (_: Exception) {
            null
        }
        if (uri != null) {
            bitmap = ThumbnailHelper.getVideoThumbnail(context, uri, video.id)
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = video.displayTitle,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant gradient procedural thumbnail
            val hash = abs(video.id.hashCode() + video.displayTitle.hashCode())
            val paletteIndex = hash % 5
            val brush = when (paletteIndex) {
                0 -> Brush.linearGradient(listOf(Color(0xFF8E0E00), Color(0xFF1F1C18)))
                1 -> Brush.linearGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
                2 -> Brush.linearGradient(listOf(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFFAF7B)))
                3 -> Brush.linearGradient(listOf(Color(0xFF134E5E), Color(0xFF71B280)))
                else -> Brush.linearGradient(listOf(Color(0xFF2C3E50), Color(0xFF000000)))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )

            // Center play badge watermark
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
