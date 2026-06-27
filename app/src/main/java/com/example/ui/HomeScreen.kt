package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.ui.components.FooterComponent
import com.example.ui.components.HeaderComponent
import com.example.ui.components.AleppoCitadelDraw
import com.example.ui.components.AleppoGeometricStar
import com.example.ui.components.SyrianArchLine
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LightText
import com.example.ui.theme.BorderColor
import com.example.ui.theme.MutedText
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportOrange
import com.example.ui.theme.SportGold
import com.example.ui.theme.TraditionalTeal

/**
 * Custom elegant background with Syrian stars and royal satellite communication wave graphics.
 */
@Composable
fun FaintSportsBackdrop(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        val waveColor = SportOrange.copy(alpha = 0.08f)
        val goldColor = SportAmber.copy(alpha = 0.05f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Floating elegant orbital waves
            drawCircle(
                color = waveColor,
                radius = w * 0.45f,
                center = Offset(w * 0.5f, h * 0.4f),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = goldColor,
                radius = w * 0.3f,
                center = Offset(w * 0.5f, h * 0.4f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Direct beaming vertical transmission line
            drawLine(
                color = waveColor.copy(alpha = 0.15f),
                start = Offset(w * 0.5f, 0f),
                end = Offset(w * 0.5f, h),
                strokeWidth = 2.dp.toPx()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Elegant top ribbons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = "منظومة شبكة X SPORT للبث ⚽",
                    color = SportAmber.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "بث فضائي رقمي مستقر 💎",
                    color = SportOrange.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Premium visual mini status capsules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val capsules = listOf(
                    "أمان وتشفير فائق" to SportOrange,
                    "دعم فني متواصل" to SportAmber,
                    "لوحة تفعيل لحظية" to SportBlue
                )
                capsules.forEach { (text, color) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(color.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            color = color.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        // Luxurious vertical fading gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardBackground.copy(alpha = 0.2f),
                            DarkBackground.copy(alpha = 0.85f),
                            DarkBackground.copy(alpha = 0.99f)
                        )
                    )
                )
        )
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var showWelcomeDialog by rememberSaveable { mutableStateOf(true) }
    var showSubscriptionDialog by rememberSaveable { mutableStateOf(false) }

    if (showWelcomeDialog) {
        Dialog(
            onDismissRequest = { showWelcomeDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(SportGold, TraditionalTeal, SportOrange))
                ),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Aesthetic backdrop element: Subtle Golden Syrian Star in the background corner
                    AleppoGeometricStar(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopStart)
                            .offset(x = (-30).dp, y = (-30).dp),
                        starColor = SportGold.copy(alpha = 0.08f),
                        strokeWidth = 2f
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Badge combining 'SAT' with ancient star geometry
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(84.dp)
                        ) {
                            AleppoGeometricStar(
                                modifier = Modifier.fillMaxSize(),
                                starColor = SportGold.copy(alpha = 0.25f),
                                strokeWidth = 4f
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(SportOrange, SportOrange.copy(alpha = 0.1f))
                                        )
                                    )
                            ) {
                                Text(
                                    text = "SAT",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "يا مية أهلاً وسهلاً بك!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = SportGold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "أهلاً بكم في منظومة وكلاء شبكة X SPORT",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Traditional Decorative line (Syrian Arch)
                        SyrianArchLine(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(16.dp),
                            color = TraditionalTeal.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "مرحباً بكم في بوابتكم المتكاملة لإدارة وتفعيل اشتراكات شبكة X SPORT ومتابعة عملائكم الكرام مع باقات البث الرياضي والترفيهي بأقصى دقة وسرعة وبث رقمي مستقر وممتاز.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { showWelcomeDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SportOrange,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "دخول لوحة التحكم",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSubscriptionDialog) {
        Dialog(
            onDismissRequest = { showSubscriptionDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(SportGold, TraditionalTeal, SportOrange))
                ),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AleppoGeometricStar(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.TopStart)
                            .offset(x = (-25).dp, y = (-25).dp),
                        starColor = SportGold.copy(alpha = 0.08f),
                        strokeWidth = 2f
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = TraditionalTeal.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, TraditionalTeal.copy(alpha = 0.25f)),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "الاشتراكات المرنة",
                                    tint = SportGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "باقات الاشتراك المرنة",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SportGold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "متوفر اشتراك 3شهور وستة شهور وسنوي",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        SyrianArchLine(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(12.dp),
                            color = TraditionalTeal.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Months Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, SportOrange.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Surface(
                                    color = SportOrange.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "3 Months",
                                            tint = SportOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "اشتراك 3 شهور",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "شاهد أقوى المنافسات وبث مباشر متميز ومضمون",
                                        fontSize = 11.sp,
                                        color = MutedText,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }

                        // 6 Months Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, SportBlue.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Surface(
                                    color = SportBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "6 Months",
                                            tint = SportBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "اشتراك ستة شهور",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "الباقة الأكثر مبيعاً مع خصم مميز وتفعيل فوري للوكلاء",
                                        fontSize = 11.sp,
                                        color = MutedText,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }

                        // 1 Year Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.35f)),
                            border = BorderStroke(1.dp, SportGold.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Surface(
                                    color = SportGold.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Annual",
                                            tint = SportGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "اشتراك سنوي (كامل العام)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "باقة X SPORT الذهبية مخصصة للمشاهدة الرياضية والترفيهية اللامحدودة بثبات كامل ممتاز وموفر",
                                        fontSize = 11.sp,
                                        color = MutedText,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showSubscriptionDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "موافق",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { HeaderComponent(currentRoute = "home", navController = navController) },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Hero Section (Gradient Card styled exactly like the user's screenshot)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    // Embed the Live TV faint sports backdrop behind the main titles
                    FaintSportsBackdrop(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(28.dp))
                    )

                    // Draw the Aleppo Citadel outline at the bottom of the card beautifully
                    AleppoCitadelDraw(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 10.dp),
                        primaryColor = SportGold.copy(alpha = 0.08f),
                        secondaryColor = Color.Transparent
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        // 1. Huge Bold White brand name styled beautifully for mobile screens
                        Text(
                            text = "ALEPPO-SAT",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. High-contrast bold Neon Cyan/Teal Satellite Arabic text on two lines, responsive for mobile
                        Text(
                            text = "وكلاء شبكة",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SportOrange, // Neon Cyan matching our new theme
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp
                        )
                        Text(
                            text = "X SPORT",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SportOrange, // Neon Cyan matching our new theme
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. Perfect description text with correct centering and layout styling
                        Text(
                            text = "المنظومة الرقمية المتكاملة لشبكة X SPORT. محطة تحكم شاملة لتفعيل وإدارة اشتراكات البث الرياضي والترفيهي بأعلى مستوى ثبات وجودة.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        // 4. Side-by-side action buttons styled for Aleppo Satellite Space theme
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            // Left outline/translucent button (إدارة المشتركين)
                            Button(
                                onClick = { navController.navigate("subscribers") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CardBackground.copy(alpha = 0.85f),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, SportBlue.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "إدارة المشتركين",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            // Right filled cyber button (ابدأ الآن)
                            Button(
                                onClick = { navController.navigate("add_subscriber") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SportOrange, // Now Soft Calm Teal (very soothing for eyes)
                                    contentColor = DarkBackground
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 10.dp
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(start = 6.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                            ) {
                                Text(
                                    text = "بث جديد",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Statistics Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "إحصائيات الشبكة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, end = 4.dp),
                        textAlign = TextAlign.Right
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            value = "+10,000",
                            label = "مشترك نشط",
                            icon = Icons.Default.People,
                            iconColor = SportBlue,
                            modifier = Modifier.weight(1f)
                        )
                         StatCard(
                            value = "+1400",
                            label = "قناة مشفرة",
                            icon = Icons.Default.SettingsInputAntenna,
                            iconColor = SportAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = "100%",
                            label = "ثبات الإشارة",
                            icon = Icons.Default.Speed,
                            iconColor = SportOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Why Us (Features) Title
            item {
                Text(
                    text = "ميزات منظومة شبكة X SPORT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                    textAlign = TextAlign.Right
                )
            }

            // 4. Features Grid
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FeatureCard(
                        title = "البث المباشر المتميز",
                        description = "بث فضائي رقمي عالي الدقة لأهم المحطات العالمية والشبكات المشفرة دون تأخير أو انقطاع ملائم لجميع الأجهزة.",
                        icon = Icons.Default.Tv,
                        iconColor = SportOrange
                    )
                    FeatureCard(
                        title = "الاشتراكات المرنة",
                        description = "باقات متنوعة للمشتركين (٣ أشهر، ٦ أشهر، سنوية) مع خيارات التفعيل الفوري لتلبية متطلبات وكلاء شبكة X SPORT.",
                        icon = Icons.Default.Autorenew,
                        iconColor = SportBlue,
                        onClick = { showSubscriptionDialog = true }
                    )
                    FeatureCard(
                        title = "تغطية شاملة وثابتة",
                        description = "نضمن تغطية متميزة لبث واستقبال المحتوى الرياضي والترفيهي الفائق الجودة عبر أحدث السيرفرات وشبكات البث.",
                        icon = Icons.Default.Star,
                        iconColor = SportAmber
                    )
                    FeatureCard(
                        title = "فريق دعم فني 24/7",
                        description = "فريق متمرس لشبكة X SPORT مستعد دوماً لتقديم المساندة التقنية وضمان جودة واستمرارية البث الرياضي للوكلاء.",
                        icon = Icons.Default.SupportAgent,
                        iconColor = Color.Green
                    )
                }
            }

            // 5. Footer
            item {
                FooterComponent()
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(
            1.dp, 
            Brush.verticalGradient(
                listOf(iconColor.copy(alpha = 0.3f), Color.Transparent)
            )
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Surface(
                color = iconColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, if (onClick != null) SportGold.copy(alpha = 0.45f) else BorderColor),
        modifier = cardModifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MutedText,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Surface(
                color = iconColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, iconColor.copy(alpha = 0.25f)),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
