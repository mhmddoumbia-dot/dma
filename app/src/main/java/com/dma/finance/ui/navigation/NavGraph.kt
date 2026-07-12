package com.dma.finance.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dma.finance.ui.accounts.AccountEditScreen
import com.dma.finance.ui.accounts.AccountListScreen
import com.dma.finance.ui.auth.AuthViewModel
import com.dma.finance.ui.auth.LoginScreen
import com.dma.finance.ui.auth.RegisterScreen
import com.dma.finance.ui.budgets.BudgetEditScreen
import com.dma.finance.ui.categories.CategoryEditScreen
import com.dma.finance.ui.categories.CategoryListScreen
import com.dma.finance.ui.projects.CreateProjectScreen
import com.dma.finance.ui.projects.ProjectListScreen
import com.dma.finance.ui.projects.ProjectMembersScreen
import com.dma.finance.ui.transactions.TransactionEditScreen

private val LONG_ARG = navArgument("projectId") { type = NavType.LongType }

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)

    LaunchedEffect(currentUser) {
        val destination = navController.currentDestination?.route
        if (currentUser != null && (destination == Routes.LOGIN || destination == Routes.REGISTER)) {
            navController.navigate(Routes.PROJECT_LIST) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.PROJECT_LIST) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.PROJECT_LIST) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.PROJECT_LIST) {
            ProjectListScreen(
                onProjectSelected = { projectId ->
                    navController.navigate(Routes.home(projectId)) {
                        popUpTo(Routes.PROJECT_LIST) { inclusive = true }
                    }
                },
                onCreateProject = { navController.navigate(Routes.PROJECT_CREATE) }
            )
        }

        composable(Routes.PROJECT_CREATE) {
            CreateProjectScreen(
                onBack = { navController.popBackStack() },
                onProjectCreated = { projectId ->
                    navController.navigate(Routes.home(projectId)) {
                        popUpTo(Routes.PROJECT_LIST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME, arguments = listOf(LONG_ARG)) { entry ->
            val projectId = requireNotNull(entry.arguments).getLong("projectId")
            HomeScreen(
                entry = entry,
                projectId = projectId,
                onAddTransaction = { navController.navigate(Routes.transactionEdit(projectId)) },
                onEditTransaction = { id -> navController.navigate(Routes.transactionEdit(projectId, id)) },
                onAddBudget = { navController.navigate(Routes.budgetEdit(projectId)) },
                onEditBudget = { id -> navController.navigate(Routes.budgetEdit(projectId, id)) },
                onOpenAccounts = { navController.navigate(Routes.accountList(projectId)) },
                onOpenCategories = { navController.navigate(Routes.categoryList(projectId)) },
                onOpenMembers = { navController.navigate(Routes.projectMembers(projectId)) },
                onSwitchProject = {
                    navController.navigate(Routes.PROJECT_LIST) {
                        popUpTo(Routes.PROJECT_LIST) { inclusive = true }
                    }
                },
                onSignedOut = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Routes.PROJECT_MEMBERS, arguments = listOf(LONG_ARG)) {
            ProjectMembersScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ACCOUNT_LIST, arguments = listOf(LONG_ARG)) { entry ->
            val projectId = requireNotNull(entry.arguments).getLong("projectId")
            AccountListScreen(
                onBack = { navController.popBackStack() },
                onAddAccount = { navController.navigate(Routes.accountEdit(projectId)) },
                onEditAccount = { id -> navController.navigate(Routes.accountEdit(projectId, id)) }
            )
        }

        composable(
            Routes.ACCOUNT_EDIT,
            arguments = listOf(LONG_ARG, navArgument("accountId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            AccountEditScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CATEGORY_LIST, arguments = listOf(LONG_ARG)) { entry ->
            val projectId = requireNotNull(entry.arguments).getLong("projectId")
            CategoryListScreen(
                onBack = { navController.popBackStack() },
                onAddCategory = { navController.navigate(Routes.categoryEdit(projectId)) },
                onEditCategory = { id -> navController.navigate(Routes.categoryEdit(projectId, id)) }
            )
        }

        composable(
            Routes.CATEGORY_EDIT,
            arguments = listOf(LONG_ARG, navArgument("categoryId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            CategoryEditScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Routes.TRANSACTION_EDIT,
            arguments = listOf(LONG_ARG, navArgument("transactionId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            TransactionEditScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Routes.BUDGET_EDIT,
            arguments = listOf(LONG_ARG, navArgument("budgetId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            BudgetEditScreen(onBack = { navController.popBackStack() })
        }
    }
}
