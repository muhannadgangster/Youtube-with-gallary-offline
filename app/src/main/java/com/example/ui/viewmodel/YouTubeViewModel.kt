package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsManager
import com.example.data.VideoItem
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class YouTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoRepository(application)
    val settingsManager = SettingsManager(application)

    val allVideos: StateFlow<List<VideoItem>> = repository.allVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortsVideos: StateFlow<List<VideoItem>> = repository.shortsVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscribedVideos: StateFlow<List<VideoItem>> = repository.subscribedVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedVideos: StateFlow<List<VideoItem>> = repository.likedVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchLaterVideos: StateFlow<List<VideoItem>> = repository.watchLaterVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedVideos: StateFlow<List<VideoItem>> = repository.downloadedVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyVideos: StateFlow<List<VideoItem>> = repository.historyVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation & Tab selection
    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Shorts, 2: Add, 3: Subscriptions, 4: You
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Filter Chips
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Search
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Currently playing video (if null, player is closed / showing feed)
    private val _currentlyPlaying = MutableStateFlow<VideoItem?>(null)
    val currentlyPlaying: StateFlow<VideoItem?> = _currentlyPlaying.asStateFlow()

    // Renaming dialog state (Custom Title & Hashtags feature via '+' icon)
    private val _editingVideo = MutableStateFlow<VideoItem?>(null)
    val editingVideo: StateFlow<VideoItem?> = _editingVideo.asStateFlow()

    // Plus (+) bottom sheet
    private val _showPlusSheet = MutableStateFlow(false)
    val showPlusSheet: StateFlow<Boolean> = _showPlusSheet.asStateFlow()

    // Comments / Notes Sheet
    private val _commentingVideo = MutableStateFlow<VideoItem?>(null)
    val commentingVideo: StateFlow<VideoItem?> = _commentingVideo.asStateFlow()

    // Notification sheet
    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    // Full Settings Screen Navigation
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Dedicated Downloads Screen Navigation (Screenshot 4)
    private val _showDownloadsScreen = MutableStateFlow(false)
    val showDownloadsScreen: StateFlow<Boolean> = _showDownloadsScreen.asStateFlow()

    // YouTube Shorts Camera UI (Screenshot 2)
    private val _showShortsCamera = MutableStateFlow(false)
    val showShortsCamera: StateFlow<Boolean> = _showShortsCamera.asStateFlow()

    // Filtered videos based on search & category chip
    val filteredVideos: StateFlow<List<VideoItem>> = combine(
        allVideos,
        _selectedFilter,
        _searchQuery
    ) { videos, filter, query ->
        var result = videos
        if (query.isNotBlank()) {
            result = result.filter {
                it.displayTitle.contains(query, ignoreCase = true) ||
                        it.displayChannelName.contains(query, ignoreCase = true) ||
                        it.hashtags.contains(query, ignoreCase = true)
            }
        } else {
            result = when (filter) {
                "Shorts" -> result.filter { it.isShort }
                "Videos" -> result.filter { !it.isShort }
                "Downloaded" -> result.filter { it.isDownloaded }
                "Favorites" -> result.filter { it.isLiked || it.isSubscribed }
                else -> result
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        scanStorage()
    }

    fun scanStorage() {
        viewModelScope.launch {
            repository.scanStorage()
        }
    }

    fun selectTab(index: Int) {
        if (index == 2) {
            _showPlusSheet.value = true
        } else {
            _selectedTab.value = index
        }
    }

    fun selectFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playVideo(video: VideoItem) {
        _currentlyPlaying.value = video
        if (!settingsManager.pauseHistory.value) {
            viewModelScope.launch {
                repository.recordWatchProgress(video.id, video.watchPositionMs.coerceAtLeast(1000L))
            }
        }
    }

    fun closePlayer() {
        _currentlyPlaying.value = null
    }

    fun openRenameDialog(video: VideoItem) {
        _editingVideo.value = video
    }

    fun dismissRenameDialog() {
        _editingVideo.value = null
    }

    fun saveCustomTitle(newTitle: String) {
        val video = _editingVideo.value ?: return
        saveCustomTitleAndHashtags(newTitle, video.hashtags)
    }

    fun saveCustomTitleAndHashtags(newTitle: String, newHashtags: String) {
        val video = _editingVideo.value ?: return
        viewModelScope.launch {
            repository.setCustomTitleAndHashtags(video.id, newTitle.trim(), newHashtags.trim())
            if (_currentlyPlaying.value?.id == video.id) {
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(
                    customTitle = newTitle.trim(),
                    hashtags = newHashtags.trim()
                )
            }
            _editingVideo.value = null
        }
    }

    fun toggleLike(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleLike(video.id)
            if (_currentlyPlaying.value?.id == video.id) {
                val wasLiked = _currentlyPlaying.value?.isLiked ?: false
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(
                    isLiked = !wasLiked,
                    isDisliked = false
                )
            }
        }
    }

    fun toggleDislike(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleDislike(video.id)
            if (_currentlyPlaying.value?.id == video.id) {
                val wasDisliked = _currentlyPlaying.value?.isDisliked ?: false
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(
                    isDisliked = !wasDisliked,
                    isLiked = false
                )
            }
        }
    }

    fun toggleSubscribe(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleSubscription(video.id)
            if (_currentlyPlaying.value?.id == video.id) {
                val wasSubscribed = _currentlyPlaying.value?.isSubscribed ?: false
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(isSubscribed = !wasSubscribed)
            }
        }
    }

    fun toggleWatchLater(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleWatchLater(video.id)
            if (_currentlyPlaying.value?.id == video.id) {
                val wasWL = _currentlyPlaying.value?.isWatchLater ?: false
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(isWatchLater = !wasWL)
            }
        }
    }

    fun toggleDownloaded(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleDownloaded(video.id)
            if (_currentlyPlaying.value?.id == video.id) {
                val wasDl = _currentlyPlaying.value?.isDownloaded ?: false
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(isDownloaded = !wasDl)
            }
        }
    }

    fun recordProgress(video: VideoItem, positionMs: Long) {
        if (!settingsManager.pauseHistory.value) {
            viewModelScope.launch {
                repository.recordWatchProgress(video.id, positionMs)
            }
        }
    }

    fun openComments(video: VideoItem) {
        _commentingVideo.value = video
    }

    fun closeComments() {
        _commentingVideo.value = null
    }

    fun saveComment(commentText: String) {
        val video = _commentingVideo.value ?: return
        viewModelScope.launch {
            repository.saveNotes(video.id, commentText)
            if (_currentlyPlaying.value?.id == video.id) {
                _currentlyPlaying.value = _currentlyPlaying.value?.copy(userNotes = commentText)
            }
            _commentingVideo.value = null
        }
    }

    fun setPlusSheetOpen(open: Boolean) {
        _showPlusSheet.value = open
    }

    fun setNotificationsSheetOpen(open: Boolean) {
        _showNotificationsSheet.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        _showSettings.value = open
    }

    fun setDownloadsScreenOpen(open: Boolean) {
        _showDownloadsScreen.value = open
    }

    fun setShortsCameraOpen(open: Boolean) {
        _showShortsCamera.value = open
    }

    fun importGalleryVideo(uri: Uri, customTitle: String? = null, hashtags: String? = null) {
        viewModelScope.launch {
            val imported = repository.importVideo(uri, customTitle, hashtags)
            _showPlusSheet.value = false
            if (imported != null) {
                playVideo(imported)
            }
        }
    }

    fun importShortMedia(uri: Uri) {
        viewModelScope.launch {
            val imported = repository.importVideo(
                uri = uri,
                customTitle = null,
                hashtags = "#Shorts #Trending #AutoClip #Gallery"
            )
            _showPlusSheet.value = false
            selectTab(1) // Jump straight to Shorts
        }
    }

    fun createPhotoSlideshow(
        photoUris: List<Uri>,
        title: String,
        hashtags: String,
        audioUri: Uri? = null,
        durationSeconds: Int = 8
    ) {
        viewModelScope.launch {
            val slideshow = repository.createPhotoSlideshow(
                photoUris = photoUris,
                title = title,
                hashtags = hashtags,
                audioUri = audioUri,
                durationSeconds = durationSeconds
            )
            _showPlusSheet.value = false
            selectTab(1) // Navigate to Shorts feed
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    fun deleteAllDownloads() {
        viewModelScope.launch {
            repository.deleteAllDownloads()
        }
    }
}
