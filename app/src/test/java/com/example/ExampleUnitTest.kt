package com.example

import com.example.data.GeminiService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testWhatsAppParsingFallback() {
    val text = "أبو عماد - ٠٩٤٤٨٨٩٩٠٠ - اشتراك ٣ أشهر من ١٠-٥-٢٠٢٦ كرم الجبل\nخالد محمد، اشتراك شهرين جهاز hd990 من أول أمس"
    val results = GeminiService.parseWhatsAppSubscriptionsFallback(text)
    
    assertEquals(2, results.size)
    
    val first = results[0]
    assertEquals("أبو عماد", first["name"])
    assertEquals("0944889900", first["phone_number"])
    assertEquals(3.0, first["subscription_duration"])
    assertEquals("months", first["subscription_duration_type"])
    assertEquals("كرم الجبل", first["address"])

    val second = results[1]
    assertEquals("خالد محمد", second["name"])
    assertEquals(2.0, second["subscription_duration"])
    assertEquals("months", second["subscription_duration_type"])
    assertEquals("hd990", second["device_number"])
  }
}
