package com.dma.finance.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.io.ExcelExporter
import com.dma.finance.data.io.ExcelImporter
import com.dma.finance.data.io.ImportSummary
import com.dma.finance.data.local.entity.ProjectEntity
import com.dma.finance.data.local.entity.UserEntity
import com.dma.finance.data.repository.AuthRepository
import com.dma.finance.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** État de la dernière opération d'import/export Excel déclenchée depuis les paramètres. */
sealed class DataTransferState {
    object Idle : DataTransferState()
    object InProgress : DataTransferState()
    object ExportSuccess : DataTransferState()
    data class ImportSuccess(val summary: ImportSummary) : DataTransferState()
    data class Failed(val message: String) : DataTransferState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    projectRepository: ProjectRepository,
    private val excelExporter: ExcelExporter,
    private val excelImporter: ExcelImporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val projectId: Long = checkNotNull(savedStateHandle["projectId"])

    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val project: StateFlow<ProjectEntity?> = projectRepository.observeProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _dataTransferState = MutableStateFlow<DataTransferState>(DataTransferState.Idle)
    val dataTransferState: StateFlow<DataTransferState> = _dataTransferState.asStateFlow()

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    fun exportToExcel(destination: Uri) {
        _dataTransferState.value = DataTransferState.InProgress
        viewModelScope.launch {
            try {
                context.contentResolver.openOutputStream(destination)?.use { output ->
                    excelExporter.export(projectId, output)
                } ?: throw IllegalStateException("IMPOSSIBLE_OPEN_FILE")
                _dataTransferState.value = DataTransferState.ExportSuccess
            } catch (e: Exception) {
                _dataTransferState.value = DataTransferState.Failed(e.message ?: "EXPORT_ERROR")
            }
        }
    }

    fun importFromExcel(source: Uri) {
        _dataTransferState.value = DataTransferState.InProgress
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUser.first()?.id
                    ?: throw IllegalStateException("NO_USER")
                val summary = context.contentResolver.openInputStream(source)?.use { input ->
                    excelImporter.import(projectId, userId, input)
                } ?: throw IllegalStateException("IMPOSSIBLE_OPEN_FILE")
                _dataTransferState.value = DataTransferState.ImportSuccess(summary)
            } catch (e: Exception) {
                _dataTransferState.value = DataTransferState.Failed(e.message ?: "IMPORT_ERROR")
            }
        }
    }

    fun consumeDataTransferState() {
        _dataTransferState.value = DataTransferState.Idle
    }
}
