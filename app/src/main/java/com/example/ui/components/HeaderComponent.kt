package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SportGold
import com.example.ui.theme.SportOrange
import com.example.ui.theme.TraditionalTeal
import com.example.ui.theme.CardBackground
import com.example.ui.theme.LightText
import com.example.ui.theme.LocalIsDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.ui.viewmodel.SubscriberViewModel

/**
 * Custom satellite dish and broadcasting cosmic radio waves.
 */
@Composable
fun PulseSignalIcon(
    modifier: Modifier = Modifier,
    color: Color = SportGold
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val cx = w * 0.35f
        val cy = h * 0.65f
        
        // Dish support structure
        val supportPath = Path().apply {
            moveTo(cx, cy + 4f)
            lineTo(cx - 7f, h - 2f)
            lineTo(cx + 7f, h - 2f)
            close()
        }
        drawPath(path = supportPath, color = color.copy(alpha = 0.55f))

        // Dish receiver bowl
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - 14f, cy - 14f),
            size = Size(28f, 28f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Transmitter feed horn rod
        drawLine(
            color = color,
            start = Offset(cx, cy),
            end = Offset(cx + 12f, cy - 12f),
            strokeWidth = 2.5.dp.toPx()
        )

        // Feed horn tip
        drawCircle(
            color = color,
            radius = 3f,
            center = Offset(cx + 12f, cy - 12f)
        )

        // Emitted electromagnetic waves
        drawArc(
            color = color.copy(alpha = 0.85f),
            startAngle = -60f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx + 4f, cy - 24f),
            size = Size(20f, 20f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawArc(
            color = color.copy(alpha = 0.55f),
            startAngle = -60f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx + 12f, cy - 32f),
            size = Size(36f, 36f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawArc(
            color = color.copy(alpha = 0.25f),
            startAngle = -60f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx + 20f, cy - 40f),
            size = Size(52f, 52f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun HeaderComponent(
    currentRoute: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 8.dp,
        color = DarkBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
        ) {
            // Main App Header Row (Exactly matching the screenshot format)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Actions row on the Left (Exit / Navigate and Theme Toggle Button)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            // Navigate back or show entry welcome dialog
                            if (currentRoute != "home") {
                                navController.navigate("home")
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Exit to Control Panel",
                            tint = SportGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Elegant theme toggle box with dropdown selection menu
                    val context = LocalContext.current
                    val activity = remember(context) {
                        var ctx = context
                        while (ctx is android.content.ContextWrapper) {
                            if (ctx is androidx.activity.ComponentActivity) break
                            ctx = ctx.baseContext
                        }
                        ctx as? androidx.activity.ComponentActivity
                    }
                    val viewModel = activity?.let {
                        ViewModelProvider(it)[SubscriberViewModel::class.java]
                    }

                    if (viewModel != null) {
                        val themeModeState = viewModel.themeMode.collectAsState()
                        val currentMode = themeModeState.value
                        var menuExpanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                // Select emoji or symbol based on theme state dynamically
                                val themeName = when (currentMode) {
                                    1 -> "☀️"
                                    2 -> "🌙"
                                    else -> "💻"
                                }
                                Text(
                                    text = themeName,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(CardBackground)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("تلقائي (حسب النظام) 💻", color = LightText, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.updateThemeMode(0)
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("الوضع المضيء ☀️", color = LightText, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.updateThemeMode(1)
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("الوضع الداكن 🌙", color = LightText, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.updateThemeMode(2)
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Header Title & Signal line on the Right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .clickable {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        }
                ) {
                    Text(
                        text = "وكلاء شبكة X SPORT",
                        color = SportGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    PulseSignalIcon(
                        modifier = Modifier.size(26.dp),
                        color = SportGold
                    )
                }
            }

            // Elegant Sub-Navigation row below
            val isDark = LocalIsDarkTheme.current
            val subNavBg = if (isDark) Color(0xFF030A09).copy(alpha = 0.85f) else Color(0xFFE2EFEB)
            val selectedIndicatorColor = if (isDark) SportGold else TraditionalTeal
            val unselectedTextColor = if (isDark) Color.White.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.55f)

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(subNavBg)
                    .padding(vertical = 10.dp)
            ) {
                val items = listOf(
                    Triple("home", "الرئيسية", "home"),
                    Triple("subscribers", "المشتركون", "subscribers"),
                    Triple("add_subscriber", "إضافة مشترك", "add_subscriber")
                )

                items.forEach { (route, label, tag) ->
                    val selected = currentRoute == route || (route == "subscribers" && currentRoute.startsWith("subscribers"))
                    val textColor = if (selected) {
                        selectedIndicatorColor
                    } else {
                        unselectedTextColor
                    }
                    val weight = if (selected) FontWeight.Bold else FontWeight.Medium

                    Text(
                        text = label,
                        color = textColor,
                        fontWeight = weight,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
