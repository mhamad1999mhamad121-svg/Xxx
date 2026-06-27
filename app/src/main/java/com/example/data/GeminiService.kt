package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object GeminiService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Gemini API Request & Response structures
    data class Content(val parts: List<Part>)
    data class Part(val text: String)
    data class GenerationConfig(val responseMimeType: String?, val temperature: Float?)
    data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig? = null
    )

    data class GeminiResponse(val candidates: List<Candidate>?)
    data class Candidate(val content: Content?)

    /**
     * Parse raw WhatsApp text into structured subscriber maps.
     * Falls back gracefully to high-performance local regex processing if the API Key is unconfigured or a network error occurs.
     */
    suspend fun parseWhatsAppSubscriptions(inputText: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext parseWhatsAppSubscriptionsFallback(inputText)
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val systemPrompt = """
            You are an expert Arabic data assistant. Your job is to extract customer subscription information from raw text chats (usually copied from WhatsApp groups) and format it into a valid JSON array.

            Input text can be quite messy, containing subscriber names, phone numbers, durations, starts/ends of subscriptions, device numbers, addresses, etc.

            You must return a raw JSON array of objects. Each object in the array must contain the following keys:
            - "name" (String): The subscriber's name or contact. Make sure to clean any emojis or tags.
            - "phone_number" (String): The subscriber's phone number. Clean it to contain only digits, e.g. "0991234567". If missing, use empty string "".
            - "subscription_date" (String in YYYY-MM-DD): The starting date of the subscription. If not explicitly found or is relative (e.g. today, now), use today's date: "$todayDate".
            - "subscription_duration" (Integer): The count/duration. Default is 1 if not specified or unclear.
            - "subscription_duration_type" (String): MUST be either "months" or "days". Default is "months".
            - "device_number" (String): The device number or receiver ID if found. Empty string "" if not found.
            - "address" (String): The address or local district of Aleppo if specified (e.g., صلاح الدين، كرم الجبل، بستان القصر، الفرقان). Empty string "" if not found.

            Example input:
            أبو عمر - بستان القصر - 0933111222 - اشتراك ٣ شهور جهاز رقم ٢٥٠ من ١٢-٠٥-٢٠٢٦

            Desired Output JSON:
            [
              {
                "name": "أبو عمر",
                "phone_number": "0933111222",
                "subscription_date": "2026-05-12",
                "subscription_duration": 3,
                "subscription_duration_type": "months",
                "device_number": "٢٥٠",
                "address": "بستان القصر"
              }
            ]

            Do NOT wrap the output in markdown. Start directly with '[' and end with ']'.
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nNow process the following raw text:\n$inputText"

        val requestObj = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = fullPrompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        val requestJson = try {
            moshi.adapter(GeminiRequest::class.java).toJson(requestObj)
        } catch (e: Exception) {
            return@withContext parseWhatsAppSubscriptionsFallback(inputText)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val requestBody = requestJson.toRequestBody("application/json".toMediaType())
        val okRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = okHttpClient.newCall(okRequest).execute()
            if (!response.isSuccessful) {
                return@withContext parseWhatsAppSubscriptionsFallback(inputText)
            }

            val responseBody = response.body?.string() ?: return@withContext parseWhatsAppSubscriptionsFallback(inputText)
            val geminiResponse = moshi.adapter(GeminiResponse::class.java).fromJson(responseBody)
            
            val rawText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext parseWhatsAppSubscriptionsFallback(inputText)

            val cleanJson = cleanJsonString(rawText)
            
            val listType = Types.newParameterizedType(List::class.java, Map::class.java)
            val listAdapter = moshi.adapter<List<Map<String, Any>>>(listType)
            
            return@withContext listAdapter.fromJson(cleanJson) ?: parseWhatsAppSubscriptionsFallback(inputText)
        } catch (e: Exception) {
            return@withContext parseWhatsAppSubscriptionsFallback(inputText)
        }
    }

    /**
     * Parse raw WhatsApp subscriptions with local regex when Gemini API key is missing or calls fail.
     */
    fun parseWhatsAppSubscriptionsFallback(inputText: String): List<Map<String, Any>> {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lines = inputText.split("\n", "\r").map { it.trim() }.filter { it.isNotBlank() }
        val results = mutableListOf<Map<String, Any>>()
        
        val aleppoNeighborhoods = listOf(
            "بستان القصر", "الكلاسة", "صلاح الدين", "كرم الجبل", "الفرقان", "حلب الجديدة", 
            "المشهد", "الجميلية", "سيف الدولة", "الشهباء", "الحمدانية", "الموكامبو", 
            "الشعار", "طريق الباب", "السكري", "الميدان", "الأعظمية", "المرجة", "الصاخور", 
            "مساكن هنانو", "الأنصاري", "باب النيرب", "جب القبة", "المغاير", "الفردوس", 
            "بستان الباشا", "السبيل", "الخالدية", "المحافظة", "الشهباء الجديدة", "النيال", 
            "السريان", "العزيزية", "السليمانية"
        )

        for (line in lines) {
            val normalizedLine = convertArabicIndianDigitsToEnglish(line)
            
            // 1. Phone number extraction
            val phoneRegex = Regex("""09\d{8}|\b\d{8,15}\b""")
            val phoneMatch = phoneRegex.find(normalizedLine)
            val phoneNumber = phoneMatch?.value ?: ""
            
            // Check for Aleppo neighborhoods
            var hasNeighborhood = false
            for (place in aleppoNeighborhoods) {
                if (normalizedLine.contains(place)) {
                    hasNeighborhood = true
                    break
                }
            }

            // Check for active subscription keywords
            val hasKeys = normalizedLine.contains("اشتراك") || 
                          normalizedLine.contains("أشترك") ||
                          normalizedLine.contains("مشترك") ||
                          normalizedLine.contains("تجديد") || 
                          normalizedLine.contains("تفعيل") || 
                          normalizedLine.contains("باقة") ||
                          normalizedLine.contains("جهاز") || 
                          normalizedLine.contains("ريسيفر") || 
                          normalizedLine.contains("سنة") || 
                          normalizedLine.contains("عام") || 
                          normalizedLine.contains("شهر") || 
                          normalizedLine.contains("يوم") || 
                          normalizedLine.contains("أسبوع") || 
                          normalizedLine.contains("اسبوع")

            // Strict Filter: Skip lines that do not have any phone numbers, neighborhood mentions, or active keywords
            if (phoneNumber.isBlank() && !hasNeighborhood && !hasKeys) {
                continue
            }
            
            // 2. Start Date extraction
            val dateRegex = Regex("""(\d{1,4})[-/.](\d{1,2})[-/.](\d{1,4})""")
            val dateMatch = dateRegex.find(normalizedLine)
            var subscriptionDate = todayDate
            if (dateMatch != null) {
                val part1 = dateMatch.groupValues[1]
                val part2 = dateMatch.groupValues[2]
                val part3 = dateMatch.groupValues[3]
                try {
                    val (year, month, day) = if (part1.length == 4) {
                        Triple(part1.toInt(), part2.toInt(), part3.toInt())
                    } else if (part3.length == 4) {
                        Triple(part3.toInt(), part2.toInt(), part1.toInt())
                    } else {
                        val currYear = Calendar.getInstance().get(Calendar.YEAR)
                        Triple(currYear, part2.toInt(), part1.toInt())
                    }
                    val formattedMonth = String.format(Locale.US, "%02d", month)
                    val formattedDay = String.format(Locale.US, "%02d", day)
                    subscriptionDate = "$year-$formattedMonth-$formattedDay"
                } catch (e: Exception) {
                    // keep default todayDate
                }
            } else {
                val calendar = Calendar.getInstance()
                if (normalizedLine.contains("أول أمس") || normalizedLine.contains("اول امس")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -2)
                    subscriptionDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
                } else if (normalizedLine.contains("أمس") || normalizedLine.contains("امس") || normalizedLine.contains("البارحة")) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    subscriptionDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
                }
            }
            
            // 3. Duration & type extraction
            var duration = 1.0
            var durationType = "months"
            if (normalizedLine.contains("أيام") || normalizedLine.contains("ايام") || normalizedLine.contains("يوما") || normalizedLine.contains("يوم") || normalizedLine.contains("يوماً")) {
                durationType = "days"
                val daysRegex = Regex("""(\d+)\s*(يوم|ايام|أيام|يوماً)""")
                val match = daysRegex.find(normalizedLine)
                if (match != null) {
                    duration = match.groupValues[1].toDoubleOrNull() ?: 1.0
                } else {
                    if (normalizedLine.contains("أسبوع") || normalizedLine.contains("اسبوع")) {
                        duration = 7.0
                    } else if (normalizedLine.contains("أسبوعين") || normalizedLine.contains("اسبوعين")) {
                        duration = 14.0
                    }
                }
            } else {
                durationType = "months"
                if (normalizedLine.contains("شهرين")) {
                    duration = 2.0
                } else if (normalizedLine.contains("سنة") || normalizedLine.contains("عام")) {
                    duration = 12.0
                } else {
                    val monthsRegex = Regex("""(\d+)\s*(شهر|أشهر|اشهر|شهور|شهراً|شهرا)""")
                    val match = monthsRegex.find(normalizedLine)
                    if (match != null) {
                        duration = match.groupValues[1].toDoubleOrNull() ?: 1.0
                    } else {
                        if (normalizedLine.contains("ثلاث")) duration = 3.0
                        else if (normalizedLine.contains("أربع") || normalizedLine.contains("اربع")) duration = 4.0
                        else if (normalizedLine.contains("خمس")) duration = 5.0
                        else if (normalizedLine.contains("ست")) duration = 6.0
                        else if (normalizedLine.contains("سبع")) duration = 7.0
                        else if (normalizedLine.contains("ثمان")) duration = 8.0
                        else if (normalizedLine.contains("تسع")) duration = 9.0
                        else if (normalizedLine.contains("عشر")) duration = 10.0
                    }
                }
            }
            
            // 4. Device number extraction
            var deviceNumber = ""
            val deviceRegex = Regex("""(جهاز|ريسيفر|رقم|hd)\s*(?:رقم)?\s*([a-zA-Z0-9]+)""")
            val deviceMatch = deviceRegex.find(normalizedLine)
            if (deviceMatch != null) {
                val potentialVal = deviceMatch.groupValues[2]
                if (potentialVal != phoneNumber && !subscriptionDate.contains(potentialVal) && potentialVal.length < 15) {
                    deviceNumber = potentialVal
                }
            }
            
            // 5. Address extraction from Aleppo neighborhoods
            var address = ""
            for (place in aleppoNeighborhoods) {
                if (normalizedLine.contains(place)) {
                    address = place
                    break
                }
            }
            
            // 6. Name extraction
            var name = ""
            
            val parts = line.split('-', ',', '،', '|', '/').map { it.trim() }.filter { it.isNotBlank() }
            if (parts.isNotEmpty()) {
                var candidate = parts[0]
                candidate = candidate
                    .replace(Regex("""\b(اشتراك|أشترك|مشترك|رقم|جهاز|ريسيفر|تفعيل|تجديد|شاشة|باقة|من|إلى|الى|اليوم|أمس|امس|البارحة|جديد)\b"""), "")
                    .replace(Regex("""[0-9٠-٩]+"""), "")
                    .trim()
                if (candidate.length >= 2) {
                    name = candidate
                }
            }
            
            if (name.isBlank() || name.length < 2) {
                val stopWords = setOf(
                    "اشتراك", "أشترك", "مشترك", "تجديد", "تفعيل", "باقة", "باقات", "من", "إلى", "الى", "جهاز", "ريسيفر", "رقم",
                    "سنة", "شهر", "أشهر", "اشهر", "شهور", "شهرين", "عام", "أيام", "ايام", "يوم", "يوما", "يوماً", "أسبوع", "اسبوع",
                    "أسبوعين", "اسبوعين", "عنوان", "الهاتف", "هاتف", "جوال", "موبايل", "تاريخ", "الافتراضي", "البث", "منظومة", "شاشة",
                    "جديد", "اليوم", "أمس", "امس", "البارحة"
                )
                val neighborhoodStopWords = setOf(
                    "بستان", "القصر", "الكلاسة", "صلاح", "الدين", "كرم", "الجبل", "الفرقان", "حلب", "الجديدة",
                    "المشهد", "الجميلية", "سيف", "الدولة", "الشهباء", "الحمدانية", "الموكامبو",
                    "الشعار", "طريق", "الباب", "السكري", "الميدان", "الأعظمية", "المرجة", "الصاخور",
                    "مساكن", "هنانو", "الأنصاري", "باب", "النيرب", "جب", "القبة", "المغاير", "الفردوس",
                    "الباشا", "السبيل", "الخالدية", "المحافظة", "النيال", "السريان", "العزيزية", "السليمانية"
                )

                val words = line.split(' ').map { it.trim().replace(Regex("""[+\-(),،.|/\\\\[\\]]"""), "") }.filter { it.isNotBlank() }
                val nameWords = mutableListOf<String>()
                for (word in words) {
                    val normalizedWord = convertArabicIndianDigitsToEnglish(word)
                    if (normalizedWord.any { it.isDigit() }) break
                    if (stopWords.contains(word) || neighborhoodStopWords.contains(word)) continue
                    nameWords.add(word)
                    if (nameWords.size >= 3) break
                }
                name = nameWords.joinToString(" ")
            }
            
            if (name.isBlank()) {
                name = "مشترك جديد"
            }
            
            val subscriberMap = mapOf<String, Any>(
                "name" to name,
                "phone_number" to phoneNumber,
                "subscription_date" to subscriptionDate,
                "subscription_duration" to duration,
                "subscription_duration_type" to durationType,
                "device_number" to deviceNumber,
                "address" to address
            )
            results.add(subscriberMap)
        }
        
        return results
    }

    private fun convertArabicIndianDigitsToEnglish(input: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var result = input
        for (i in 0..9) {
            result = result.replace(arabicDigits[i], i.toString().first())
        }
        return result
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        }
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }
}
