package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ThumbnailHelper {
    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(50) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    suspend fun getVideoThumbnail(context: Context, videoUri: Uri, videoId: String): Bitmap? =
        withContext(Dispatchers.IO) {
            memoryCache.get(videoId)?.let { return@withContext it }

            // Check file cache
            val cacheFile = File(context.cacheDir, "thumb_$videoId.jpg")
            if (cacheFile.exists()) {
                try {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(cacheFile.absolutePath)
                    if (bitmap != null) {
                        memoryCache.put(videoId, bitmap)
                        return@withContext bitmap
                    }
                } catch (_: Exception) {}
            }

            // If it is a sample placeholder or contains sample_media, skip MediaMetadataRetriever
            if (videoId.startsWith("sample_") || videoUri.toString().contains("sample_media")) {
                return@withContext null
            }

            var bitmap: Bitmap? = null
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        bitmap = context.contentResolver.loadThumbnail(videoUri, Size(512, 288), null)
                    } catch (_: Exception) {}
                }

                if (bitmap == null) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, videoUri)
                        val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                        if (hasVideo.equals("yes", ignoreCase = true)) {
                            bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        }
                    } catch (_: Exception) {
                    } finally {
                        try {
                            retriever.release()
                        } catch (_: Exception) {}
                    }
                }

                if (bitmap != null) {
                    memoryCache.put(videoId, bitmap)
                    try {
                        FileOutputStream(cacheFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            return@withContext bitmap
        }
}
