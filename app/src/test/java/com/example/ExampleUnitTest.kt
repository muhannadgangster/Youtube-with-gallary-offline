package com.example

import com.example.util.MediaScannerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCatchyTitleAndHashtagsForShorts() {
    val (title, hashtags) = MediaScannerHelper.generateCatchyTitleAndHashtags(
      rawName = "VID_20260928_12345.mp4",
      isPhotoSlideshow = false,
      isShort = true,
      dateAddedMs = 1759060000000L
    )
    assertTrue(title.contains("Highlight"))
    assertTrue(hashtags.contains("#Shorts"))
  }

  @Test
  fun testCatchyTitleForPhotoSlideshow() {
    val (title, hashtags) = MediaScannerHelper.generateCatchyTitleAndHashtags(
      rawName = "IMG_9999.jpg",
      isPhotoSlideshow = true,
      isShort = true,
      dateAddedMs = 1759060000000L
    )
    assertTrue(title.contains("Memories"))
    assertTrue(hashtags.contains("#PhotoSlideshow"))
  }
}
