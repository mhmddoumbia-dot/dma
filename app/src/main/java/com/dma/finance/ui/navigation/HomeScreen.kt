package com.dma.finance.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import com.dma.finance.R
import com.dma.finance.ui.budgets.BudgetListScreen
import com.dma.finance.ui.dashboard.DashboardScreen
import com.dma.finance.ui.reports.ReportsScreen
import com.dma.finance.ui.settings.SettingsScreen
import com.dma.finance.ui.transactions.TransactionListScreen

private enum class HomeTab(val labelRes: Int) {
    DASHBOARD(R.string.nav_dashboard),
    TRANSACTIONS(R.string.nav_transactions),
    BUDGETS(R.string.nav_budgets),
    REPORTS(R.string.nav_reports),
    SETTINGS(R.string.nav_settings)
}

/**
 * Écran principal après sélection d'un projet : navigation par onglets (revenus/dépenses,
 * budgets, rapports, paramètres) sans NavHost imbriqué — chaque onglet réutilise le
 * [entry] du graphe de navigation externe pour que ses ViewModels Hilt retrouvent
 * `projectId` dans leur `SavedStateHandle`.
 */
@Composable
fun HomeScreen(
    entry: NavBackStackEntry,
    projectId: Long,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onAddBudget: () -> Unit,
    onEditBudget: (Long) -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenMembers: () -> Unit,
    onSwitchProject: () -> Unit,
    onSignedOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = HomeTab.values()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tabIcon(tab), contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (tabs[selectedTab]) {
                HomeTab.DASHBOARD -> DashboardScreen(
                    onOpenTransaction = onEditTransaction,
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(entry)
                )
                HomeTab.TRANSACTIONS -> TransactionListScreen(
                    onAddTransaction = onAddTransaction,
                    onEditTransaction = onEditTransaction,
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(entry)
                )
                HomeTab.BUDGETS -> BudgetListScreen(
                    onAddBudget = onAddBudget,
                    onEditBudget = onEditBudget,
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(entry)
                )
                HomeTab.REPORTS -> ReportsScreen(
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(entry)
                )
                HomeTab.SETTINGS -> SettingsScreen(
                    onOpenAccounts = onOpenAccounts,
                    onOpenCategories = onOpenCategories,
                    onOpenMembers = onOpenMembers,
                    onSwitchProject = onSwitchProject,
                    onSignedOut = onSignedOut,
                    viewModel = androidx.hilt.navigation.compose.hiltViewModel(entry)
                )
            }
        }
    }
}

private fun tabIcon(tab: HomeTab) = when (tab) {
    HomeTab.DASHBOARD -> Icons.Default.AccountBalanceWallet
    HomeTab.TRANSACTIONS -> Icons.Default.List
    HomeTab.BUDGETS -> Icons.Default.PieChart
    HomeTab.REPORTS -> Icons.Default.BarChart
    HomeTab.SETTINGS -> Icons.Default.Settings
}
