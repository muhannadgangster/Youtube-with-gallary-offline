package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.VideoItem
import com.example.data.local.AppDatabase
import com.example.data.local.VideoMetadata
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyTube", appName)
  }

  @Test
  fun `videoItem custom title takes precedence over original`() {
    val item = VideoItem(
      id = "1",
      uriString = "content://media/1",
      filePath = "/storage/1.mp4",
      originalTitle = "VID_20260928_1001",
      customTitle = "My Epic Vlog #1"
    )
    assertEquals("My Epic Vlog #1", item.displayTitle)
  }

  @Test
  fun `videoItem formatted duration renders correctly`() {
    val item = VideoItem(
      id = "2",
      uriString = "content://media/2",
      filePath = "/storage/2.mp4",
      originalTitle = "Short Clip",
      durationMs = 125000L // 2 min 5 sec
    )
    assertEquals("2:05", item.formattedDuration)
  }

  @Test
  fun `videoItem watch progress calculation`() {
    val item = VideoItem(
      id = "3",
      uriString = "content://media/3",
      filePath = "/storage/3.mp4",
      originalTitle = "Tutorial",
      durationMs = 100000L,
      watchPositionMs = 50000L
    )
    assertEquals(0.5f, item.watchProgressFraction, 0.01f)
  }

  @Test
  fun `videoMetadata room persistence and custom title update`() = runBlocking {
    val dao = db.videoMetadataDao()
    val metadata = VideoMetadata(
      id = "test_vid_101",
      uriString = "content://media/101",
      filePath = "/storage/emulated/0/DCIM/Camera/VID_101.mp4",
      originalTitle = "Camera Recording 101",
      customTitle = null,
      hashtags = "#Shorts #Trending",
      channelName = "Camera",
      durationMs = 60000L,
      sizeBytes = 15000000L,
      dateAdded = System.currentTimeMillis(),
      width = 1920,
      height = 1080
    )

    dao.insertOrUpdate(metadata)

    val fetched = dao.getById("test_vid_101")
    assertNotNull(fetched)
    assertEquals("Camera Recording 101", fetched!!.originalTitle)

    // Update custom title and hashtags
    dao.updateCustomTitleAndHashtags("test_vid_101", "My Viral Daily Vlog", "#Viral #DailyVlog")
    val updated = dao.getById("test_vid_101")
    assertEquals("My Viral Daily Vlog", updated!!.customTitle)
    assertEquals("#Viral #DailyVlog", updated.hashtags)

    // Update watch progress
    dao.updateWatchProgress("test_vid_101", 35000L, 1759060000000L)
    val afterWatch = dao.getById("test_vid_101")
    assertEquals(35000L, afterWatch!!.watchPositionMs)
    assertEquals(1759060000000L, afterWatch.lastWatchedTimestamp)

    // Flow stream emission
    val allItems = dao.getAllMetadata().first()
    assertEquals(1, allItems.size)
  }
}
