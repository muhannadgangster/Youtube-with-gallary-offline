package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_metadata")
data class VideoMetadata(
    @PrimaryKey
    val id: String, // filePath or content URI string
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
    val isPhotoSlideshow: Boolean = false,
    val photoUrisString: String = "", // Comma-separated URIs
    val clipStartMs: Long = 0L,
    val clipEndMs: Long = 0L,
    val audioUriString: String = ""
)

// Backward compatibility alias
typealias VideoEntity = VideoMetadata
