package com.example.tdtustudentinformationmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdtustudentinformationmanagement.data.model.LoginHistory
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.example.tdtustudentinformationmanagement.data.model.UserStatus
import com.example.tdtustudentinformationmanagement.data.repository.AuthRepository
import com.example.tdtustudentinformationmanagement.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsersUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loginHistory: List<LoginHistory> = emptyList(),
    val historyUser: User? = null,
    val isHistoryLoading: Boolean = false
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = userRepository.getAllUsers()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = result.getOrNull().orEmpty(),
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể tải danh sách người dùng"
                    )
                }
            }
        }
    }

    fun addUser(
        adminEmail: String,
        adminPassword: String,
        email: String,
        password: String,
        user: User
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            val result = authRepository.createUserAccount(email, password, user.copy(email = email))
            if (result.isSuccess) {
                val relogin = authRepository.signIn(adminEmail, adminPassword)
                if (relogin.isSuccess) {
                    loadUsers()
                    _uiState.update { it.copy(successMessage = "Thêm người dùng thành công") }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = relogin.exceptionOrNull()?.message ?: "Tạo user thành công nhưng không thể đăng nhập lại admin"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể tạo người dùng mới"
                    )
                }
            }
        }
    }

    fun updateUser(userId: String, user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            val result = userRepository.updateUser(userId, user.copy(id = userId))
            if (result.isSuccess) {
                loadUsers()
                _uiState.update { it.copy(successMessage = "Cập nhật người dùng thành công") }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể cập nhật người dùng"
                    )
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            val result = userRepository.deleteUser(userId)
            if (result.isSuccess) {
                loadUsers()
                _uiState.update { it.copy(successMessage = "Đã xóa người dùng") }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể xóa người dùng"
                    )
                }
            }
        }
    }

    fun updateStatus(userId: String, status: UserStatus) {
        viewModelScope.launch {
            val result = userRepository.updateUserStatus(userId, status)
            if (result.isSuccess) {
                loadUsers()
                _uiState.update { it.copy(successMessage = "Cập nhật trạng thái thành công") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun updateRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            val result = userRepository.updateUserRole(userId, role)
            if (result.isSuccess) {
                loadUsers()
                _uiState.update { it.copy(successMessage = "Cập nhật quyền thành công") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun fetchLoginHistory(user: User) {
        viewModelScope.launch {
            // Kiểm tra quyền truy cập - chỉ Admin mới được xem lịch sử đăng nhập
            val roleResult = authRepository.checkUserRole()
            val currentRole = roleResult.getOrNull()
            
            if (currentRole != UserRole.ADMIN) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Bạn không có quyền xem lịch sử đăng nhập. Chỉ Admin mới có quyền này."
                    )
                }
                return@launch
            }
            
            _uiState.update {
                it.copy(
                    isHistoryLoading = true,
                    historyUser = user,
                    loginHistory = emptyList(),
                    errorMessage = null
                )
            }
            val result = userRepository.getLoginHistory(user.id)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        loginHistory = result.getOrNull().orEmpty()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể tải lịch sử đăng nhập"
                    )
                }
            }
        }
    }

    fun clearHistory() {
        _uiState.update { it.copy(historyUser = null, loginHistory = emptyList(), isHistoryLoading = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

