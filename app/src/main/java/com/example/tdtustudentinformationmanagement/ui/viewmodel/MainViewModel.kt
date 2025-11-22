package com.example.tdtustudentinformationmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdtustudentinformationmanagement.data.model.StudentStatus
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.example.tdtustudentinformationmanagement.data.model.UserStatus
import com.example.tdtustudentinformationmanagement.data.repository.AuthRepository
import com.example.tdtustudentinformationmanagement.data.repository.StorageRepository
import com.example.tdtustudentinformationmanagement.data.repository.StudentRepository
import com.example.tdtustudentinformationmanagement.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val totalUsers: Int = 0,
    val lockedUsers: Int = 0,
    val totalStudents: Int = 0,
    val graduatedStudents: Int = 0,
    val errorMessage: String? = null
)

data class ProfilePictureState(
    val isUploading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()
    
    private val _dashboardState = MutableStateFlow(DashboardState(isLoading = true))
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    private val _profilePictureState = MutableStateFlow(ProfilePictureState())
    val profilePictureState: StateFlow<ProfilePictureState> = _profilePictureState.asStateFlow()
    
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()
    
    private var lastFirebaseUserId: String? = null
    
    init {
        // Observe Firebase Auth state changes to detect user switches
        viewModelScope.launch {
            authRepository.observeAuthState()
                .catch { e ->
                    // Handle errors silently
                    println("Error observing auth state: ${e.message}")
                }
                .collect { firebaseUser ->
                    val currentFirebaseUserId = firebaseUser?.uid
                    
                    // When Firebase Auth user changes, reload current user and role
                    if (firebaseUser != null) {
                        // Check if user actually changed
                        if (currentFirebaseUserId != lastFirebaseUserId) {
                            // Clear old state first
                            _currentUser.value = null
                            _userRole.value = null
                            
                            // Update last user ID
                            lastFirebaseUserId = currentFirebaseUserId
                            
                            // Reload new user data
                            loadCurrentUserInternal()
                            // Refresh dashboard when user changes
                            refreshDashboard()
                        }
                    } else {
                        // User logged out, clear state
                        lastFirebaseUserId = null
                        _currentUser.value = null
                        _userRole.value = null
                        _dashboardState.value = DashboardState()
                    }
                }
        }
        
        // Initial load
        lastFirebaseUserId = authRepository.getCurrentUser()?.uid
        loadCurrentUser()
        refreshDashboard()
    }
    
    fun loadCurrentUser() {
        viewModelScope.launch {
            loadCurrentUserInternal()
        }
    }
    
    private suspend fun loadCurrentUserInternal() {
        val userResult = authRepository.getCurrentUserData()
        if (userResult.isSuccess) {
            _currentUser.value = userResult.getOrNull()
        }
        
        val roleResult = authRepository.checkUserRole()
        if (roleResult.isSuccess) {
            _userRole.value = roleResult.getOrNull()
        }
    }
    
    fun refreshDashboard() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val usersResult = userRepository.getAllUsers()
                val studentsResult = studentRepository.getAllStudents()
                if (usersResult.isSuccess && studentsResult.isSuccess) {
                    val users = usersResult.getOrNull().orEmpty()
                    val students = studentsResult.getOrNull().orEmpty()
                    _dashboardState.update {
                        it.copy(
                            isLoading = false,
                            totalUsers = users.size,
                            lockedUsers = users.count { user -> user.status == UserStatus.LOCKED },
                            totalStudents = students.size,
                            graduatedStudents = students.count { student -> student.status == StudentStatus.GRADUATED },
                            errorMessage = null
                        )
                    }
                } else {
                    val error = usersResult.exceptionOrNull()?.message
                        ?: studentsResult.exceptionOrNull()?.message
                        ?: "Không thể tải thống kê"
                    _dashboardState.update { it.copy(isLoading = false, errorMessage = error) }
                }
            } catch (e: Exception) {
                _dashboardState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
    
    fun updateProfilePicture(
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            _profilePictureState.value = ProfilePictureState(isUploading = true)
            val uploadResult = storageRepository.uploadProfilePicture(userId, imageBytes, mimeType)
            if (uploadResult.isSuccess) {
                val imageUrl = uploadResult.getOrNull().orEmpty()
                val updateResult = userRepository.updateProfilePicture(userId, imageUrl)
                if (updateResult.isSuccess) {
                    val updatedUser = _currentUser.value?.copy(profilePictureUrl = imageUrl)
                    _currentUser.value = updatedUser
                    _profilePictureState.value = ProfilePictureState(
                        isUploading = false,
                        successMessage = "Cập nhật ảnh thành công"
                    )
                } else {
                    _profilePictureState.value = ProfilePictureState(
                        isUploading = false,
                        errorMessage = updateResult.exceptionOrNull()?.message ?: "Không thể cập nhật ảnh"
                    )
                }
            } else {
                _profilePictureState.value = ProfilePictureState(
                    isUploading = false,
                    errorMessage = uploadResult.exceptionOrNull()?.message ?: "Tải ảnh thất bại"
                )
            }
        }
    }
    
    suspend fun logout() {
        _isLoggingOut.value = true
        try {
            authRepository.signOut()
            _currentUser.value = null
            _userRole.value = null
            _dashboardState.value = DashboardState()
        } finally {
            _isLoggingOut.value = false
        }
    }
}
