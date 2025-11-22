package com.example.tdtustudentinformationmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdtustudentinformationmanagement.data.model.Certificate
import com.example.tdtustudentinformationmanagement.data.model.Student
import com.example.tdtustudentinformationmanagement.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentsUiState(
    val isLoading: Boolean = true,
    val students: List<Student> = emptyList(),
    val selectedStudent: Student? = null,
    val certificates: List<Certificate> = emptyList(),
    val isCertificateLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = studentRepository.getAllStudents()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        students = result.getOrNull().orEmpty()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể tải danh sách sinh viên"
                    )
                }
            }
        }
    }

    fun createStudent(student: Student) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = studentRepository.createStudent(student)
            if (result.isSuccess) {
                loadStudents()
                _uiState.update { it.copy(successMessage = "Thêm sinh viên thành công") }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể thêm sinh viên"
                    )
                }
            }
        }
    }

    fun updateStudent(studentId: String, student: Student) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = studentRepository.updateStudent(studentId, student.copy(id = studentId))
            if (result.isSuccess) {
                loadStudents()
                _uiState.update { it.copy(successMessage = "Cập nhật sinh viên thành công") }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể cập nhật sinh viên"
                    )
                }
            }
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = studentRepository.deleteStudent(studentId)
            if (result.isSuccess) {
                loadStudents()
                _uiState.update { it.copy(successMessage = "Đã xóa sinh viên") }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Không thể xóa sinh viên"
                    )
                }
            }
        }
    }

    fun selectStudent(student: Student) {
        _uiState.update { it.copy(selectedStudent = student, isCertificateLoading = true) }
        viewModelScope.launch {
            val result = studentRepository.getCertificatesByStudentId(student.id)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        certificates = result.getOrNull().orEmpty(),
                        isCertificateLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        certificates = emptyList(),
                        isCertificateLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedStudent = null, certificates = emptyList(), isCertificateLoading = false) }
    }

    fun createCertificate(certificate: Certificate) {
        viewModelScope.launch {
            val result = studentRepository.createCertificate(certificate)
            if (result.isSuccess) {
                _uiState.value.selectedStudent?.let { selectStudent(it) }
                _uiState.update { it.copy(successMessage = "Thêm chứng chỉ thành công") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun updateCertificate(certificateId: String, certificate: Certificate) {
        viewModelScope.launch {
            val result = studentRepository.updateCertificate(certificateId, certificate.copy(id = certificateId))
            if (result.isSuccess) {
                _uiState.value.selectedStudent?.let { selectStudent(it) }
                _uiState.update { it.copy(successMessage = "Cập nhật chứng chỉ thành công") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun deleteCertificate(certificateId: String) {
        viewModelScope.launch {
            val result = studentRepository.deleteCertificate(certificateId)
            if (result.isSuccess) {
                _uiState.value.selectedStudent?.let { selectStudent(it) }
                _uiState.update { it.copy(successMessage = "Đã xóa chứng chỉ") }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

