package com.example.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.example.ui.components.HeaderComponent
import com.example.ui.viewmodel.SubscriberViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriberScreen(
    viewModel: SubscriberViewModel,
    navController: NavController,
    subscriberId: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSubs by viewModel.subscribers.collectAsState()

    // Find existing subscriber if in edit mode
    val existingSubscriber = remember(subscriberId, allSubs) {
        if (subscriberId != null) allSubs.find { it.id == subscriberId } else null
    }

    val isEditMode = existingSubscriber != null

    // Form fields hold local state
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var subscriptionDate by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("1") }
    var durationType by remember { mutableStateOf("months") } // "months" or "days"
    var deviceNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Validation State
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var showWelcomeWhatsAppDialog by remember { mutableStateOf(false) }
    var welcomeDialogData by remember { mutableStateOf<com.example.data.Subscriber?>(null) }

    // Load initial values if editing
    LaunchedEffect(existingSubscriber) {
        if (existingSubscriber != null) {
            name = existingSubscriber.name
            phoneNumber = existingSubscriber.phoneNumber
            subscriptionDate = existingSubscriber.subscriptionDate
            duration = existingSubscriber.subscriptionDuration.toString()
            durationType = existingSubscriber.subscriptionDurationType
            deviceNumber = existingSubscriber.deviceNumber
            address = existingSubscriber.address
        } else {
            // Default subscription date to today
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            subscriptionDate = today
        }
    }

    // Calculated End Date
    val calculatedEndDate = remember(subscriptionDate, duration, durationType) {
        val durationInt = duration.toIntOrNull() ?: 0
        viewModel.calculateEndDate(subscriptionDate, durationInt, durationType)
    }

    // Setup native calendar picker function
    val showDatePicker: () -> Unit = {
        try {
            val calendar = Calendar.getInstance()
            if (subscriptionDate.isNotBlank()) {
                val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(subscriptionDate)
                if (parsedDate != null) {
                    calendar.time = parsedDate
                }
            }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formattedMonth = String.format(Locale.US, "%02d", month + 1)
                    val formattedDay = String.format(Locale.US, "%02d", dayOfMonth)
                    subscriptionDate = "$year-$formattedMonth-$formattedDay"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        } catch (e: Exception) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formattedMonth = String.format(Locale.US, "%02d", month + 1)
                    val formattedDay = String.format(Locale.US, "%02d", dayOfMonth)
                    subscriptionDate = "$year-$formattedMonth-$formattedDay"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    Scaffold(
        topBar = {
            HeaderComponent(
                currentRoute = if (isEditMode) "subscribers" else "add_subscriber",
                navController = navController
            )
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Form Title header card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) "تعديل بيانات المشترك" else "إضافة مشترك جديد لشبكة X SPORT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.EditCalendar else Icons.Default.PersonAdd,
                            contentDescription = "Form Title Icon",
                            tint = SportOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- Form Inputs in Arabic rtl flow ---

            // 1. Name Input
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "اسم المشترك *")
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = null
                        },
                        placeholder = {
                            Text(
                                "أدخل الاسم الكامل للمشترك",
                                color = MutedText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        isError = nameError != null,
                        singleLine = true,
                        colors = formFieldColors(),
                        textStyle = formFieldTextStyle(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp)
                        )
                    }
                }
            }

            // 2. Phone Input (locked to numbers layout LTR)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "رقم الهاتف *")
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            if (it.isNotBlank()) phoneError = null
                        },
                        placeholder = {
                            Text(
                                "09XX XXX XXX",
                                color = MutedText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        isError = phoneError != null,
                        singleLine = true,
                        colors = formFieldColors(),
                        textStyle = formFieldTextStyle().copy(textAlign = TextAlign.Right),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (phoneError != null) {
                        Text(
                            text = phoneError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp)
                        )
                    }
                }
            }

            // 3. Subscription Date Input (DatePicker clicker)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "تاريخ الاشتراك *")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker() }
                            .padding(end = 16.dp, start = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "اختر تاريخ",
                                tint = SportBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = subscriptionDate,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // 4. Duration input grid (Presets: 3 Months, 6 Months, Annual, Custom styled as requested)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "مدة الاشتراك *")
                    
                    // Determine current active preset
                    val selectedPreset = when {
                        duration == "3" && durationType == "months" -> "3months"
                        duration == "6" && durationType == "months" -> "6months"
                        duration == "12" && durationType == "months" -> "annual"
                        else -> "custom"
                    }

                    // Grid - Row 1
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        // Card 1: 3 Months
                        val is3Selected = selectedPreset == "3months"
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (is3Selected) SportOrange.copy(alpha = 0.15f) else CardBackground
                            ),
                            border = BorderStroke(
                                width = if (is3Selected) 2.dp else 1.dp,
                                color = if (is3Selected) SportOrange else BorderColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    duration = "3"
                                    durationType = "months"
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (is3Selected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = SportOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "3 أشهر",
                                        color = if (is3Selected) Color.White else LightText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        // Card 2: 6 Months
                        val is6Selected = selectedPreset == "6months"
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (is6Selected) SportBlue.copy(alpha = 0.15f) else CardBackground
                            ),
                            border = BorderStroke(
                                width = if (is6Selected) 2.dp else 1.dp,
                                color = if (is6Selected) SportBlue else BorderColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    duration = "6"
                                    durationType = "months"
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (is6Selected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = SportBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "ستة أشهر",
                                        color = if (is6Selected) Color.White else LightText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Grid - Row 2
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Card 3: Annual (12 Months / سنوي)
                        val isAnnualSelected = selectedPreset == "annual"
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAnnualSelected) SportGold.copy(alpha = 0.15f) else CardBackground
                            ),
                            border = BorderStroke(
                                width = if (isAnnualSelected) 2.dp else 1.dp,
                                color = if (isAnnualSelected) SportGold else BorderColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    duration = "12"
                                    durationType = "months"
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isAnnualSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = SportGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "سنوي",
                                        color = if (isAnnualSelected) Color.White else LightText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        // Card 4: Custom Option (مدة مخصصة)
                        val isCustomSelected = selectedPreset == "custom"
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCustomSelected) TraditionalTeal.copy(alpha = 0.15f) else CardBackground
                            ),
                            border = BorderStroke(
                                width = if (isCustomSelected) 2.dp else 1.dp,
                                color = if (isCustomSelected) TraditionalTeal else BorderColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    if (selectedPreset != "custom") {
                                        duration = "1"
                                        durationType = "months"
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCustomSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = TraditionalTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "مدة مخصصة",
                                        color = if (isCustomSelected) Color.White else LightText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Reveal original numeric/type picker inputs only if Custom is selected
                    if (selectedPreset == "custom") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Type selection days vs months
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel(text = "نوع المدة *")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CardBackground)
                                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(if (durationType == "days") SportBlue.copy(alpha = 0.2f) else Color.Transparent)
                                                .clickable { durationType = "days" }
                                        ) {
                                            Text(
                                                text = "أيام",
                                                color = if (durationType == "days") SportBlue else LightText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        VerticalDivider(
                                            color = BorderColor,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(1.dp)
                                        )
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(if (durationType == "months") SportOrange.copy(alpha = 0.15f) else Color.Transparent)
                                                .clickable { durationType = "months" }
                                        ) {
                                            Text(
                                                text = "أشهر",
                                                color = if (durationType == "months") SportOrange else LightText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                // Numeric Value Text Field
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel(text = "القيمة المخصصة *")
                                    OutlinedTextField(
                                        value = duration,
                                        onValueChange = {
                                            val cleanNum = it.filter { char -> char.isDigit() }
                                            duration = cleanNum.ifEmpty { "1" }
                                        },
                                        singleLine = true,
                                        colors = formFieldColors(),
                                        textStyle = formFieldTextStyle(),
                                        shape = RoundedCornerShape(10.dp),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Automated calculated End date read only
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "تاريخ الانتهاء (حساب تلقائي)")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = "تاريخ وميعاد الانتهاء",
                                tint = SportAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = calculatedEndDate,
                                color = SportAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }

            // 6. Device Number Input
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "رقم الجهاز والريسيفر")
                    OutlinedTextField(
                        value = deviceNumber,
                        onValueChange = { deviceNumber = it },
                        placeholder = {
                            Text(
                                "أدخل رقم ريسيفر أو كود المشترك إن وجد",
                                color = MutedText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        singleLine = true,
                        colors = formFieldColors(),
                        textStyle = formFieldTextStyle(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 7. Address Input
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FormLabel(text = "العنوان والمنطقة")
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = {
                            Text(
                                "أدخل عنوان المشترك أو الحي (مثال: حي الفرقان)",
                                color = MutedText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        singleLine = true,
                        colors = formFieldColors(),
                        textStyle = formFieldTextStyle(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Spacer before buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 8. Submit and Cancel Action buttons in row
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = "إلغاء",
                            color = LightText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Save / Update
                    Button(
                        onClick = {
                            // Trim fields & Validate
                            val trimmedName = name.trim()
                            val trimmedPhone = phoneNumber.trim()
                            var valid = true

                            if (trimmedName.isEmpty()) {
                                nameError = "الرجاء كود أو إدخال اسم المشترك"
                                valid = false
                            } else {
                                nameError = null
                            }

                            if (trimmedPhone.isEmpty()) {
                                phoneError = "الرجاء إدخال رقم الهاتف للتواصل"
                                valid = false
                            } else {
                                phoneError = null
                            }

                            if (valid) {
                                val cleanDuration = duration.toIntOrNull() ?: 1
                                if (isEditMode) {
                                    viewModel.updateSubscriber(
                                        id = existingSubscriber!!.id,
                                        name = trimmedName,
                                        phoneNumber = trimmedPhone,
                                        subscriptionDate = subscriptionDate,
                                        duration = cleanDuration,
                                        durationType = durationType,
                                        deviceNumber = deviceNumber.trim(),
                                        address = address.trim(),
                                        onSuccess = {
                                            Toast.makeText(context, "تم تحديث بيانات المشترك بنجاح", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                    )
                                } else {
                                    viewModel.addSubscriber(
                                        name = trimmedName,
                                        phoneNumber = trimmedPhone,
                                        subscriptionDate = subscriptionDate,
                                        duration = cleanDuration,
                                        durationType = durationType,
                                        deviceNumber = deviceNumber.trim(),
                                        address = address.trim(),
                                        onSuccess = {
                                            Toast.makeText(context, "تم إضافة المشترك بنجاح", Toast.LENGTH_SHORT).show()
                                            // Prepare WhatsApp welcome dialog
                                            welcomeDialogData = com.example.data.Subscriber(
                                                name = trimmedName,
                                                phoneNumber = trimmedPhone,
                                                subscriptionDate = subscriptionDate,
                                                subscriptionDuration = cleanDuration,
                                                subscriptionDurationType = durationType,
                                                endDate = calculatedEndDate,
                                                deviceNumber = deviceNumber.trim(),
                                                address = address.trim()
                                            )
                                            showWelcomeWhatsAppDialog = true
                                        }
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "تحديث البيانات" else "تأكيد إضافة المشترك",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Padding at end
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showWelcomeWhatsAppDialog && welcomeDialogData != null) {
        val subData = welcomeDialogData!!
        val durationLabelText = if (subData.subscriptionDurationType == "months") {
            "${subData.subscriptionDuration} أشهر"
        } else {
            "${subData.subscriptionDuration} يوم"
        }
        
        var messageText by remember(subData) {
            mutableStateOf(
                "أهلاً بك سيد ${subData.name} في شبكة X SPORT! ⚽📺\n" +
                "تم تفعيل اشتراكك بنجاح لتبدأ مشاهدة ممتعة.\n\n" +
                "📅 تاريخ البدء: ${subData.subscriptionDate}\n" +
                "⏳ تاريخ الانتهاء: ${subData.endDate}\n" +
                "📦 الباقة: $durationLabelText\n" +
                (if (subData.deviceNumber.isNotEmpty()) "🔑 رقم الجهاز: ${subData.deviceNumber}\n" else "") +
                "\nنتمنى لك أوقاتاً رائعة واشتراكاً مستقراً ومميزاً! 📡✨"
            )
        }

        AlertDialog(
            onDismissRequest = {
                showWelcomeWhatsAppDialog = false
                navController.navigate("subscribers") {
                    popUpTo("home") { inclusive = false }
                }
            },
            title = {
                Text(
                    text = "إرسال رسالة ترحيبية عبر الواتساب 💬",
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
                        text = "تم إضافة المشترك بنجاح! 🎉 يمكنك تعديل الرسالة الترحيبية أدناه قبل إرسالها:",
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
                            val cleanPhone = subData.phoneNumber.filter { it.isDigit() }
                            val formattedPhone = if (cleanPhone.startsWith("0")) {
                                "963" + cleanPhone.substring(1)
                            } else if (!cleanPhone.startsWith("963") && cleanPhone.length == 9) {
                                "963" + cleanPhone
                            } else {
                                cleanPhone
                            }
                            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${android.net.Uri.encode(messageText)}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح تطبيق واتساب ❌", Toast.LENGTH_SHORT).show()
                        }
                        
                        showWelcomeWhatsAppDialog = false
                        navController.navigate("subscribers") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
                ) {
                    Text("إرسال عبر واتساب 💬", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWelcomeWhatsAppDialog = false
                        navController.navigate("subscribers") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                ) {
                    Text(text = "تخطي ❌", color = LightText)
                }
            },
            containerColor = CardBackground,
            textContentColor = LightText,
            titleContentColor = Color.White
        )
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = LightText,
        textAlign = TextAlign.Right,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 2.dp)
    )
}

@Composable
fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SportOrange,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedTextColor = LightText,
    unfocusedTextColor = LightText,
    errorBorderColor = MaterialTheme.colorScheme.error
)

@Composable
fun formFieldTextStyle() = androidx.compose.ui.text.TextStyle(
    textAlign = TextAlign.Right,
    fontSize = 15.sp,
    fontWeight = FontWeight.Medium
)
