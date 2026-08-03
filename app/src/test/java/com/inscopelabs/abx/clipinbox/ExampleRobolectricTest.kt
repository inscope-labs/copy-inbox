package com.inscopelabs.abx.clipinbox

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Copy Inbox", appName)
  }

  @Test
  fun `application context is instantiated`() {
    val app = ApplicationProvider.getApplicationContext<ClipInBoxApplication>()
    assertNotNull(app)
  }
}
