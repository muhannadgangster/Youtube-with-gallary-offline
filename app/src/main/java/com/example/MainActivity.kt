package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VideoItem
import com.example.ui.components.CommentsSheet
import com.example.ui.components.CreateClipDialog
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.PlusModalSheet
import com.example.ui.components.RenameTitleDialog
import com.example.ui.components.YouTubeBottomBar
import com.example.ui.components.YouTubeTopBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ModernPlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.YtDarkBackground
import com.example.ui.viewmodel.MediaScannerViewModel
import com.example.ui.viewmodel.YouTubeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyTubeApp()
            }
        }
    }
}

@Composable
fun MyTubeApp(
    viewModel: YouTubeViewModel = viewModel(),
    mediaScannerViewModel: MediaScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State collections from ViewModel
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val filteredVideos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val shortsVideos by viewModel.shortsVideos.collectAsStateWithLifecycle()
    val subscribedVideos by viewModel.subscribedVideos.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()
    val watchLaterVideos by viewModel.watchLaterVideos.collectAsStateWithLifecycle()
    val downloadedVideos by viewModel.downloadedVideos.collectAsStateWithLifecycle()
    val historyVideos by viewModel.historyVideos.collectAsStateWithLifecycle()

    val isScanningMedia by mediaScannerViewModel.isScanning.collectAsStateWithLifecycle()
    val mediaScanStatus by mediaScannerViewModel.statusMessage.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentlyPlaying by viewModel.currentlyPlaying.collectAsStateWithLifecycle()

    val editingVideo by viewModel.editingVideo.collectAsStateWithLifecycle()
    val showPlusSheet by viewModel.showPlusSheet.collectAsStateWithLifecycle()
    val commentingVideo by viewModel.commentingVideo.collectAsStateWithLifecycle()
    val showNotificationsSheet by viewModel.showNotificationsSheet.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()

    var showCreateClipDialog by remember { mutableStateOf(false) }

    // Runtime Permission Request on App Launch (Handles both Videos & Images)
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (granted) {
            mediaScannerViewModel.scanDeviceMedia()
            viewModel.scanStorage()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest)
    }

    fun shareVideo(video: VideoItem) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out \"${video.displayTitle}\" by ${video.displayChannelName} on MyTube!"
            )
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share video"))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YtDarkBackground)
    ) {
        if (showSettings) {
            // Full YouTube Settings Screen
            SettingsScreen(
                settingsManager = viewModel.settingsManager,
                onBack = { viewModel.setSettingsOpen(false) },
                onClearWatchHistory = {
                    viewModel.clearWatchHistory()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Watch history cleared")
                    }
                },
                onClearSearchHistory = {
                    viewModel.updateSearchQuery("")
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Search history cleared")
                    }
                },
                onDeleteAllDownloads = {
                    viewModel.deleteAllDownloads()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("All downloads removed")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (currentlyPlaying != null) {
            // Full Video Player View
            ModernPlayerScreen(
                video = currentlyPlaying!!,
                upNextVideos = allVideos,
                onClose = { viewModel.closePlayer() },
                onEditTitleClick = { viewModel.openRenameDialog(currentlyPlaying!!) },
                onToggleLike = { viewModel.toggleLike(currentlyPlaying!!) },
                onToggleDislike = { viewModel.toggleDislike(currentlyPlaying!!) },
                onToggleSubscribe = { viewModel.toggleSubscribe(currentlyPlaying!!) },
                onToggleDownload = {
                    viewModel.toggleDownloaded(currentlyPlaying!!)
                    coroutineScope.launch {
                        val isDl = !(currentlyPlaying!!.isDownloaded)
                        snackbarHostState.showSnackbar(if (isDl) "Saved to Downloads" else "Removed from Downloads")
                    }
                },
                onOpenComments = { viewModel.openComments(currentlyPlaying!!) },
                onShare = { shareVideo(currentlyPlaying!!) },
                onSelectUpNext = { nextVideo -> viewModel.playVideo(nextVideo) },
                onProgressUpdate = { pos -> viewModel.recordProgress(currentlyPlaying!!, pos) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Main App Shell with Top Bar and Bottom Bar
            Scaffold(
                containerColor = YtDarkBackground,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (selectedTab != 1) { // Hide top bar on Shorts tab for full immersion
                        YouTubeTopBar(
                            isSearchActive = isSearchActive,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onSearchToggle = { viewModel.setSearchActive(it) },
                            onNotificationsClick = { viewModel.setNotificationsSheetOpen(true) },
                            onProfileClick = { viewModel.selectTab(4) }
                        )
                    }
                },
                bottomBar = {
                    YouTubeBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_switch_animation"
                    ) { tab ->
                        when (tab) {
                            0 -> HomeScreen(
                                videos = filteredVideos,
                                selectedFilter = selectedFilter,
                                onFilterSelected = { viewModel.selectFilter(it) },
                                onVideoClick = { viewModel.playVideo(it) },
                                onEditTitleClick = { viewModel.openRenameDialog(it) },
                                onToggleWatchLater = {
                                    viewModel.toggleWatchLater(it)
                                    coroutineScope.launch {
                                        val isWl = !(it.isWatchLater)
                                        snackbarHostState.showSnackbar(if (isWl) "Saved to Watch Later" else "Removed from Watch Later")
                                    }
                                },
                                onToggleSubscribe = {
                                    viewModel.toggleSubscribe(it)
                                    coroutineScope.launch {
                                        val isSub = !(it.isSubscribed)
                                        snackbarHostState.showSnackbar(if (isSub) "Subscribed to ${it.displayChannelName}" else "Unsubscribed")
                                    }
                                },
                                onToggleDownload = {
                                    viewModel.toggleDownloaded(it)
                                    coroutineScope.launch {
                                        val isDl = !(it.isDownloaded)
                                        snackbarHostState.showSnackbar(if (isDl) "Saved to Downloads" else "Removed from Downloads")
                                    }
                                },
                                onShareClick = { shareVideo(it) },
                                onScanStorageClick = {
                                    mediaScannerViewModel.scanDeviceMedia()
                                    viewModel.scanStorage()
                                }
                            )

                            1 -> ShortsScreen(
                                shortsList = if (shortsVideos.isNotEmpty()) shortsVideos else allVideos,
                                onToggleLike = { viewModel.toggleLike(it) },
                                onToggleDislike = { viewModel.toggleDislike(it) },
                                onToggleSubscribe = { viewModel.toggleSubscribe(it) },
                                onOpenComments = { viewModel.openComments(it) },
                                onEditTitle = { viewModel.openRenameDialog(it) },
                                onShare = { shareVideo(it) }
                            )

                            3 -> SubscriptionsScreen(
                                subscribedVideos = subscribedVideos,
                                onVideoClick = { viewModel.playVideo(it) },
                                onEditTitleClick = { viewModel.openRenameDialog(it) },
                                onToggleWatchLater = { viewModel.toggleWatchLater(it) },
                                onToggleSubscribe = { viewModel.toggleSubscribe(it) },
                                onToggleDownload = { viewModel.toggleDownloaded(it) },
                                onShareClick = { shareVideo(it) },
                                onExploreClick = { viewModel.selectTab(0) }
                            )

                            4 -> LibraryScreen(
                                historyVideos = historyVideos,
                                watchLaterVideos = watchLaterVideos,
                                downloadedVideos = downloadedVideos,
                                likedVideos = likedVideos,
                                allVideos = allVideos,
                                onVideoClick = { viewModel.playVideo(it) },
                                onSearchClick = {
                                    viewModel.selectTab(0)
                                    viewModel.setSearchActive(true)
                                },
                                onNotificationsClick = { viewModel.setNotificationsSheetOpen(true) },
                                onSettingsClick = { viewModel.setSettingsOpen(true) }
                            )
                        }
                    }
                }
            }
        }

        // Rename Title & Hashtags Dialog (Working Custom Title Feature)
        if (editingVideo != null) {
            RenameTitleDialog(
                video = editingVideo!!,
                onDismiss = { viewModel.dismissRenameDialog() },
                onSave = { newTitle, newHashtags ->
                    viewModel.saveCustomTitleAndHashtags(newTitle, newHashtags)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Title & Hashtags updated")
                    }
                }
            )
        }

        // Center Plus (+) Modal Sheet with Real Import & Creation Options
        if (showPlusSheet) {
            PlusModalSheet(
                onDismiss = { viewModel.setPlusSheetOpen(false) },
                onVideoSelected = { uri ->
                    viewModel.importGalleryVideo(uri)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Importing video from gallery...")
                    }
                },
                onShortMediaSelected = { uri ->
                    viewModel.importShortMedia(uri)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Added 5-10s clip to Shorts feed")
                    }
                },
                onPhotosSelected = { uris ->
                    viewModel.createPhotoSlideshow(
                        photoUris = uris,
                        title = "Gallery Story | Ken Burns #Shorts",
                        hashtags = "#Shorts #PhotoStory #Memories #Gallery"
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Created animated Photo Story")
                    }
                },
                onScanStorage = {
                    mediaScannerViewModel.scanDeviceMedia()
                    viewModel.scanStorage()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Scanning local device storage...")
                    }
                },
                onCreateShortsClip = {
                    showCreateClipDialog = true
                }
            )
        }

        // Create Shorts Clip Dialog
        if (showCreateClipDialog) {
            CreateClipDialog(
                videos = allVideos,
                onDismiss = { showCreateClipDialog = false },
                onSaveClip = { selectedVideo, clipTitle, durationSec ->
                    showCreateClipDialog = false
                    viewModel.saveCustomTitle(clipTitle)
                    viewModel.selectTab(1)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Short created: $clipTitle")
                    }
                }
            )
        }

        // Comments & Offline Notes Sheet
        if (commentingVideo != null) {
            CommentsSheet(
                video = commentingVideo!!,
                onDismiss = { viewModel.closeComments() },
                onSaveComment = { comment ->
                    viewModel.saveComment(comment)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Note saved offline")
                    }
                }
            )
        }

        // Notifications Sheet
        if (showNotificationsSheet) {
            NotificationsSheet(
                onDismiss = { viewModel.setNotificationsSheetOpen(false) }
            )
        }
    }
}
