package com.helloeva.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.helloeva.app.HelloEvaApp
import com.helloeva.app.ui.screens.chat.ChatScreen
import com.helloeva.app.ui.screens.create.CreateConversationViewModel
import com.helloeva.app.ui.screens.create.SurveyScreen
import com.helloeva.app.ui.screens.create.TargetGradeScreen
import com.helloeva.app.ui.screens.home.HomeScreen
import com.helloeva.app.ui.screens.login.LoginScreen
import com.helloeva.app.ui.screens.onboarding.OnboardingScreen
import com.helloeva.app.ui.screens.settings.SettingsScreen
import com.helloeva.app.ui.screens.start.StartScreen
import kotlinx.serialization.Serializable

// Type-safe routes
@Serializable data object LoginRoute
@Serializable data object OnboardingRoute
@Serializable data object HomeRoute
@Serializable data object SettingsRoute
@Serializable data object CreateGraph
@Serializable data object TargetGradeRoute
@Serializable data object SurveyRoute
@Serializable data class StartRoute(val conversationId: Long)
@Serializable data class ChatRoute(val conversationId: Long)

/**
 * login ─(first run)→ onboarding → home
 * home → settings
 * home → [create: target grade → survey 1..8] → start → chat
 * home → chat (resume / read-only)
 */
@Composable
fun HelloEvaNavHost(navController: NavHostController = rememberNavController()) {
    val container = (LocalContext.current.applicationContext as HelloEvaApp).container

    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(onLoginSuccess = {
                if (container.onboardingDone) {
                    navController.navigate(HomeRoute) { popUpTo(LoginRoute) { inclusive = true } }
                } else {
                    navController.navigate(OnboardingRoute) { popUpTo(LoginRoute) { inclusive = true } }
                }
            })
        }
        composable<OnboardingRoute> {
            OnboardingScreen(onFinish = {
                container.onboardingDone = true
                navController.navigate(HomeRoute) { popUpTo(OnboardingRoute) { inclusive = true } }
            })
        }
        composable<HomeRoute> {
            HomeScreen(
                onNewConversation = { navController.navigate(CreateGraph) },
                onOpenConversation = { navController.navigate(ChatRoute(it.id)) },
                onSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    // TODO: clear the saved token
                    navController.navigate(LoginRoute) { popUpTo(navController.graph.id) { inclusive = true } }
                },
            )
        }
        navigation<CreateGraph>(startDestination = TargetGradeRoute) {
            composable<TargetGradeRoute> { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(CreateGraph) }
                val vm: CreateConversationViewModel = viewModel(parent, factory = CreateConversationViewModel.Factory)
                TargetGradeScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(SurveyRoute) },
                )
            }
            composable<SurveyRoute> { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(CreateGraph) }
                val vm: CreateConversationViewModel = viewModel(parent, factory = CreateConversationViewModel.Factory)
                SurveyScreen(
                    viewModel = vm,
                    onExit = { navController.popBackStack() },
                    onCreated = { id ->
                        navController.navigate(StartRoute(id)) { popUpTo(CreateGraph) { inclusive = true } }
                    },
                )
            }
        }
        composable<StartRoute> { entry ->
            val id = entry.toRoute<StartRoute>().conversationId
            StartScreen(
                onBack = { navController.popBackStack() },
                onStart = {
                    navController.navigate(ChatRoute(id)) { popUpTo<StartRoute> { inclusive = true } }
                },
            )
        }
        composable<ChatRoute> {
            ChatScreen(onBack = { navController.popBackStack() })
        }
    }
}
