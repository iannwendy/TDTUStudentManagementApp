package com.example.tdtustudentinformationmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdtustudentinformationmanagement.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(
                isLoading = true, 
                isError = false, 
                isSuccess = false,
                errorMessage = null
            )
            
            val result = authRepository.signIn(email, password)
            
            if (result.isSuccess) {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    isError = false
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Login failed"
                println("❌ [LoginViewModel] Login error: $errorMsg")
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    isError = true,
                    isSuccess = false, // Ensure success is false when there's an error
                    errorMessage = errorMsg
                )
            }
        }
    }
    
    fun resetState() {
        _loginState.value = LoginState()
    }
    
    fun testConnection(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.testFirebaseConnection()
            callback(result.isSuccess)
        }
    }
}
