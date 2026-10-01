package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoMetadataDao {
    @Query("SELECT * FROM video_metadata ORDER BY dateAdded DESC")
    fun getAllMetadata(): Flow<List<VideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE isSubscribed = 1 ORDER BY dateAdded DESC")
    fun getSubscribed(): Flow<List<VideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE isLiked = 1 ORDER BY dateAdded DESC")
    fun getLiked(): Flow<List<VideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE isWatchLater = 1 ORDER BY dateAdded DESC")
    fun getWatchLater(): Flow<List<VideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE isDownloaded = 1 ORDER BY dateAdded DESC")
    fun getDownloaded(): Flow<List<VideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE lastWatchedTimestamp > 0 ORDER BY lastWatchedTimestamp DESC LIMIT 30")
    fun getHistory(): Flow<List<VideoMetadata>>

    @Query("SELECT * FROM video_metadata WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VideoMetadata?

    @Query("SELECT COUNT(*) FROM video_metadata")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: VideoMetadata)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<VideoMetadata>)

    @Query("UPDATE video_metadata SET customTitle = :newTitle, hashtags = :newHashtags WHERE id = :id")
    suspend fun updateCustomTitleAndHashtags(id: String, newTitle: String, newHashtags: String)

    @Query("UPDATE video_metadata SET customTitle = :newTitle WHERE id = :id")
    suspend fun updateCustomTitle(id: String, newTitle: String)

    @Query("UPDATE video_metadata SET isLiked = :isLiked, isDisliked = :isDisliked WHERE id = :id")
    suspend fun updateLikeStatus(id: String, isLiked: Boolean, isDisliked: Boolean)

    @Query("UPDATE video_metadata SET isSubscribed = :isSubscribed WHERE id = :id")
    suspend fun updateSubscription(id: String, isSubscribed: Boolean)

    @Query("UPDATE video_metadata SET isWatchLater = :isWatchLater WHERE id = :id")
    suspend fun updateWatchLater(id: String, isWatchLater: Boolean)

    @Query("UPDATE video_metadata SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateDownloaded(id: String, isDownloaded: Boolean)

    @Query("UPDATE video_metadata SET watchPositionMs = :positionMs, lastWatchedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateWatchProgress(id: String, positionMs: Long, timestamp: Long)

    @Query("UPDATE video_metadata SET userNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String)

    @Query("UPDATE video_metadata SET watchPositionMs = 0, lastWatchedTimestamp = 0")
    suspend fun clearWatchHistory()

    @Query("UPDATE video_metadata SET isDownloaded = 0")
    suspend fun deleteAllDownloads()

    @Query("DELETE FROM video_metadata WHERE id = :id")
    suspend fun deleteById(id: String)
}

typealias VideoDao = VideoMetadataDao
