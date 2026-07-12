package com.dma.finance.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.repository.AuthRepository
import com.dma.finance.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateProjectUiState(
    val isSaving: Boolean = false,
    val createdProjectId: Long? = null
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val projects: StateFlow<List<ProjectEntity>> = currentUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else projectRepository.observeProjectsForUser(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProjectId: StateFlow<Long?> = projectRepository.currentProjectId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _createState = MutableStateFlow(CreateProjectUiState())
    val createState: StateFlow<CreateProjectUiState> = _createState.asStateFlow()

    fun createProject(name: String, description: String, currencyCode: String) {
        val ownerId = currentUserId.value ?: return
        if (name.isBlank()) return
        _createState.value = CreateProjectUiState(isSaving = true)
        viewModelScope.launch {
            val id = projectRepository.createProject(name, description, currencyCode, ownerId)
            _createState.value = CreateProjectUiState(createdProjectId = id)
        }
    }

    fun selectProject(projectId: Long) {
        viewModelScope.launch { projectRepository.selectProject(projectId) }
    }

    fun resetCreateState() {
        _createState.value = CreateProjectUiState()
    }
}
