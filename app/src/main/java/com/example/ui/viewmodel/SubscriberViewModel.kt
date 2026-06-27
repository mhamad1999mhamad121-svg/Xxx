package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import android.net.Uri
import android.provider.OpenableColumns

class SubscriberViewModel(
    application: Application,
    private val repository: SubscriberRepository
) : AndroidViewModel(application) {

    // List of subscribers
    private val sharedPrefs = application.getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE)

    // Theme mode: Flow of Integer (0 = System, 1 = Light, 2 = Dark)
    private val _themeMode = MutableStateFlow(sharedPrefs.getInt("theme_mode", 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun updateThemeMode(mode: Int) {
        _themeMode.value = mode
        sharedPrefs.edit().putInt("theme_mode", mode).apply()
    }

    val subscribers: StateFlow<List<Subscriber>> = repository.allSubscribers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    enum class SubscriptionFilter {
        ALL, ACTIVE, EXPIRED
    }

    private val _statusFilter = MutableStateFlow(SubscriptionFilter.ALL)
    val statusFilter: StateFlow<SubscriptionFilter> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered list based on search query and status filter
    val filteredSubscribers: StateFlow<List<Subscriber>> = combine(subscribers, _searchQuery, _statusFilter) { list, query, filter ->
        val filteredList = if (query.isBlank()) {
            list
        } else {
            val lowercaseQuery = query.lowercase(Locale.getDefault())
            list.filter {
                it.name.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                        it.phoneNumber.contains(lowercaseQuery) ||
                        it.deviceNumber.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                        it.address.lowercase(Locale.getDefault()).contains(lowercaseQuery)
            }
        }

        when (filter) {
            SubscriptionFilter.ALL -> filteredList
            SubscriptionFilter.ACTIVE -> filteredList.filter { isSubscriptionActiveLocal(it.endDate) }
            SubscriptionFilter.EXPIRED -> filteredList.filter { !isSubscriptionActiveLocal(it.endDate) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun isSubscriptionActiveLocal(endDateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val endDate = sdf.parse(endDateStr) ?: return false
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            !endDate.before(today)
        } catch (e: Exception) {
            false
        }
    }

    fun setStatusFilter(filter: SubscriptionFilter) {
        _statusFilter.value = filter
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addSubscriber(
        name: String,
        phoneNumber: String,
        subscriptionDate: String,
        duration: Int,
        durationType: String,
        deviceNumber: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val calculatedEndDate = calculateEndDate(subscriptionDate, duration, durationType)
            val subscriber = Subscriber(
                name = name,
                phoneNumber = phoneNumber,
                subscriptionDate = subscriptionDate,
                subscriptionDuration = duration,
                subscriptionDurationType = durationType,
                endDate = calculatedEndDate,
                deviceNumber = deviceNumber,
                address = address
            )
            repository.insert(subscriber)
            onSuccess()
        }
    }

    fun updateSubscriber(
        id: Int,
        name: String,
        phoneNumber: String,
        subscriptionDate: String,
        duration: Int,
        durationType: String,
        deviceNumber: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val calculatedEndDate = calculateEndDate(subscriptionDate, duration, durationType)
            val subscriber = Subscriber(
                id = id,
                name = name,
                phoneNumber = phoneNumber,
                subscriptionDate = subscriptionDate,
                subscriptionDuration = duration,
                subscriptionDurationType = durationType,
                endDate = calculatedEndDate,
                deviceNumber = deviceNumber,
                address = address
            )
            repository.update(subscriber)
            onSuccess()
        }
    }

    fun deleteSubscriber(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun renewSubscriber(subscriber: Subscriber, months: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = Date()
            val todayStr = sdf.format(today)
            
            var baseDateStr = todayStr
            try {
                val currentEndDate = sdf.parse(subscriber.endDate)
                if (currentEndDate != null && currentEndDate.after(today)) {
                    baseDateStr = subscriber.endDate
                }
            } catch (e: Exception) {
                // Fallback to today
            }

            val calendar = Calendar.getInstance()
            try {
                val baseDate = sdf.parse(baseDateStr) ?: today
                calendar.time = baseDate
            } catch (e: Exception) {
                calendar.time = today
            }
            
            calendar.add(Calendar.MONTH, months)
            val newEndDateStr = sdf.format(calendar.time)

            val updatedSubscriber = subscriber.copy(
                subscriptionDate = baseDateStr,
                subscriptionDuration = months,
                subscriptionDurationType = "months",
                endDate = newEndDateStr
            )
            repository.update(updatedSubscriber)
            onSuccess()
        }
    }

    fun updateSubscriberWithCustomPeriod(
        subscriber: Subscriber,
        startDate: String,
        duration: Int,
        durationType: String,
        endDate: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val updatedSubscriber = subscriber.copy(
                subscriptionDate = startDate,
                subscriptionDuration = duration,
                subscriptionDurationType = durationType,
                endDate = endDate
            )
            repository.update(updatedSubscriber)
            onSuccess()
        }
    }

    // Helper calculate end date
    fun calculateEndDate(startDateStr: String, duration: Int, durationType: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(startDateStr) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            if (durationType == "days") {
                calendar.add(Calendar.DAY_OF_YEAR, duration)
            } else {
                calendar.add(Calendar.MONTH, duration)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            startDateStr
        }
    }

    // JSON export/import helpers
    private val moshi by lazy { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }
    private val listType by lazy { Types.newParameterizedType(List::class.java, Map::class.java) }
    private val jsonAdapter by lazy { moshi.adapter<List<Map<String, Any>>>(listType) }

    fun exportToJsonString(): String {
        val list = subscribers.value.map {
            mapOf(
                "name" to it.name,
                "phone_number" to it.phoneNumber,
                "subscription_date" to it.subscriptionDate,
                "subscription_duration" to it.subscriptionDuration.toDouble(),
                "subscription_duration_type" to it.subscriptionDurationType,
                "end_date" to it.endDate,
                "device_number" to it.deviceNumber,
                "address" to it.address
            )
        }
        return try {
            jsonAdapter.toJson(list)
        } catch (e: Exception) {
            ""
        }
    }

    fun importFromJsonString(
        jsonStr: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val listToInsert = withContext(Dispatchers.Default) {
                    val parsedList = jsonAdapter.fromJson(jsonStr) ?: throw Exception("Invalid JSON formatting")
                    parsedList.mapNotNull { map ->
                        val name = map["name"]?.toString() ?: ""
                        val phoneNumber = map["phone_number"]?.toString() ?: ""
                        val subscriptionDate = map["subscription_date"]?.toString() ?: ""
                        val durationRaw = map["subscription_duration"]
                        val duration = when (durationRaw) {
                            is Double -> durationRaw.toInt()
                            is Float -> durationRaw.toInt()
                            is Int -> durationRaw
                            is Number -> durationRaw.toInt()
                            is String -> {
                                durationRaw.toIntOrNull() 
                                    ?: durationRaw.toDoubleOrNull()?.toInt() 
                                    ?: 1
                            }
                            else -> 1
                        }
                        val durationType = map["subscription_duration_type"]?.toString() ?: "months"
                        val endDate = map["end_date"]?.toString() ?: calculateEndDate(subscriptionDate, duration, durationType)
                        val deviceNumber = map["device_number"]?.toString() ?: ""
                        val address = map["address"]?.toString() ?: ""

                        if (name.isNotBlank()) {
                            Subscriber(
                                name = name,
                                phoneNumber = phoneNumber,
                                subscriptionDate = subscriptionDate,
                                subscriptionDuration = duration,
                                subscriptionDurationType = durationType,
                                endDate = endDate,
                                deviceNumber = deviceNumber,
                                address = address
                            )
                        } else null
                    }
                }

                if (listToInsert.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        repository.insertAll(listToInsert)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "فشل تحليل أو حفظ النسخة الاحتياطية")
            }
        }
    }

    /**
     * Parse raw WhatsApp chat text using Gemini AI Service helper.
     */
    fun parseWhatsAppText(
        rawText: String,
        onResult: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val results = GeminiService.parseWhatsAppSubscriptions(rawText)
                onResult(results)
            } catch (e: Exception) {
                onError(e.message ?: "حدث خطأ غير معروف أثناء التحليل بالذكاء الاصطناعي")
            }
        }
    }

    /**
     * Batch save WhatsApp imported and verified subscriber maps to local Room Database.
     */
    fun saveParsedSubscribers(
        parsedList: List<Map<String, Any>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val listToInsert = mutableListOf<Subscriber>()
            parsedList.forEach { map ->
                val name = map["name"]?.toString() ?: ""
                val phoneNumber = map["phone_number"]?.toString() ?: ""
                val subscriptionDate = map["subscription_date"]?.toString() ?: ""
                val durationRaw = map["subscription_duration"]
                val duration = when (durationRaw) {
                    is Double -> durationRaw.toInt()
                    is Float -> durationRaw.toInt()
                    is Int -> durationRaw
                    is Number -> durationRaw.toInt()
                    is String -> {
                        durationRaw.toIntOrNull() 
                            ?: durationRaw.toDoubleOrNull()?.toInt() 
                            ?: 1
                    }
                    else -> 1
                }
                val durationType = map["subscription_duration_type"]?.toString() ?: "months"
                val endDate = map["end_date"]?.toString() ?: calculateEndDate(subscriptionDate, duration, durationType)
                val deviceNumber = map["device_number"]?.toString() ?: ""
                val address = map["address"]?.toString() ?: ""

                if (name.isNotBlank()) {
                    listToInsert.add(
                        Subscriber(
                            name = name,
                            phoneNumber = phoneNumber,
                            subscriptionDate = subscriptionDate,
                            subscriptionDuration = duration,
                            subscriptionDurationType = durationType,
                            endDate = endDate,
                            deviceNumber = deviceNumber,
                            address = address
                        )
                    )
                }
            }
            if (listToInsert.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    repository.insertAll(listToInsert)
                }
            }
            onSuccess()
        }
    }

    /**
     * Read a ZIP (containing exported chat txt) or a raw TXT file, clean up metadata,
     * and invoke AI or local parsing to automatically import WhatsApp subscribers.
     */
    fun selectAndParseFile(
        context: android.content.Context,
        uri: Uri,
        onStart: () -> Unit,
        onResult: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        onStart()
        viewModelScope.launch {
            try {
                val extractedText = withContext(Dispatchers.IO) {
                    val contentResolver = context.contentResolver
                    val mimeType = contentResolver.getType(uri)
                    val fileName = getFileName(context, uri) ?: ""

                    if (fileName.endsWith(".zip", ignoreCase = true) || mimeType == "application/zip" || mimeType?.contains("zip") == true) {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            val zipInputStream = ZipInputStream(inputStream)
                            var entry = zipInputStream.nextEntry
                            val stringBuilder = StringBuilder()
                            while (entry != null) {
                                if (!entry.isDirectory && entry.name.endsWith(".txt", ignoreCase = true)) {
                                    val reader = BufferedReader(InputStreamReader(zipInputStream, "UTF-8"))
                                    var line = reader.readLine()
                                    while (line != null) {
                                        stringBuilder.append(line).append("\n")
                                        line = reader.readLine()
                                    }
                                }
                                zipInputStream.closeEntry()
                                entry = zipInputStream.nextEntry
                            }
                            stringBuilder.toString()
                        } ?: ""
                    } else {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                            val stringBuilder = StringBuilder()
                            var line = reader.readLine()
                            while (line != null) {
                                stringBuilder.append(line).append("\n")
                                line = reader.readLine()
                            }
                            stringBuilder.toString()
                        } ?: ""
                    }
                }

                if (extractedText.isBlank()) {
                    onError("الملف فارغ أو لا يحتوي على ملف نصي متوافق (.txt)")
                    return@launch
                }

                // Filter WhatsApp metadata on background dispatcher to avoid blocking the main UI thread.
                val processedText = withContext(Dispatchers.Default) {
                    val linesFiltered = mutableListOf<String>()
                    extractedText.split("\n", "\r").forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotBlank() && !trimmed.contains("encrypted") && !trimmed.contains("سيرفر المشتركين") && !trimmed.contains("تشفير") && !trimmed.contains("تم إنشاؤها") && !trimmed.contains("مضافة من قبل")) {
                            val cleanedLine = removeWhatsAppMetaData(trimmed)
                            if (cleanedLine.isNotBlank()) {
                                linesFiltered.add(cleanedLine)
                            }
                        }
                    }
                    linesFiltered.joinToString("\n")
                }

                if (processedText.isBlank()) {
                    onError("لم يتم العثور على أي رسائل اشتراك صالحة بعد تصفية الملف")
                    return@launch
                }

                // Parse the processed text asynchronously.
                val results = withContext(Dispatchers.IO) {
                    GeminiService.parseWhatsAppSubscriptions(processedText)
                }

                onResult(results)
            } catch (e: Exception) {
                onError("خطأ أثناء استيراد وقراءة الملف: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    private fun getFileName(context: android.content.Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            result = cursor.getString(index)
                        }
                    }
                } finally {
                    cursor?.close()
                }
            } catch (e: Exception) {
                // Return null on failure and let it fall back gracefully
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun removeWhatsAppMetaData(line: String): String {
        var result = line

        if (result.startsWith("[")) {
            val closeBracketIndex = result.indexOf("]")
            if (closeBracketIndex != -1 && closeBracketIndex < 40) {
                result = result.substring(closeBracketIndex + 1).trim()
                val colonIndex = result.indexOf(":")
                if (colonIndex != -1 && colonIndex < 30) {
                    result = result.substring(colonIndex + 1).trim()
                }
                return result
            }
        }

        val hyphenIndex = result.indexOf(" - ")
        if (hyphenIndex != -1 && hyphenIndex < 35) {
            val prefix = result.substring(0, hyphenIndex)
            val hasDigits = prefix.any { it.isDigit() || it == '٠' || it == '١' || it == '٢' || it == '٣' || it == '٤' || it == '٥' || it == '٦' || it == '٧' || it == '٨' || it == '٩' }
            if (hasDigits && (prefix.contains("/") || prefix.contains("-") || prefix.contains(".") || prefix.contains(":"))) {
                result = result.substring(hyphenIndex + 3).trim()
                val colonIndex = result.indexOf(":")
                if (colonIndex != -1 && colonIndex < 30) {
                    result = result.substring(colonIndex + 1).trim()
                    return result
                }
                val arabicColonIndex = result.indexOf("：")
                if (arabicColonIndex != -1 && arabicColonIndex < 30) {
                    result = result.substring(arabicColonIndex + 1).trim()
                    return result
                }
            }
        }

        return result
    }

    // Factory
    class Factory(
        private val application: Application,
        private val repository: SubscriberRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubscriberViewModel::class.java)) {
                return SubscriberViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
