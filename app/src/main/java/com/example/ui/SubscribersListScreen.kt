package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.Subscriber
import com.example.ui.components.HeaderComponent
import com.example.ui.viewmodel.SubscriberViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribersListScreen(
    viewModel: SubscriberViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val subscribers by viewModel.filteredSubscribers.collectAsState()
    val allSubs by viewModel.subscribers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Dialog state
    var showDeleteConfirmDialogBySubscriberId by remember { mutableStateOf<Int?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRenewConfirmDialog by remember { mutableStateOf<Pair<Subscriber, Int>?>(null) }
    var showRenewWhatsAppDialog by remember { mutableStateOf<Pair<Subscriber, Int>?>(null) }
    var showCustomRenewDialog by remember { mutableStateOf<Subscriber?>(null) }

    // WhatsApp/File Import States
    var whatsappText by remember { mutableStateOf("") }
    var isParsing by remember { mutableStateOf(false) }
    var parsingError by remember { mutableStateOf<String?>(null) }
    var parsedSubscribers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // File Picker Launcher for ZIP/TXT WhatsApp chat logs
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectAndParseFile(
                context = context,
                uri = it,
                onStart = {
                    isParsing = true
                    parsingError = null
                },
                onResult = { results ->
                    isParsing = false
                    if (results.isEmpty()) {
                        parsingError = "لم نتمكن من استخراج أي مشتركين من الملف المحدد. يرجى التأكد من صياغة الملف والرسائل بداخله."
                    } else {
                        parsedSubscribers = results
                        Toast.makeText(context, "تم قراءة الملف بنجاح! تم استخراج ${results.size} مشتركين ✨", Toast.LENGTH_LONG).show()
                    }
                },
                onError = { err ->
                    isParsing = false
                    parsingError = err
                }
            )
        }
    }

    // Reset import states when the backup dialog is closed
    LaunchedEffect(showBackupDialog) {
        if (!showBackupDialog) {
            whatsappText = ""
            isParsing = false
            parsingError = null
            parsedSubscribers = emptyList()
        }
    }

    // Count statistics
    val totalCount = allSubs.size
    val activeCount = allSubs.count { isSubscriptionActive(it.endDate) }
    val expiredCount = totalCount - activeCount

    Scaffold(
        topBar = { HeaderComponent(currentRoute = "subscribers", navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_subscriber") },
                containerColor = SportOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مشترك")
            }
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Search Bar Field (Arabic rtl layout)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "ابحث بالاسم، الهاتف، الجهاز أو العنوان...",
                        color = MutedText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = MutedText
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MutedText
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportOrange,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Right,
                    fontSize = 14.sp
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // 2. Control stats & backup button
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                // Left - backup actions button
                OutlinedButton(
                    onClick = { showBackupDialog = true },
                    border = BorderStroke(
                        width = 1.dp,
                        color = SportBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "Backup",
                        tint = SportBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "استيراد / تصدير",
                        color = SportBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Right - Counts indicator
                Text(
                    text = "المشتركين: $totalCount (نشط: $activeCount | منتهي: $expiredCount)",
                    color = LightText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
            }

            // Nearing Expiration Alerts Banner
            val nearingExpirationSubs = allSubs.filter {
                val days = getSubscriptionRemainingDays(it.endDate)
                days in 0..5
            }
            if (nearingExpirationSubs.isNotEmpty()) {
                var showNearingDialog by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SportOrange.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SportOrange.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .clickable { showNearingDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "عرض التفاصيل",
                            tint = SportOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "تنبيه: يوجد ${nearingExpirationSubs.size} اشتراكات تنتهي قريباً (خلال ٥ أيام أو أقل) ⚠️",
                            color = SportOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                    }
                }
                
                if (showNearingDialog) {
                    AlertDialog(
                        onDismissRequest = { showNearingDialog = false },
                        title = {
                            Text(
                                text = "تنبيه الاشتراكات القريبة من الانتهاء ⚠️",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "يرجى مراجعة وتنبيه المشتركين التاليين لتجديد باقاتهم المتاحة:",
                                    color = LightText,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(nearingExpirationSubs) { sub ->
                                            val days = getSubscriptionRemainingDays(sub.endDate)
                                            val daysText = if (days == 0) "اليوم!" else "خلال $days أيام"
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(CardBackground.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        showNearingDialog = false
                                                        showCustomRenewDialog = sub
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("تجديد ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                ) {
                                                    Text(
                                                        text = sub.name,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        textAlign = TextAlign.Right
                                                    )
                                                    Text(
                                                        text = "تاريخ الانتهاء: ${sub.endDate} ($daysText)",
                                                        color = SportAmber,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Right
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showNearingDialog = false }) {
                                Text(text = "إغلاق ❌", color = LightText)
                            }
                        },
                        containerColor = DarkBackground,
                        titleContentColor = Color.White
                    )
                }
            }

            // Custom elegant filter chips for sports network states
            val currentFilter by viewModel.statusFilter.collectAsState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                listOf(
                    com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.ALL to "الكل 👥",
                    com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.ACTIVE to "النشطين ✅",
                    com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.EXPIRED to "المنتهين ❌"
                ).forEach { (filterOption, label) ->
                    val isSelected = currentFilter == filterOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) SportOrange else CardBackground
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SportOrange else BorderColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setStatusFilter(filterOption) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else LightText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // 3. List
            if (subscribers.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = CardBackground,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .size(72.dp)
                                .border(1.dp, BorderColor, RoundedCornerShape(50))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PeopleOutline,
                                    contentDescription = "No subscribers",
                                    tint = MutedText,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val emptyTitle = when {
                            searchQuery.isNotBlank() -> "لم يتم العثور على نتائج للبحث"
                            currentFilter == com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.ACTIVE -> "لا يوجد مشتركين نشطين حالياً"
                            currentFilter == com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.EXPIRED -> "لا يوجد مشتركين منتهية اشتراكاتهم حالياً"
                            else -> "قائمة المشتركين فارغة حالياً"
                        }
                        val emptySub = when {
                            searchQuery.isNotBlank() -> "يرجى المحاولة مجدداً بكلمات بحث أخرى"
                            currentFilter == com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.ACTIVE -> "جميع الاشتراكات الحالية منتهية الصلاحية"
                            currentFilter == com.example.ui.viewmodel.SubscriberViewModel.SubscriptionFilter.EXPIRED -> "جميع الاشتراكات الحالية نشطة ومستمرة!"
                            else -> "اضغط على الزر أدناه لإضافة أول مشترك في منظومة البث"
                        }
                        Text(
                            text = emptyTitle,
                            color = LightText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = emptySub,
                            color = MutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(subscribers, key = { it.id }) { subscriber ->
                        SubscriberCard(
                            subscriber = subscriber,
                            isActive = isSubscriptionActive(subscriber.endDate),
                            onCall = { phone ->
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لم يتمكن الجهاز من إجراء المكالمة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEdit = { id ->
                                navController.navigate("add_subscriber?id=$id")
                            },
                            onDelete = { id ->
                                showDeleteConfirmDialogBySubscriberId = id
                            },
                            onRenewClick = { sub, months ->
                                showRenewConfirmDialog = sub to months
                            }
                        )
                    }
                }
            }
        }
    }

    // 4. Delete Confirmation Dialog
    if (showDeleteConfirmDialogBySubscriberId != null) {
        val targetId = showDeleteConfirmDialogBySubscriberId!!
        val targetSub = allSubs.find { it.id == targetId }
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialogBySubscriberId = null },
            title = {
                Text(
                    text = "تأكيد حذف المشترك",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف المشترك '${targetSub?.name}' نهائياً؟ لا يمكن التراجع عن هذا الإجراء.",
                    textAlign = TextAlign.Right,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscriber(targetId)
                        showDeleteConfirmDialogBySubscriberId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialogBySubscriberId = null }) {
                    Text(text = "إلغاء", color = LightText)
                }
            },
            containerColor = CardBackground,
            textContentColor = LightText,
            titleContentColor = Color.White
        )
    }

    // 4.5. Renewal Confirmation Dialog
    if (showRenewConfirmDialog != null) {
        val (subscriber, months) = showRenewConfirmDialog!!
        val durationLabel = when (months) {
            3 -> "٣ أشهر"
            6 -> "٦ أشهر"
            12 -> "سنة كاملة (١٢ شهراً)"
            else -> "$months أشهر"
        }
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Date()
        val todayStr = sdf.format(today)
        var previewStartDateStr = todayStr
        try {
            val currentEndDate = sdf.parse(subscriber.endDate)
            if (currentEndDate != null && currentEndDate.after(today)) {
                previewStartDateStr = subscriber.endDate
            }
        } catch (e: Exception) {}

        val calendar = Calendar.getInstance()
        try {
            val startDate = sdf.parse(previewStartDateStr) ?: today
            calendar.time = startDate
        } catch (e: Exception) {
            calendar.time = today
        }
        calendar.add(Calendar.MONTH, months)
        val previewEndDateStr = sdf.format(calendar.time)

        AlertDialog(
            onDismissRequest = { showRenewConfirmDialog = null },
            title = {
                Text(
                    text = "تأكيد تجديد الاشتراك ⏳",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "أنت على وشك تجديد الاشتراك للمشترك:",
                        textAlign = TextAlign.Right,
                        fontSize = 14.sp,
                        color = MutedText,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = subscriber.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "الباقة المختارة: $durationLabel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SportOrange,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = previewStartDateStr,
                            color = LightText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "تاريخ البدء:",
                            color = MutedText,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = previewEndDateStr,
                            color = SportAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "تاريخ الانتهاء الجديد:",
                            color = MutedText,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renewSubscriber(subscriber, months) {
                            Toast.makeText(context, "تم تجديد اشتراك ${subscriber.name} بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                            showRenewWhatsAppDialog = Pair(subscriber, months)
                        }
                        showRenewConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    Text("تأكيد التجديد ✅", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenewConfirmDialog = null }) {
                    Text(text = "إلغاء ❌", color = LightText)
                }
            },
            containerColor = CardBackground,
            textContentColor = LightText,
            titleContentColor = Color.White
        )
    }

    if (showRenewWhatsAppDialog != null) {
        val (subscriber, months) = showRenewWhatsAppDialog!!
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Date()
        val calendar = Calendar.getInstance()
        val isExpired = !isSubscriptionActive(subscriber.endDate)
        if (!isExpired) {
            val currentEndDate = sdf.parse(subscriber.endDate) ?: today
            calendar.time = currentEndDate
        } else {
            calendar.time = today
        }
        calendar.add(Calendar.MONTH, months)
        val newEndDateStr = sdf.format(calendar.time)

        val durationLabelText = "$months أشهر"
        
        var messageText by remember(subscriber, months) {
            mutableStateOf(
                "مرحباً سيد ${subscriber.name}، تم تجديد اشتراكك بنجاح في شبكة X SPORT! ⚽🔥\n" +
                "📦 الباقة المجددة: $durationLabelText\n" +
                "📅 تاريخ الانتهاء الجديد: $newEndDateStr\n\n" +
                "شكراً لثقتكم المستمرة بنا ونتمنى لكم مشاهدة ممتعة وممتازة! 📡🍿"
            )
        }

        AlertDialog(
            onDismissRequest = { showRenewWhatsAppDialog = null },
            title = {
                Text(
                    text = "إرسال رسالة تجديد الاشتراك عبر الواتساب 💬",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تم تجديد الاشتراك بنجاح! 🎉 يمكنك تعديل رسالة التجديد أدناه قبل إرسالها للعميل:",
                        textAlign = TextAlign.Right,
                        fontSize = 13.sp,
                        color = MutedText,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = LightText, textAlign = TextAlign.Right),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportOrange,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val cleanPhone = subscriber.phoneNumber.filter { it.isDigit() }
                            val formattedPhone = if (cleanPhone.startsWith("0")) {
                                "963" + cleanPhone.substring(1)
                            } else if (!cleanPhone.startsWith("963") && cleanPhone.length == 9) {
                                "963" + cleanPhone
                            } else {
                                cleanPhone
                            }
                            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(messageText)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح تطبيق واتساب ❌", Toast.LENGTH_SHORT).show()
                        }
                        showRenewWhatsAppDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    Text("إرسال عبر واتساب 💬", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenewWhatsAppDialog = null }) {
                    Text(text = "تخطي ❌", color = LightText)
                }
            },
            containerColor = CardBackground,
            textContentColor = LightText,
            titleContentColor = Color.White
        )
    }

    // 5. Import/Export Backup Dialog with Smart AI Import
    if (showBackupDialog) {
        var importText by remember { mutableStateOf("") }
        var activeTab by remember { mutableStateOf(2) } // 0 = Export, 1 = JSON Import, 2 = AI WhatsApp Import
        
        AlertDialog(
            onDismissRequest = { 
                if (!isParsing) showBackupDialog = false 
            },
            title = {
                Text(
                    text = "إدارة واستيراد البيانات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Tabs Header
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = Color.Transparent,
                        contentColor = SportOrange,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("استيراد من واتساب ✨", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("استيراد JSON", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("تصدير بيانات", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (activeTab == 0) {
                        // EXPORT TAB
                        val jsonString = viewModel.exportToJsonString()
                        Text(
                            text = "انسخ النص البرمجي أدناه واحتفظ به في مكان آمن لاستعادة بيانات المشتركين لاحقاً:",
                            fontSize = 12.sp,
                            color = MutedText,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = jsonString.ifEmpty { "[]" },
                                color = SportBlue,
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (jsonString.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(jsonString))
                                    Toast.makeText(context, "تم نسخ رمز الاحتياطي بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("نسخ رمز النسخة الاحتياطية", fontSize = 13.sp)
                        }
                    } else if (activeTab == 1) {
                        // IMPORT JSON TAB
                        Text(
                            text = "ألصق رمز النسخة الاحتياطية (تنسيق JSON) أدناه لاستعادة قائمة المشتركين:",
                            fontSize = 12.sp,
                            color = MutedText,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = LightText),
                            placeholder = {
                                Text(
                                    text = "[ {\"name\": \"...\"}, ... ]",
                                    color = MutedText,
                                    fontSize = 11.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportBlue,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (importText.isBlank()) {
                                    Toast.makeText(context, "يرجى إلصاق الرمز أولاً", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.importFromJsonString(
                                        jsonStr = importText,
                                        onSuccess = {
                                            Toast.makeText(context, "تم استيراد المشتركين بنجاح", Toast.LENGTH_SHORT).show()
                                            showBackupDialog = false
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, "رمز النسخة الاحتياطية غير صالح: $error", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Publish, contentDescription = "Import", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ابدأ عملية الاستيراد", fontSize = 13.sp)
                        }
                    } else {
                        // SMART AI WHATSAPP IMPORT TAB
                        Text(
                            text = "لأسباب أمنية في نظام أندرويد، لا يمكن لأي تطبيق سحب الرسائل تلقائياً من داخل واتساب حرصاً على خصوصيتك. لتسهيل العملية لأقصى حد، استخدم الأزرار السريعة التالية:",
                            fontSize = 11.sp,
                            color = MutedText,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))

                        // Smart Assisted Automation row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Open WhatsApp Button
                            Button(
                                onClick = {
                                    try {
                                        val businessIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp.w4b")
                                        val personalIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                                        if (businessIntent != null) {
                                            context.startActivity(businessIntent)
                                        } else if (personalIntent != null) {
                                            context.startActivity(personalIntent)
                                        } else {
                                            val whatsappUriIntent = Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://"))
                                            whatsappUriIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(whatsappUriIntent)
                                        }
                                    } catch (e: Exception) {
                                        try {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com"))
                                            context.startActivity(webIntent)
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "يرجى فتح واتساب ونسخ النص يدوياً", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open WhatsApp",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("افتح واتساب 💬", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            // 2. Smart Paste Button
                            Button(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        whatsappText = clipText
                                        Toast.makeText(context, "تم قراءة ولصق النص من الحافظة! ✨", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الحافظة فارغة! يرجى نسخ الرسائل من واتساب أولاً", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(38.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste Clipboard",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لصق ذكي للحافظة 📋", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Import chat export file (.zip or .txt)
                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Publish,
                                contentDescription = "Import Chat Log File",
                                modifier = Modifier.size(15.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد ملف المحادثة المصدّر (ZIP / TXT) 📁", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "💡 خطوتك للسرعة: من خيارات الدردشة في واتساب ➜ المزيد ➜ نقل الدردشة (دون وسائط) ثم حدد الملف المصدّر هنا لتتم معالجة المشتركين بالكامل تلقائياً!",
                            fontSize = 10.sp,
                            color = SportAmber,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (parsedSubscribers.isEmpty()) {
                            // Let the user paste raw text
                            OutlinedTextField(
                                value = whatsappText,
                                onValueChange = { whatsappText = it },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = LightText, textAlign = TextAlign.Right),
                                placeholder = {
                                    Text(
                                        text = "مثال:\nأبو عماد - ٠٩٤٤٨٨٩٩٠٠ - اشتراك ٣ أشهر من ١٠-٥-٢٠٢٦ كرم الجبل\nخالد محمد، اشتراك شهرين جهاز hd990 من أول أمس",
                                        color = MutedText,
                                        fontSize = 11.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportOrange,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground,
                                    focusedTextColor = LightText,
                                    unfocusedTextColor = LightText
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(8.dp)
                            )

                            parsingError?.let { err ->
                                Text(
                                    text = "⚠️ خطأ في التحليل المتقدم: $err",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (whatsappText.trim().isBlank()) {
                                        Toast.makeText(context, "الرجاء كتابة أو إلصاق نص الرسائل أولاً", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isParsing = true
                                        parsingError = null
                                        viewModel.parseWhatsAppText(
                                            rawText = whatsappText,
                                            onResult = { parsed ->
                                                isParsing = false
                                                if (parsed.isEmpty()) {
                                                    parsingError = "لم نتمكن من العثور على مشتركين في النص المُدخل. يرجى مراجعة الصياغة والتأكد من وجود أسماء صحيحة."
                                                } else {
                                                    parsedSubscribers = parsed
                                                }
                                            },
                                            onError = { err ->
                                                isParsing = false
                                                parsingError = err
                                            }
                                        )
                                    }
                                },
                                enabled = !isParsing,
                                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isParsing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري قراءة وتحليل النص بالذكاء الاصطناعي...", fontSize = 12.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Scan", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("معالجة وتحليل بالذكاء الاصطناعي ✨", fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Show preview of parsed subscribers
                            Text(
                                text = "تم العثور على (${parsedSubscribers.size}) مشتركين! يرجى مراجعتها وتأكيد حفظها مباشرة إلى قاعدة بيانات منظومة البث الحية:",
                                fontSize = 12.sp,
                                color = SportAmber,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderColor),
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                            ) {
                                LazyColumn(
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(parsedSubscribers) { map ->
                                        val name = map["name"]?.toString() ?: ""
                                        val phone = map["phone_number"]?.toString() ?: ""
                                        val date = map["subscription_date"]?.toString() ?: ""
                                        val durationRaw = map["subscription_duration"]
                                        val duration = when (durationRaw) {
                                            is Double -> durationRaw.toInt()
                                            is Int -> durationRaw
                                            is String -> durationRaw.toIntOrNull() ?: 1
                                            else -> 1
                                        }
                                        val type = if (map["subscription_duration_type"]?.toString() == "days") "أيام" else "أشهر"
                                        val device = map["device_number"]?.toString() ?: ""
                                        val address = map["address"]?.toString() ?: ""

                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    CardBackground.copy(alpha = 0.5f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "✓ $name ${if (phone.isNotBlank()) "($phone)" else ""}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Right
                                                )
                                                Text(
                                                    text = "باقة: $duration $type | البدء: $date${if (device.isNotBlank()) " | جهاز: $device" else ""}${if (address.isNotBlank()) " | عنوان: $address" else ""}",
                                                    color = MutedText,
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        parsedSubscribers = emptyList()
                                    },
                                    border = BorderStroke(1.dp, BorderColor),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تراجع وتصحيح", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveParsedSubscribers(parsedSubscribers) {
                                            Toast.makeText(context, "تم حفظ واستيراد كافة المشتركين المحددين بنجاح!", Toast.LENGTH_LONG).show()
                                            showBackupDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("حفظ واستيراد الكل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showBackupDialog = false },
                    enabled = !isParsing
                ) {
                    Text(text = "إغلاق", color = LightText)
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White
        )
    }
}

@Composable
fun SubscriberCard(
    subscriber: Subscriber,
    isActive: Boolean,
    onCall: (String) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onRenewClick: (Subscriber, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, if (isActive) SportOrange.copy(alpha = 0.3f) else BorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name and Custom Badge in Arabic (RTL)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status Badge (Left)
                Surface(
                    color = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (isActive) Color(0xFF10B981) else Color(0xFFEF4444))
                ) {
                    Text(
                        text = if (isActive) "نشط" else "منتهي الصلاحية",
                        color = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Name (Right)
                Text(
                    text = subscriber.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details body list matching Arabic order with clean labels
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Phone row (with quick dial trigger)
                DetailRow(
                    label = "رقم الهاتف",
                    valueLayout = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .clickable { onCall(subscriber.phoneNumber) }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "اتصال",
                                tint = SportBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subscriber.phoneNumber,
                                color = SportBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                )

                // Device number row
                if (subscriber.deviceNumber.isNotBlank()) {
                    DetailRow(
                        label = "رقم الجهاز",
                        value = subscriber.deviceNumber
                    )
                }

                // Address row
                if (subscriber.address.isNotBlank()) {
                    DetailRow(
                        label = "العنوان",
                        value = subscriber.address
                    )
                }

                // Subscription dates row (start to end)
                DetailRow(
                    label = "مدة الاشتراك",
                    value = "${subscriber.subscriptionDuration} " + if (subscriber.subscriptionDurationType == "days") "أيام" else "أشهر"
                )

                DetailRow(
                    label = "فترة الصلاحية",
                    value = "${subscriber.subscriptionDate}  إلى  ${subscriber.endDate}",
                    valueColor = if (isActive) SportAmber else MutedText
                )

                // Quick renewal layout under period validity with exquisite design
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "تجديد سريع:",
                        color = MutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    QuickRenewPill(
                        label = "٣ أشهر",
                        containerColor = SportOrange,
                        onClick = { onRenewClick(subscriber, 3) }
                    )
                    
                    QuickRenewPill(
                        label = "٦ أشهر",
                        containerColor = SportBlue,
                        onClick = { onRenewClick(subscriber, 6) }
                    )
                    
                    QuickRenewPill(
                        label = "سنوي 👑",
                        containerColor = SportAmber,
                        onClick = { onRenewClick(subscriber, 12) }
                    )
                }
            }

            // Separator Line
            HorizontalDivider(
                color = BorderColor,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Bottom Actions (Edit/Delete) with correct weight spacing
            val context = LocalContext.current
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Delete button
                IconButton(
                    onClick = { onDelete(subscriber.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف المشترك",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // WhatsApp Business Reminder Button in Green color scheme
                Button(
                    onClick = {
                        try {
                            val msg = "مرحباً سيد ${subscriber.name}، نود تذكيرك بأن اشتراكك لشبكة X SPORT ينتهي بتاريخ ${subscriber.endDate}. يرجى التواصل معنا لتجديد الباقة واستمرار البث الرياضي والترفيهي الممتاز. شكراً لثقتكم بنا! ⚽📡"
                            val cleanPhone = subscriber.phoneNumber.filter { it.isDigit() }
                            val formattedPhone = if (cleanPhone.startsWith("0")) {
                                "963" + cleanPhone.substring(1)
                            } else if (!cleanPhone.startsWith("963") && cleanPhone.length == 9) {
                                "963" + cleanPhone
                            } else {
                                cleanPhone
                            }
                            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(msg)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح تطبيق واتساب للأعمال", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "تذكير واتساب",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تذكير واتساب 💬",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Edit button (makes a full touch surface)
                Button(
                    onClick = { onEdit(subscriber.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل البيانات",
                        tint = LightText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تعديل",
                        fontSize = 12.sp,
                        color = LightText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$label :",
            color = MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Right,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    valueLayout: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.weight(1f)
        ) {
            valueLayout()
        }
        Text(
            text = "$label :",
            color = MutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Right,
            modifier = Modifier.width(100.dp)
        )
    }
}

// Global safe date helper
fun isSubscriptionActive(endDateStr: String): Boolean {
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

fun getSubscriptionRemainingDays(endDateStr: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val endDate = sdf.parse(endDateStr) ?: return -1
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val diffInMillis = endDate.time - today.time
        val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        days
    } catch (e: Exception) {
        -1
    }
}

@Composable
fun QuickRenewPill(
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color = Color.White
) {
    Surface(
        onClick = onClick,
        color = containerColor.copy(alpha = 0.12f),
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.4f)),
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = containerColor
            )
        }
    }
}
