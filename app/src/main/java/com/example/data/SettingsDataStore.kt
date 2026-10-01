package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "mytube_user_preferences")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_DARK_MODE = booleanPreferencesKey("pref_dark_mode")
        val KEY_PLAYBACK_SPEED = floatPreferencesKey("pref_playback_speed")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("pref_notifications_enabled")
        val KEY_SUBSCRIPTIONS_NOTIFICATIONS = booleanPreferencesKey("pref_subscriptions_notifications")
        val KEY_RECOMMENDED_NOTIFICATIONS = booleanPreferencesKey("pref_recommended_notifications")
        val KEY_SCHEDULED_DIGEST = booleanPreferencesKey("pref_scheduled_digest")

        val KEY_REMIND_BREAK = booleanPreferencesKey("pref_remind_break")
        val KEY_BREAK_INTERVAL = intPreferencesKey("pref_break_interval")
        val KEY_REMIND_BEDTIME = booleanPreferencesKey("pref_remind_bedtime")
        val KEY_BEDTIME_HOUR = intPreferencesKey("pref_bedtime_hour")
        val KEY_DOUBLE_TAP_SEEK = intPreferencesKey("pref_double_tap_seek")
        val KEY_ZOOM_TO_FILL = booleanPreferencesKey("pref_zoom_to_fill")
        val KEY_PLAYBACK_IN_FEEDS = stringPreferencesKey("pref_playback_in_feeds")
        val KEY_UPLOAD_QUALITY = stringPreferencesKey("pref_upload_quality")

        val KEY_AUTOPLAY_NEXT = booleanPreferencesKey("pref_autoplay_next")
        val KEY_VIDEO_QUALITY = stringPreferencesKey("pref_video_quality")
        val KEY_CAPTIONS_ENABLED = booleanPreferencesKey("pref_captions_enabled")
        val KEY_DOWNLOAD_QUALITY = stringPreferencesKey("pref_download_quality")
        val KEY_DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("pref_download_wifi_only")
        val KEY_SMART_DOWNLOADS = booleanPreferencesKey("pref_smart_downloads")
        val KEY_PAUSE_HISTORY = booleanPreferencesKey("pref_pause_history")
        val KEY_PAUSE_SEARCH_HISTORY = booleanPreferencesKey("pref_pause_search_history")
    }

    private val dataStore = context.settingsDataStore

    private val preferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    // Toggles for Dark Mode, Playback Speed, and Notification Preferences using DataStore
    val darkModeFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_DARK_MODE] ?: true
    }

    val playbackSpeedFlow: Flow<Float> = preferencesFlow.map { prefs ->
        prefs[KEY_PLAYBACK_SPEED] ?: 1.0f
    }

    val notificationsEnabledFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val subscriptionsNotificationsFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_SUBSCRIPTIONS_NOTIFICATIONS] ?: true
    }

    val recommendedNotificationsFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_RECOMMENDED_NOTIFICATIONS] ?: true
    }

    val scheduledDigestFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_SCHEDULED_DIGEST] ?: true
    }

    val autoplayNextFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_AUTOPLAY_NEXT] ?: true
    }

    val doubleTapSeekSecondsFlow: Flow<Int> = preferencesFlow.map { prefs ->
        prefs[KEY_DOUBLE_TAP_SEEK] ?: 10
    }

    val remindBreakFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_REMIND_BREAK] ?: false
    }

    val breakIntervalMinutesFlow: Flow<Int> = preferencesFlow.map { prefs ->
        prefs[KEY_BREAK_INTERVAL] ?: 60
    }

    val remindBedtimeFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_REMIND_BEDTIME] ?: false
    }

    val zoomToFillFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_ZOOM_TO_FILL] ?: false
    }

    val playbackInFeedsFlow: Flow<String> = preferencesFlow.map { prefs ->
        prefs[KEY_PLAYBACK_IN_FEEDS] ?: "Always on"
    }

    val uploadQualityFlow: Flow<String> = preferencesFlow.map { prefs ->
        prefs[KEY_UPLOAD_QUALITY] ?: "Full HD (1080p)"
    }

    val videoQualityFlow: Flow<String> = preferencesFlow.map { prefs ->
        prefs[KEY_VIDEO_QUALITY] ?: "Auto (recommended)"
    }

    val captionsEnabledFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_CAPTIONS_ENABLED] ?: false
    }

    val downloadQualityFlow: Flow<String> = preferencesFlow.map { prefs ->
        prefs[KEY_DOWNLOAD_QUALITY] ?: "Full HD (1080p)"
    }

    val downloadWifiOnlyFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_DOWNLOAD_WIFI_ONLY] ?: true
    }

    val smartDownloadsFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_SMART_DOWNLOADS] ?: true
    }

    val pauseHistoryFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_PAUSE_HISTORY] ?: false
    }

    val pauseSearchHistoryFlow: Flow<Boolean> = preferencesFlow.map { prefs ->
        prefs[KEY_PAUSE_SEARCH_HISTORY] ?: false
    }

    // Mutations via DataStore edit
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        dataStore.edit { it[KEY_PLAYBACK_SPEED] = speed }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSubscriptionsNotifications(enabled: Boolean) {
        dataStore.edit { it[KEY_SUBSCRIPTIONS_NOTIFICATIONS] = enabled }
    }

    suspend fun setRecommendedNotifications(enabled: Boolean) {
        dataStore.edit { it[KEY_RECOMMENDED_NOTIFICATIONS] = enabled }
    }

    suspend fun setScheduledDigest(enabled: Boolean) {
        dataStore.edit { it[KEY_SCHEDULED_DIGEST] = enabled }
    }

    suspend fun setAutoplayNext(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTOPLAY_NEXT] = enabled }
    }

    suspend fun setDoubleTapSeekSeconds(seconds: Int) {
        dataStore.edit { it[KEY_DOUBLE_TAP_SEEK] = seconds }
    }

    suspend fun setRemindBreak(enabled: Boolean) {
        dataStore.edit { it[KEY_REMIND_BREAK] = enabled }
    }

    suspend fun setBreakIntervalMinutes(minutes: Int) {
        dataStore.edit { it[KEY_BREAK_INTERVAL] = minutes }
    }

    suspend fun setRemindBedtime(enabled: Boolean) {
        dataStore.edit { it[KEY_REMIND_BEDTIME] = enabled }
    }

    suspend fun setZoomToFill(enabled: Boolean) {
        dataStore.edit { it[KEY_ZOOM_TO_FILL] = enabled }
    }

    suspend fun setPlaybackInFeeds(setting: String) {
        dataStore.edit { it[KEY_PLAYBACK_IN_FEEDS] = setting }
    }

    suspend fun setUploadQuality(quality: String) {
        dataStore.edit { it[KEY_UPLOAD_QUALITY] = quality }
    }

    suspend fun setVideoQuality(quality: String) {
        dataStore.edit { it[KEY_VIDEO_QUALITY] = quality }
    }

    suspend fun setCaptionsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_CAPTIONS_ENABLED] = enabled }
    }

    suspend fun setDownloadQuality(quality: String) {
        dataStore.edit { it[KEY_DOWNLOAD_QUALITY] = quality }
    }

    suspend fun setDownloadWifiOnly(enabled: Boolean) {
        dataStore.edit { it[KEY_DOWNLOAD_WIFI_ONLY] = enabled }
    }

    suspend fun setSmartDownloads(enabled: Boolean) {
        dataStore.edit { it[KEY_SMART_DOWNLOADS] = enabled }
    }

    suspend fun setPauseHistory(paused: Boolean) {
        dataStore.edit { it[KEY_PAUSE_HISTORY] = paused }
    }

    suspend fun setPauseSearchHistory(paused: Boolean) {
        dataStore.edit { it[KEY_PAUSE_SEARCH_HISTORY] = paused }
    }
}
