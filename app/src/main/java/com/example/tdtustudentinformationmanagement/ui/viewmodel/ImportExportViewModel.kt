package com.example.tdtustudentinformationmanagement.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdtustudentinformationmanagement.data.model.Certificate
import com.example.tdtustudentinformationmanagement.data.model.Student
import com.example.tdtustudentinformationmanagement.data.repository.StudentRepository
import com.example.tdtustudentinformationmanagement.utils.CsvUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportExportState(
    val isProcessing: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportState())
    val uiState: StateFlow<ImportExportState> = _uiState.asStateFlow()

    private fun setProcessing(message: String? = null) {
        _uiState.update { it.copy(isProcessing = true, message = message, errorMessage = null) }
    }

    private fun setSuccess(message: String) {
        _uiState.update { it.copy(isProcessing = false, message = message, errorMessage = null) }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(isProcessing = false, errorMessage = message, message = null) }
    }

    fun importStudents(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setProcessing("Đang nhập danh sách sinh viên...")
                val students = context.contentResolver.openInputStream(uri)?.use { input ->
                    CsvUtils.parseStudentsCsv(input.bufferedReader().readText())
                } ?: emptyList()
                students.forEach { student ->
                    studentRepository.createStudent(student.copy(id = ""))
                }
                setSuccess("Nhập danh sách sinh viên thành công (${students.size})")
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi khi nhập danh sách sinh viên")
            }
        }
    }

    fun exportStudents(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setProcessing("Đang xuất danh sách sinh viên...")
                val studentsResult = studentRepository.getAllStudents()
                if (studentsResult.isSuccess) {
                    val csv = CsvUtils.studentsToCsv(studentsResult.getOrNull().orEmpty())
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(csv.toByteArray())
                    }
                    setSuccess("Xuất danh sách sinh viên thành công")
                } else {
                    throw Exception(studentsResult.exceptionOrNull()?.message ?: "Không thể tải danh sách")
                }
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi khi xuất danh sách sinh viên")
            }
        }
    }

    fun importCertificates(context: Context, studentId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setProcessing("Đang nhập danh sách chứng chỉ...")
                val certificates = context.contentResolver.openInputStream(uri)?.use { input ->
                    CsvUtils.parseCertificatesCsv(studentId, input.bufferedReader().readText())
                } ?: emptyList()
                certificates.forEach { certificate ->
                    studentRepository.createCertificate(certificate.copy(id = ""))
                }
                setSuccess("Nhập chứng chỉ thành công (${certificates.size})")
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi khi nhập chứng chỉ")
            }
        }
    }

    fun exportCertificates(context: Context, studentId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setProcessing("Đang xuất chứng chỉ...")
                val result = studentRepository.getCertificatesByStudentId(studentId)
                if (result.isSuccess) {
                    val csv = CsvUtils.certificatesToCsv(result.getOrNull().orEmpty())
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(csv.toByteArray())
                    }
                    setSuccess("Xuất chứng chỉ thành công")
                } else {
                    throw Exception(result.exceptionOrNull()?.message ?: "Không thể tải chứng chỉ")
                }
            } catch (e: Exception) {
                setError(e.message ?: "Lỗi khi xuất chứng chỉ")
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, errorMessage = null) }
    }
}

