package com.sgc.teknisyen.presentation.navigation

import androidx.navigation.NavHost
import androidx.navigation.composable
import androidx.navigation.navArgument
import com.sgc.teknisyen.presentation.screens.login.LoginScreen
import com.sgc.teknisyen.presentation.screens.login.LoginViewModel
import com.sgc.teknisyen.presentation.screens.home.HomeScreen
import com.sgc.teknisyen.presentation.screens.home.HomeViewModel
import com.sgc.teknisyen.presentation.screens.jobdetail.JobDetailScreen
import com.sgc.teknisyen.presentation.screens.jobdetail.JobDetailViewModel
import com.sgc.teknisyen.presentation.screens.profile.ProfileScreen
import com.sgc.teknisyen.presentation.screens.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.hiltViewModel

@Composable
fun SgcNavHost(loginViewModel: LoginViewModel) {
    val navController = rememberNavController()
    val authState = loginViewModel.authState.collectAsStateWithLifecycle()

    NavHost(navController, startDestination = if (authState.value is AuthResult.Authenticated) Screens.Home else Screens.Login) {
        composable(Screens.Login) {
            LoginScreen(onLoginSuccess = { navController.navigate(Screens.Home) { popUpTo(Screens.Login) { inclusive = true } } })
        }
        composable(Screens.Home) {
            HomeScreen(onJobClick = { jobNo -> navController.navigate("${Screens.JobDetail}/$jobNo") }, onProfileClick = { navController.navigate(Screens.Profile) })
        }
        composable(
            route = "${Screens.JobDetail}/{jobNo}",
            arguments = listOf(navArgument("jobNo") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobNo = backStackEntry.getString()!!
            JobDetailScreen(jobNo = jobNo, onBack = { navController.popBackStack() })
        }
        composable(Screens.Profile) {
            ProfileScreen(onLogout = { navController.navigate(Screens.Login) { popUpTo(Screens.Login) { inclusive = true } } })
        }
    }
}