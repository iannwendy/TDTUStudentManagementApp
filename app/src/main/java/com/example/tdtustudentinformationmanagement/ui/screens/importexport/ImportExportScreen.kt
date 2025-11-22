package com.example.tdtustudentinformationmanagement.ui.screens.importexport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.example.tdtustudentinformationmanagement.data.model.Student
import com.example.tdtustudentinformationmanagement.ui.viewmodel.ImportExportState

@Composable
fun ImportExportScreen(
    uiState: ImportExportState,
    students: List<Student>,
    onImportStudents: (Uri) -> Unit,
    onExportStudents: (Uri) -> Unit,
    onImportCertificates: (String, Uri) -> Unit,
    onExportCertificates: (String, Uri) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id.orEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val importStudentsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(onImportStudents)
    }

    val exportStudentsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let(onExportStudents)
    }

    val importCertificatesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && selectedStudentId.isNotBlank()) {
            onImportCertificates(selectedStudentId, uri)
        } else {
            errorMessage = "Vui lòng chọn sinh viên trước khi nhập chứng chỉ"
        }
    }

    val exportCertificatesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && selectedStudentId.isNotBlank()) {
            onExportCertificates(selectedStudentId, uri)
        } else {
            errorMessage = "Vui lòng chọn sinh viên trước khi xuất chứng chỉ"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Nhập/Xuất dữ liệu", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Sao lưu và khôi phục dữ liệu sinh viên, chứng chỉ dưới định dạng CSV",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.isProcessing) {
            Text(
                text = uiState.message ?: "Đang xử lý...",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dữ liệu sinh viên", fontWeight = FontWeight.Bold)
                Text("Nhập hoặc xuất toàn bộ danh sách sinh viên định dạng CSV")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            importStudentsLauncher.launch(arrayOf("text/*", "application/vnd.ms-excel"))
                        }
                    ) {
                        Text("Nhập CSV")
                    }
                    OutlinedButton(
                        onClick = {
                            exportStudentsLauncher.launch("students_export.csv")
                        }
                    ) {
                        Text("Xuất CSV")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dữ liệu chứng chỉ", fontWeight = FontWeight.Bold)
                Text("Chọn sinh viên và thực hiện nhập/xuất chứng chỉ")
                Spacer(modifier = Modifier.height(12.dp))

                StudentPicker(
                    students = students,
                    selectedId = selectedStudentId,
                    onSelected = { selectedStudentId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {
                        importCertificatesLauncher.launch(arrayOf("text/*"))
                    }) { Text("Nhập chứng chỉ") }
                    OutlinedButton(onClick = {
                        exportCertificatesLauncher.launch("certificates_${selectedStudentId}.csv")
                    }) { Text("Xuất chứng chỉ") }
                }
            }
        }
    }
}

@Composable
private fun StudentPicker(
    students: List<Student>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    val sortedAndFilteredStudents = remember(students, searchQuery) {
        students
            .filter { student ->
                searchQuery.isBlank() || 
                student.name.contains(searchQuery, ignoreCase = true) ||
                student.studentId.contains(searchQuery, ignoreCase = true) ||
                student.email.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                compareBy { it.studentId.lowercase(Locale.getDefault()) }
            )
    }
    
    val selectedStudent = students.firstOrNull { it.id == selectedId }
    
    Column {
        OutlinedButton(onClick = { showDialog = true }) {
            Text(selectedStudent?.let { "${it.studentId} - ${it.name}" } ?: "Chọn sinh viên")
        }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showDialog = false
                    searchQuery = ""
                },
                title = { Text("Chọn sinh viên") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Tìm kiếm (MSSV, tên, email)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (sortedAndFilteredStudents.isEmpty()) {
                                item {
                                    Text(
                                        text = "Không tìm thấy sinh viên nào",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                items(sortedAndFilteredStudents) { student ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (student.id == selectedId) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        onClick = {
                                            onSelected(student.id)
                                            showDialog = false
                                            searchQuery = ""
                                        }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Text(
                                                text = "${student.studentId} - ${student.name}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (student.id == selectedId) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = student.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        showDialog = false
                        searchQuery = ""
                    }) {
                        Text("Đóng")
                    }
                }
            )
        }
    }
}

