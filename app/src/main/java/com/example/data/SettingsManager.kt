package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsManager(context: Context) {
    val dataStore = SettingsDataStore(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("mytube_settings", Context.MODE_PRIVATE)

    // General
    private val _remindBreak = MutableStateFlow(prefs.getBoolean("remind_break", false))
    val remindBreak: StateFlow<Boolean> = _remindBreak.asStateFlow()

    private val _breakIntervalMinutes = MutableStateFlow(prefs.getInt("break_interval", 60))
    val breakIntervalMinutes: StateFlow<Int> = _breakIntervalMinutes.asStateFlow()

    private val _remindBedtime = MutableStateFlow(prefs.getBoolean("remind_bedtime", false))
    val remindBedtime: StateFlow<Boolean> = _remindBedtime.asStateFlow()

    private val _bedtimeHour = MutableStateFlow(prefs.getInt("bedtime_hour", 23))
    val bedtimeHour: StateFlow<Int> = _bedtimeHour.asStateFlow()

    private val _darkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _doubleTapSeekSeconds = MutableStateFlow(prefs.getInt("seek_duration", 10))
    val doubleTapSeekSeconds: StateFlow<Int> = _doubleTapSeekSeconds.asStateFlow()

    private val _zoomToFill = MutableStateFlow(prefs.getBoolean("zoom_to_fill", false))
    val zoomToFill: StateFlow<Boolean> = _zoomToFill.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "English (US)") ?: "English (US)")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Playback
    private val _autoplayNext = MutableStateFlow(prefs.getBoolean("autoplay_next", true))
    val autoplayNext: StateFlow<Boolean> = _autoplayNext.asStateFlow()

    private val _defaultSpeed = MutableStateFlow(prefs.getFloat("default_speed", 1.0f))
    val defaultSpeed: StateFlow<Float> = _defaultSpeed.asStateFlow()

    private val _videoQuality = MutableStateFlow(prefs.getString("video_quality", "Auto (recommended)") ?: "Auto (recommended)")
    val videoQuality: StateFlow<String> = _videoQuality.asStateFlow()

    private val _captionsEnabled = MutableStateFlow(prefs.getBoolean("captions_enabled", false))
    val captionsEnabled: StateFlow<Boolean> = _captionsEnabled.asStateFlow()

    private val _captionSize = MutableStateFlow(prefs.getString("caption_size", "Normal") ?: "Normal")
    val captionSize: StateFlow<String> = _captionSize.asStateFlow()

    // Downloads
    private val _downloadQuality = MutableStateFlow(prefs.getString("download_quality", "Full HD (1080p)") ?: "Full HD (1080p)")
    val downloadQuality: StateFlow<String> = _downloadQuality.asStateFlow()

    private val _smartDownloads = MutableStateFlow(prefs.getBoolean("smart_downloads", true))
    val smartDownloads: StateFlow<Boolean> = _smartDownloads.asStateFlow()

    // History & Privacy
    private val _pauseHistory = MutableStateFlow(prefs.getBoolean("pause_history", false))
    val pauseHistory: StateFlow<Boolean> = _pauseHistory.asStateFlow()

    // Notifications
    private val _scheduledDigest = MutableStateFlow(prefs.getBoolean("scheduled_digest", true))
    val scheduledDigest: StateFlow<Boolean> = _scheduledDigest.asStateFlow()

    private val _subscriptionsNotifications = MutableStateFlow(prefs.getBoolean("subscriptions_notifications", true))
    val subscriptionsNotifications: StateFlow<Boolean> = _subscriptionsNotifications.asStateFlow()

    private val _recommendedNotifications = MutableStateFlow(prefs.getBoolean("recommended_notifications", true))
    val recommendedNotifications: StateFlow<Boolean> = _recommendedNotifications.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_master", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _inAppSounds = MutableStateFlow(prefs.getBoolean("in_app_sounds", true))
    val inAppSounds: StateFlow<Boolean> = _inAppSounds.asStateFlow()

    // Feeds & Upload
    private val _playbackInFeeds = MutableStateFlow(prefs.getString("playback_in_feeds", "Always on") ?: "Always on")
    val playbackInFeeds: StateFlow<String> = _playbackInFeeds.asStateFlow()

    private val _uploadQuality = MutableStateFlow(prefs.getString("upload_quality", "Full HD (1080p)") ?: "Full HD (1080p)")
    val uploadQuality: StateFlow<String> = _uploadQuality.asStateFlow()

    // Download over Wi-Fi
    private val _downloadWifiOnly = MutableStateFlow(prefs.getBoolean("download_wifi_only", true))
    val downloadWifiOnly: StateFlow<Boolean> = _downloadWifiOnly.asStateFlow()

    // Search History Privacy
    private val _pauseSearchHistory = MutableStateFlow(prefs.getBoolean("pause_search_history", false))
    val pauseSearchHistory: StateFlow<Boolean> = _pauseSearchHistory.asStateFlow()

    // Experimental Features
    private val _experimentalAiClips = MutableStateFlow(prefs.getBoolean("experimental_ai_clips", true))
    val experimentalAiClips: StateFlow<Boolean> = _experimentalAiClips.asStateFlow()

    private val _experimentalAudioBooster = MutableStateFlow(prefs.getBoolean("experimental_audio_booster", false))
    val experimentalAudioBooster: StateFlow<Boolean> = _experimentalAudioBooster.asStateFlow()

    fun setRemindBreak(enabled: Boolean) {
        _remindBreak.value = enabled
        prefs.edit().putBoolean("remind_break", enabled).apply()
        scope.launch { dataStore.setRemindBreak(enabled) }
    }

    fun setBreakInterval(minutes: Int) {
        _breakIntervalMinutes.value = minutes
        prefs.edit().putInt("break_interval", minutes).apply()
        scope.launch { dataStore.setBreakIntervalMinutes(minutes) }
    }

    fun setRemindBedtime(enabled: Boolean) {
        _remindBedtime.value = enabled
        prefs.edit().putBoolean("remind_bedtime", enabled).apply()
        scope.launch { dataStore.setRemindBedtime(enabled) }
    }

    fun setBedtimeHour(hour: Int) {
        _bedtimeHour.value = hour
        prefs.edit().putInt("bedtime_hour", hour).apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        prefs.edit().putBoolean("dark_theme", enabled).apply()
        scope.launch { dataStore.setDarkMode(enabled) }
    }

    fun setDoubleTapSeekSeconds(seconds: Int) {
        _doubleTapSeekSeconds.value = seconds
        prefs.edit().putInt("seek_duration", seconds).apply()
        scope.launch { dataStore.setDoubleTapSeekSeconds(seconds) }
    }

    fun setZoomToFill(enabled: Boolean) {
        _zoomToFill.value = enabled
        prefs.edit().putBoolean("zoom_to_fill", enabled).apply()
        scope.launch { dataStore.setZoomToFill(enabled) }
    }

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("app_language", lang).apply()
    }

    fun setAutoplayNext(enabled: Boolean) {
        _autoplayNext.value = enabled
        prefs.edit().putBoolean("autoplay_next", enabled).apply()
        scope.launch { dataStore.setAutoplayNext(enabled) }
    }

    fun setDefaultSpeed(speed: Float) {
        _defaultSpeed.value = speed
        prefs.edit().putFloat("default_speed", speed).apply()
        scope.launch { dataStore.setPlaybackSpeed(speed) }
    }

    fun setVideoQuality(quality: String) {
        _videoQuality.value = quality
        prefs.edit().putString("video_quality", quality).apply()
        scope.launch { dataStore.setVideoQuality(quality) }
    }

    fun setCaptionsEnabled(enabled: Boolean) {
        _captionsEnabled.value = enabled
        prefs.edit().putBoolean("captions_enabled", enabled).apply()
        scope.launch { dataStore.setCaptionsEnabled(enabled) }
    }

    fun setCaptionSize(size: String) {
        _captionSize.value = size
        prefs.edit().putString("caption_size", size).apply()
    }

    fun setDownloadQuality(quality: String) {
        _downloadQuality.value = quality
        prefs.edit().putString("download_quality", quality).apply()
        scope.launch { dataStore.setDownloadQuality(quality) }
    }

    fun setSmartDownloads(enabled: Boolean) {
        _smartDownloads.value = enabled
        prefs.edit().putBoolean("smart_downloads", enabled).apply()
        scope.launch { dataStore.setSmartDownloads(enabled) }
    }

    fun setPauseHistory(paused: Boolean) {
        _pauseHistory.value = paused
        prefs.edit().putBoolean("pause_history", paused).apply()
        scope.launch { dataStore.setPauseHistory(paused) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_master", enabled).apply()
        scope.launch { dataStore.setNotificationsEnabled(enabled) }
    }

    fun setScheduledDigest(enabled: Boolean) {
        _scheduledDigest.value = enabled
        prefs.edit().putBoolean("scheduled_digest", enabled).apply()
        scope.launch { dataStore.setScheduledDigest(enabled) }
    }

    fun setInAppSounds(enabled: Boolean) {
        _inAppSounds.value = enabled
        prefs.edit().putBoolean("in_app_sounds", enabled).apply()
    }

    fun setPlaybackInFeeds(setting: String) {
        _playbackInFeeds.value = setting
        prefs.edit().putString("playback_in_feeds", setting).apply()
        scope.launch { dataStore.setPlaybackInFeeds(setting) }
    }

    fun setUploadQuality(quality: String) {
        _uploadQuality.value = quality
        prefs.edit().putString("upload_quality", quality).apply()
        scope.launch { dataStore.setUploadQuality(quality) }
    }

    fun setDownloadWifiOnly(enabled: Boolean) {
        _downloadWifiOnly.value = enabled
        prefs.edit().putBoolean("download_wifi_only", enabled).apply()
        scope.launch { dataStore.setDownloadWifiOnly(enabled) }
    }

    fun setPauseSearchHistory(paused: Boolean) {
        _pauseSearchHistory.value = paused
        prefs.edit().putBoolean("pause_search_history", paused).apply()
        scope.launch { dataStore.setPauseSearchHistory(paused) }
    }

    fun setSubscriptionsNotifications(enabled: Boolean) {
        _subscriptionsNotifications.value = enabled
        prefs.edit().putBoolean("subscriptions_notifications", enabled).apply()
        scope.launch { dataStore.setSubscriptionsNotifications(enabled) }
    }

    fun setRecommendedNotifications(enabled: Boolean) {
        _recommendedNotifications.value = enabled
        prefs.edit().putBoolean("recommended_notifications", enabled).apply()
        scope.launch { dataStore.setRecommendedNotifications(enabled) }
    }

    fun setExperimentalAiClips(enabled: Boolean) {
        _experimentalAiClips.value = enabled
        prefs.edit().putBoolean("experimental_ai_clips", enabled).apply()
    }

    fun setExperimentalAudioBooster(enabled: Boolean) {
        _experimentalAudioBooster.value = enabled
        prefs.edit().putBoolean("experimental_audio_booster", enabled).apply()
    }
}
