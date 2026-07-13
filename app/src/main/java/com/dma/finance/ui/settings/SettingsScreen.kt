package com.dma.finance.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dma.finance.R

@Composable
fun SettingsScreen(
    onOpenAccounts: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenMembers: () -> Unit,
    onSwitchProject: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val project by viewModel.project.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_account_section), style = MaterialTheme.typography.titleMedium)
                    Text(user?.fullName.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                    Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_current_project), style = MaterialTheme.typography.titleMedium)
                    Text(project?.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.accounts_title)) },
                leadingContent = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickableCard(onOpenAccounts)
            )
        }
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.categories_title)) },
                leadingContent = { Icon(Icons.Default.Category, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickableCard(onOpenCategories)
            )
        }
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.projects_members)) },
                leadingContent = { Icon(Icons.Default.Group, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickableCard(onOpenMembers)
            )
        }
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_switch_project)) },
                leadingContent = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickableCard(onSwitchProject)
            )
        }
        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_logout)) },
                leadingContent = { Icon(Icons.Default.Logout, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickableCard {
                    viewModel.signOut()
                    onSignedOut()
                }
            )
        }
    }
}

private fun Modifier.clickableCard(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
