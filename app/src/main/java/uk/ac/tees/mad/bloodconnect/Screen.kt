package uk.ac.tees.mad.bloodconnect

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import uk.ac.tees.mad.bloodconnect.ui.screens.AuthScreen
import uk.ac.tees.mad.bloodconnect.ui.screens.ProfileScreen
import uk.ac.tees.mad.bloodconnect.ui.screens.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome_screen")
    object Auth : Screen("auth_screen")
    object Profile : Screen("profile_screen")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val isLoggedIn = Firebase.auth.currentUser != null
    NavHost(navController = navController, startDestination = if (isLoggedIn) Screen.Profile.route else Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        composable(Screen.Auth.route) {
            AuthScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}