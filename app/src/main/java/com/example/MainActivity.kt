package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import com.example.data.AppDatabase
import com.example.data.SubscriberRepository
import com.example.ui.AddEditSubscriberScreen
import com.example.ui.HomeScreen
import com.example.ui.SubscribersListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SubscriberViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, DAO and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SubscriberRepository(database.subscriberDao())
        
        // Retrieve Subscriber View Model using custom factory
        val viewModel = ViewModelProvider(
            this, 
            SubscriberViewModel.Factory(application, repository)
        )[SubscriberViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            val themeModeState = viewModel.themeMode.collectAsState()
            val themeMode = themeModeState.value
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = isDark) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: SubscriberViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        composable("subscribers") {
            SubscribersListScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        
        composable(
            route = "add_subscriber?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val idStr = backStackEntry.arguments?.getString("id")
            val id = idStr?.toIntOrNull()
            AddEditSubscriberScreen(
                viewModel = viewModel,
                navController = navController,
                subscriberId = id
            )
        }
    }
}

