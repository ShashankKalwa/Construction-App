package com.example.contruction_management_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.contruction_management_app.feature.auth.AuthUiState
import com.example.contruction_management_app.feature.auth.AuthViewModel
import com.example.contruction_management_app.feature.auth.LoginScreen
import com.example.contruction_management_app.feature.gallery.ComponentGalleryScreen
import com.example.contruction_management_app.feature.project.ProjectDetailScreen
import com.example.contruction_management_app.feature.project.ProjectViewModel
import com.example.contruction_management_app.feature.shell.MainShell
import com.example.contruction_management_app.feature.task.TaskViewModel

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = if (authState is AuthUiState.Authenticated) {
        AppRoutes.MAIN_SHELL
    } else {
        AppRoutes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.MAIN_SHELL) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onOpenGallery = {
                    navController.navigate(AppRoutes.GALLERY)
                }
            )
        }

        composable(AppRoutes.MAIN_SHELL) {
            val user = (authState as? AuthUiState.Authenticated)?.user
            if (user != null) {
                MainShell(
                    user = user,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.MAIN_SHELL) { inclusive = true }
                        }
                    },
                    onOpenGallery = {
                        navController.navigate(AppRoutes.GALLERY)
                    },
                    onOpenProjectDetail = { projectId ->
                        navController.navigate(AppRoutes.projectDetail(projectId))
                    }
                )
            } else {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(AppRoutes.MAIN_SHELL) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onOpenGallery = {
                        navController.navigate(AppRoutes.GALLERY)
                    }
                )
            }
        }

        composable(AppRoutes.GALLERY) {
            ComponentGalleryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoutes.PROJECT_DETAIL,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val projectViewModel: ProjectViewModel = viewModel()
            val taskViewModel: TaskViewModel = viewModel()

            ProjectDetailScreen(
                projectId = projectId,
                projectViewModel = projectViewModel,
                taskViewModel = taskViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
