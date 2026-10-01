package com.example.data

data class VideoItem(
    val id: String,
    val uriString: String,
    val filePath: String,
    val originalTitle: String,
    val customTitle: String? = null,
    val hashtags: String = "#Shorts #Trending #LocalMedia",
    val channelName: String = "Local Channel",
    val customChannelName: String? = null,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val width: Int = 1920,
    val height: Int = 1080,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSubscribed: Boolean = false,
    val isWatchLater: Boolean = false,
    val isDownloaded: Boolean = false,
    val watchPositionMs: Long = 0L,
    val lastWatchedTimestamp: Long = 0L,
    val userNotes: String = "",
    val viewsCount: String = "1.2M views",
    val uploadedAgo: String = "2 days ago",
    val isShort: Boolean = false,
    val isPhotoSlideshow: Boolean = false,
    val photoUris: List<String> = emptyList(),
    val clipStartMs: Long = 0L,
    val clipEndMs: Long = 0L,
    val audioUriString: String = ""
) {
    val displayTitle: String
        get() = if (!customTitle.isNullOrBlank()) customTitle else originalTitle

    val displayChannelName: String
        get() = if (!customChannelName.isNullOrBlank()) customChannelName else channelName

    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "0:00"
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hours = minutes / 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }

    val watchProgressFraction: Float
        get() {
            if (durationMs <= 0 || watchPositionMs <= 0) return 0f
            return (watchPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }
}
