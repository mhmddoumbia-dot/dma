package com.dma.finance.ui.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.ProjectMemberEntity
import com.dma.finance.data.local.entity.ProjectRole
import com.dma.finance.data.local.relation.ProjectMemberWithUser
import com.dma.finance.data.repository.AuthRepository
import com.dma.finance.data.repository.ProjectMemberResult
import com.dma.finance.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectMembersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val members: StateFlow<List<ProjectMemberWithUser>> = projectRepository.observeMembers(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myRole: StateFlow<ProjectRole?> = authRepository.currentUser
        .map { it?.id }
        .flatMapLatest { userId ->
            if (userId == null) flowOf(null) else projectRepository.observeMyRole(projectId, userId)
        }
        .map { it?.role }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _lastResult = MutableStateFlow<ProjectMemberResult?>(null)
    val lastResult: StateFlow<ProjectMemberResult?> = _lastResult

    fun addMember(email: String, role: ProjectRole) {
        if (email.isBlank()) return
        viewModelScope.launch {
            _lastResult.value = projectRepository.addMemberByEmail(projectId, email, role)
        }
    }

    fun updateRole(member: ProjectMemberEntity, newRole: ProjectRole) {
        viewModelScope.launch {
            _lastResult.value = projectRepository.updateMemberRole(member, newRole)
        }
    }

    fun removeMember(member: ProjectMemberEntity) {
        viewModelScope.launch {
            _lastResult.value = projectRepository.removeMember(member)
        }
    }

    fun consumeResult() {
        _lastResult.value = null
    }
}
