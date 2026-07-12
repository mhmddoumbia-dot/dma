package com.dma.finance.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dma.finance.data.repository.AuthRepository
import com.dma.finance.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser = authRepository.currentUser

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "REQUIRED_FIELDS")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(success = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "REQUIRED_FIELDS")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(errorMessage = "PASSWORDS_MISMATCH")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = authRepository.register(fullName, email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(success = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
