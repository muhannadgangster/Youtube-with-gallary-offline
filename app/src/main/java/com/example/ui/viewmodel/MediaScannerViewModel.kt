package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.VideoMetadata
import com.example.util.MediaScannerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ContentResolver-based media scanner service in a dedicated Kotlin ViewModel.
 * Locates .mp4, .mkv, .mov, .jpg, and .png files via ContentResolver and MediaStore,
 * and stores the metadata in a Room database to populate the Home feed.
 */
class MediaScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val dao = database.videoMetadataDao()
    private val contentResolver: ContentResolver = application.contentResolver

    // Reactive feed observed from Room Database to populate Home feed
    val mediaFeed: StateFlow<List<VideoMetadata>> = dao.getAllMetadata()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedMediaCount = MutableStateFlow(0)
    val scannedMediaCount: StateFlow<Int> = _scannedMediaCount.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready to scan media")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        // Trigger initial scan on startup
        scanDeviceMedia()
    }

    /**
     * Executes ContentResolver queries to locate .mp4, .mkv, .mov, .jpg, and .png files
     * and persist them into Room Database ('VideoMetadata').
     */
    fun scanDeviceMedia() {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _statusMessage.value = "Scanning local media via ContentResolver..."

            val discoveredItems = withContext(Dispatchers.IO) {
                val list = mutableListOf<VideoMetadata>()

                // 1. Locate .mp4, .mkv, and .mov video files via ContentResolver
                list.addAll(scanVideosFromContentResolver())

                // 2. Locate .jpg and .png photo files via ContentResolver
                list.addAll(scanPhotosFromContentResolver())

                list
            }

            // Persist to Room database preserving user custom titles and watch progress
            withContext(Dispatchers.IO) {
                if (discoveredItems.isNotEmpty()) {
                    val merged = discoveredItems.map { newItem ->
                        val existing = dao.getById(newItem.id)
                        if (existing != null) {
                            newItem.copy(
                                customTitle = existing.customTitle,
                                hashtags = existing.hashtags,
                                isLiked = existing.isLiked,
                                isDisliked = existing.isDisliked,
                                isSubscribed = existing.isSubscribed,
                                isWatchLater = existing.isWatchLater,
                                isDownloaded = existing.isDownloaded,
                                watchPositionMs = existing.watchPositionMs,
                                lastWatchedTimestamp = existing.lastWatchedTimestamp,
                                userNotes = existing.userNotes
                            )
                        } else {
                            newItem
                        }
                    }
                    dao.insertAll(merged)
                }
            }

            _scannedMediaCount.value = discoveredItems.size
            _statusMessage.value = "Scan complete. Found ${discoveredItems.size} local media items."
            _isScanning.value = false
        }
    }

    /**
     * Queries ContentResolver for .mp4, .mkv, and .mov video files.
     */
    private fun scanVideosFromContentResolver(): List<VideoMetadata> {
        val videoResults = mutableListOf<VideoMetadata>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        val supportedVideoExtensions = listOf(".mp4", ".mkv", ".mov", ".avi", ".webm")

        try {
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val name = c.getString(nameCol) ?: "Video_$id"
                    val filePath = c.getString(dataCol) ?: ""
                    val isSupported = supportedVideoExtensions.any { ext ->
                        name.endsWith(ext, ignoreCase = true) || filePath.endsWith(ext, ignoreCase = true)
                    } || name.startsWith("VID", ignoreCase = true)

                    if (!isSupported && filePath.isNotBlank() && !filePath.contains(".")) {
                        // Continue if not a known video
                        continue
                    }

                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val duration = c.getLong(durCol)
                    val size = c.getLong(sizeCol)
                    val dateAdded = c.getLong(dateCol) * 1000L
                    val width = if (widthCol >= 0) c.getInt(widthCol) else 1920
                    val height = if (heightCol >= 0) c.getInt(heightCol) else 1080

                    val parentFolder = try {
                        File(filePath).parentFile?.name ?: "Gallery Videos"
                    } catch (_: Exception) {
                        "Gallery Videos"
                    }

                    val (title, tags) = MediaScannerHelper.generateCatchyTitleAndHashtags(
                        rawName = name,
                        isPhotoSlideshow = false,
                        isShort = (height > width && height > 0) || (duration in 1..60000L),
                        dateAddedMs = if (dateAdded > 0) dateAdded else System.currentTimeMillis()
                    )

                    val metadata = VideoMetadata(
                        id = id.toString(),
                        uriString = contentUri.toString(),
                        filePath = filePath.ifBlank { contentUri.toString() },
                        originalTitle = title,
                        customTitle = null,
                        hashtags = tags,
                        channelName = parentFolder,
                        durationMs = if (duration > 0) duration else 60000L,
                        sizeBytes = size,
                        dateAdded = if (dateAdded > 0) dateAdded else System.currentTimeMillis(),
                        width = if (width > 0) width else 1920,
                        height = if (height > 0) height else 1080,
                        isPhotoSlideshow = false
                    )
                    videoResults.add(metadata)

                    // Automated 5-10s Shorts Highlight snippet for videos > 15s
                    if (duration > 15000L) {
                        val clipDuration = (8000L).coerceAtMost(duration)
                        val shortId = "short_clip_$id"
                        videoResults.add(
                            metadata.copy(
                                id = shortId,
                                originalTitle = "$title [5-10s Highlight]",
                                hashtags = "#Shorts #Highlights #Trending #LocalMedia",
                                durationMs = clipDuration,
                                width = 1080,
                                height = 1920,
                                clipStartMs = 0L,
                                clipEndMs = clipDuration
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MediaScannerVM", "ContentResolver error querying videos", e)
        }
        return videoResults
    }

    /**
     * Queries ContentResolver for .jpg and .png photo files and groups them into 5-10s stories.
     */
    private fun scanPhotosFromContentResolver(): List<VideoMetadata> {
        val photoResults = mutableListOf<VideoMetadata>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val supportedPhotoExtensions = listOf(".jpg", ".jpeg", ".png")

        try {
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )

            val photoList = mutableListOf<Triple<String, String, Long>>() // uri, album, date
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val bucketCol = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val dateCol = c.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                var count = 0
                while (c.moveToNext() && count < 60) {
                    val name = c.getString(nameCol) ?: ""
                    val path = c.getString(dataCol) ?: ""
                    val isPhoto = supportedPhotoExtensions.any { ext ->
                        name.endsWith(ext, ignoreCase = true) || path.endsWith(ext, ignoreCase = true)
                    } || name.startsWith("IMG", ignoreCase = true) || name.startsWith("PXL", ignoreCase = true)

                    if (isPhoto) {
                        val id = c.getLong(idCol)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        val album = if (bucketCol >= 0) c.getString(bucketCol) ?: "Gallery" else "Gallery"
                        val date = if (dateCol >= 0) c.getLong(dateCol) * 1000L else System.currentTimeMillis()
                        photoList.add(Triple(contentUri.toString(), album, date))
                        count++
                    }
                }
            }

            // Group into 3-5 photo clusters for Ken Burns animation clips
            if (photoList.isNotEmpty()) {
                val chunks = photoList.chunked(4)
                chunks.forEachIndexed { index, chunkPhotos ->
                    val firstPhoto = chunkPhotos.first()
                    val photoUrisString = chunkPhotos.joinToString(",") { it.first }
                    val albumName = firstPhoto.second
                    val slideshowId = "photo_slideshow_$index"

                    val (slideTitle, slideHashtags) = MediaScannerHelper.generateCatchyTitleAndHashtags(
                        rawName = "$albumName Photo Journey #Shorts",
                        isPhotoSlideshow = true,
                        isShort = true,
                        dateAddedMs = firstPhoto.third
                    )

                    val entity = VideoMetadata(
                        id = slideshowId,
                        uriString = firstPhoto.first,
                        filePath = firstPhoto.first,
                        originalTitle = slideTitle,
                        customTitle = null,
                        hashtags = slideHashtags,
                        channelName = "$albumName Showcase",
                        durationMs = (chunkPhotos.size * 2500L).coerceIn(5000L, 10000L), // 5-10 seconds
                        sizeBytes = 2 * 1024 * 1024L,
                        dateAdded = firstPhoto.third,
                        width = 1080,
                        height = 1920,
                        isPhotoSlideshow = true,
                        photoUrisString = photoUrisString,
                        clipStartMs = 0L,
                        clipEndMs = 8000L
                    )
                    photoResults.add(entity)
                }
            }
        } catch (e: Exception) {
            Log.e("MediaScannerVM", "ContentResolver error querying photos", e)
        }
        return photoResults
    }
}
