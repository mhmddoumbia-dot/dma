package com.dma.finance.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dma.finance.R
import com.dma.finance.data.local.entity.ProjectRole
import com.dma.finance.data.local.relation.ProjectMemberWithUser

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProjectMembersScreen(
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    viewModel: ProjectMembersViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val myRole by viewModel.myRole.collectAsState()
    val canManage = myRole?.canManageMembers() == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.projects_members)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManage) {
                FloatingActionButton(onClick = onAddMember) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.projects_invite_member))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(members, key = { it.member.id }) { memberWithUser ->
                MemberRow(
                    memberWithUser = memberWithUser,
                    canManage = canManage,
                    onRoleChange = { newRole -> viewModel.updateRole(memberWithUser.member, newRole) },
                    onRemove = { viewModel.removeMember(memberWithUser.member) }
                )
            }
        }
    }
}

@Composable
private fun MemberRow(
    memberWithUser: ProjectMemberWithUser,
    canManage: Boolean,
    onRoleChange: (ProjectRole) -> Unit,
    onRemove: () -> Unit
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.size(8.dp))
                androidx.compose.foundation.layout.Column {
                    Text(text = memberWithUser.user.fullName, style = MaterialTheme.typography.bodyLarge)
                    Text(text = memberWithUser.user.email, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    TextButton(
                        onClick = { if (canManage) roleMenuExpanded = true }
                    ) {
                        Text(roleLabel(memberWithUser.member.role))
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        ProjectRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(roleLabel(role)) },
                                onClick = {
                                    onRoleChange(role)
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                if (canManage) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
        }
    }
}

@Composable
internal fun roleLabel(role: ProjectRole): String = when (role) {
    ProjectRole.OWNER -> stringResource(R.string.projects_role_owner)
    ProjectRole.ADMIN -> stringResource(R.string.projects_role_admin)
    ProjectRole.MEMBER -> stringResource(R.string.projects_role_member)
    ProjectRole.VIEWER -> stringResource(R.string.projects_role_viewer)
}
