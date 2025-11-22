package com.example.tdtustudentinformationmanagement.ui.screens.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tdtustudentinformationmanagement.data.model.Certificate
import com.example.tdtustudentinformationmanagement.data.model.Gender
import com.example.tdtustudentinformationmanagement.data.model.Student
import com.example.tdtustudentinformationmanagement.data.model.StudentStatus
import com.example.tdtustudentinformationmanagement.ui.viewmodel.StudentsUiState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StudentSortOption(val label: String) {
    NAME_ASC("Tên (A-Z)"),
    GPA_DESC("GPA giảm dần"),
    YEAR_DESC("Năm học giảm dần")
}

@Composable
fun StudentsScreen(
    uiState: StudentsUiState,
    canManageStudents: Boolean,
    onCreateStudent: (Student) -> Unit,
    onUpdateStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onSelectStudent: (Student) -> Unit,
    onClearSelection: () -> Unit,
    onCreateCertificate: (Certificate) -> Unit,
    onUpdateCertificate: (Certificate) -> Unit,
    onDeleteCertificate: (Certificate) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf<StudentStatus?>(null) }
    var sortOption by rememberSaveable { mutableStateOf(StudentSortOption.NAME_ASC) }
    var showStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var confirmDeleteStudent by remember { mutableStateOf<Student?>(null) }

    val filteredStudents = remember(uiState.students, searchQuery, statusFilter, sortOption) {
        uiState.students.filter { student ->
            val queryMatch = student.name.contains(searchQuery, true) ||
                student.studentId.contains(searchQuery, true) ||
                student.email.contains(searchQuery, true)
            val statusMatch = statusFilter?.let { student.status == it } ?: true
            queryMatch && statusMatch
        }.sortedWith(
            when (sortOption) {
                StudentSortOption.NAME_ASC -> compareBy { it.name.lowercase(Locale.getDefault()) }
                StudentSortOption.GPA_DESC -> compareByDescending { it.gpa }
                StudentSortOption.YEAR_DESC -> compareByDescending { it.yearOfStudy }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text("Quản lý sinh viên", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = if (canManageStudents) "Thêm, cập nhật, sắp xếp và theo dõi chứng chỉ"
                else "Chế độ xem danh sách sinh viên",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Tìm theo tên/MSSV/Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDropdown(selected = statusFilter, onSelected = { statusFilter = it })
            SortDropdown(selected = sortOption, onSelected = { sortOption = it })
            if (canManageStudents) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        editingStudent = null
                        showStudentDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thêm SV")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(20.dp)
                )
                Text(
                    text = "Danh sách sinh viên",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = onRefresh,
                enabled = !uiState.isLoading
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.width(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Làm mới")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStudents) { student ->
                    StudentCard(
                        student = student,
                        canManage = canManageStudents,
                        onDetail = {
                            onSelectStudent(student)
                        },
                        onEdit = {
                            editingStudent = student
                            showStudentDialog = true
                        },
                        onDelete = {
                            confirmDeleteStudent = student
                        }
                    )
                }
            }
        }
    }

    if (showStudentDialog) {
        StudentFormDialog(
            initialStudent = editingStudent,
            canEdit = canManageStudents,
            onDismiss = { showStudentDialog = false },
            onSubmit = { student ->
                if (editingStudent == null) {
                    onCreateStudent(student)
                } else {
                    onUpdateStudent(student)
                }
                showStudentDialog = false
            }
        )
    }

    confirmDeleteStudent?.let { student ->
        AlertDialog(
            onDismissRequest = { confirmDeleteStudent = null },
            title = { Text("Xóa sinh viên") },
            text = { Text("Bạn có chắc muốn xóa ${student.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteStudent(student)
                    confirmDeleteStudent = null
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteStudent = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    uiState.selectedStudent?.let { selected ->
        StudentDetailDialog(
            student = selected,
            certificates = uiState.certificates,
            canManage = canManageStudents,
            isCertificateLoading = uiState.isCertificateLoading,
            onDismiss = onClearSelection,
            onEditStudent = {
                onClearSelection()
                editingStudent = selected
                showStudentDialog = true
            },
            onCreateCertificate = onCreateCertificate,
            onUpdateCertificate = onUpdateCertificate,
            onDeleteCertificate = onDeleteCertificate
        )
    }
}

@Composable
private fun StatusDropdown(
    selected: StudentStatus?,
    onSelected: (StudentStatus?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected?.name ?: "Trạng thái")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Tất cả") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            StudentStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selected: StudentSortOption,
    onSelected: (StudentSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            StudentSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: Student,
    canManage: Boolean,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(student.name, style = MaterialTheme.typography.titleMedium)
            Text("MSSV: ${student.studentId}")
            Text("Chuyên ngành: ${student.major}")
            Text("Năm học: ${student.yearOfStudy}")
            Text("GPA: ${student.gpa}")
            Text("Trạng thái: ${student.status.name}")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDetail) { Text("Xem chi tiết") }
                if (canManage) {
                    Row {
                        TextButton(onClick = onEdit) { Text("Chỉnh sửa") }
                        TextButton(onClick = onDelete) { Text("Xóa") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentFormDialog(
    initialStudent: Student?,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Student) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialStudent?.name.orEmpty()) }
    var studentId by rememberSaveable { mutableStateOf(initialStudent?.studentId.orEmpty()) }
    var email by rememberSaveable { mutableStateOf(initialStudent?.email.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initialStudent?.phoneNumber.orEmpty()) }
    var major by rememberSaveable { mutableStateOf(initialStudent?.major.orEmpty()) }
    var year by rememberSaveable { mutableStateOf(initialStudent?.yearOfStudy?.toString().orEmpty()) }
    var gpa by rememberSaveable { mutableStateOf(initialStudent?.gpa?.toString().orEmpty()) }
    var status by rememberSaveable { mutableStateOf(initialStudent?.status ?: StudentStatus.ACTIVE) }
    var gender by rememberSaveable { mutableStateOf(initialStudent?.gender ?: Gender.MALE) }
    var address by rememberSaveable { mutableStateOf(initialStudent?.address.orEmpty()) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var dob by rememberSaveable {
        mutableStateOf(
            initialStudent?.dateOfBirth?.toDate()?.let { dateFormatter.format(it) }.orEmpty()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        Student(
                            id = initialStudent?.id.orEmpty(),
                            name = name,
                            studentId = studentId,
                            email = email,
                            phoneNumber = phone,
                            major = major,
                            yearOfStudy = year.toIntOrNull() ?: 1,
                            gpa = gpa.toDoubleOrNull() ?: 0.0,
                            status = status,
                            gender = gender,
                            address = address,
                            dateOfBirth = dob.takeIf { it.isNotBlank() }?.let { dateString ->
                                val parsedDate: Date? = dateFormatter.parse(dateString)
                                Timestamp(parsedDate ?: Date())
                            } ?: Timestamp.now()
                        )
                    )
                },
                enabled = name.isNotBlank() && studentId.isNotBlank()
            ) {
                Text(if (initialStudent == null) "Thêm" else "Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text(if (initialStudent == null) "Thêm sinh viên" else "Chỉnh sửa sinh viên") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Họ tên") })
                OutlinedTextField(value = studentId, onValueChange = { studentId = it }, label = { Text("MSSV") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") })
                OutlinedTextField(value = major, onValueChange = { major = it }, label = { Text("Chuyên ngành") })
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Năm học") })
                OutlinedTextField(value = gpa, onValueChange = { gpa = it }, label = { Text("GPA") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Địa chỉ") })
                OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("Ngày sinh (yyyy-MM-dd)") })
                StatusDropdown(selected = status, onSelected = { status = it ?: StudentStatus.ACTIVE })
            }
        }
    )
}

@Composable
private fun StudentDetailDialog(
    student: Student,
    certificates: List<Certificate>,
    canManage: Boolean,
    isCertificateLoading: Boolean,
    onDismiss: () -> Unit,
    onEditStudent: () -> Unit,
    onCreateCertificate: (Certificate) -> Unit,
    onUpdateCertificate: (Certificate) -> Unit,
    onDeleteCertificate: (Certificate) -> Unit
) {
    var showCertificateDialog by remember { mutableStateOf(false) }
    var editingCertificate by remember { mutableStateOf<Certificate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = { Text("Thông tin sinh viên") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(student.name, style = MaterialTheme.typography.titleMedium)
                Text("MSSV: ${student.studentId}")
                Text("Chuyên ngành: ${student.major}")
                Text("Email: ${student.email}")
                Text("Điện thoại: ${student.phoneNumber}")
                Text("Trạng thái: ${student.status.name}")

                if (canManage) {
                    TextButton(onClick = onEditStudent) {
                        Text("Chỉnh sửa hồ sơ")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Danh sách chứng chỉ", fontWeight = FontWeight.Bold)

                if (isCertificateLoading) {
                    CircularProgressIndicator()
                } else {
                    certificates.takeIf { it.isNotEmpty() }?.forEach { certificate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(certificate.name, fontWeight = FontWeight.SemiBold)
                                Text("Tổ chức: ${certificate.issuingOrganization}")
                                Text("Mô tả: ${certificate.description}")
                                if (canManage) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            editingCertificate = certificate
                                            showCertificateDialog = true
                                        }) { Text("Sửa") }
                                        TextButton(onClick = { onDeleteCertificate(certificate) }) {
                                            Text("Xóa")
                                        }
                                    }
                                }
                            }
                        }
                    } ?: Text("Chưa có chứng chỉ")

                    if (canManage) {
                        TextButton(onClick = {
                            editingCertificate = null
                            showCertificateDialog = true
                        }) {
                            Text("Thêm chứng chỉ")
                        }
                    }
                }
            }
        }
    )

    if (showCertificateDialog) {
        CertificateDialog(
            studentId = student.id,
            initialCertificate = editingCertificate,
            onDismiss = { showCertificateDialog = false },
            onSubmit = { certificate ->
                if (editingCertificate == null) {
                    onCreateCertificate(certificate)
                } else {
                    onUpdateCertificate(certificate)
                }
                showCertificateDialog = false
            }
        )
    }
}

@Composable
private fun CertificateDialog(
    studentId: String,
    initialCertificate: Certificate?,
    onDismiss: () -> Unit,
    onSubmit: (Certificate) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var name by rememberSaveable { mutableStateOf(initialCertificate?.name.orEmpty()) }
    var organization by rememberSaveable { mutableStateOf(initialCertificate?.issuingOrganization.orEmpty()) }
    var issueDate by rememberSaveable {
        mutableStateOf(
            initialCertificate?.issueDate?.toDate()?.let { dateFormatter.format(it) }.orEmpty()
        )
    }
    var expiryDate by rememberSaveable {
        mutableStateOf(
            initialCertificate?.expiryDate?.toDate()?.let { dateFormatter.format(it) }.orEmpty()
        )
    }
    var description by rememberSaveable { mutableStateOf(initialCertificate?.description.orEmpty()) }
    

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        Certificate(
                            id = initialCertificate?.id.orEmpty(),
                            studentId = studentId,
                            name = name,
                            issuingOrganization = organization,
                            issueDate = issueDate.takeIf { it.isNotBlank() }?.let { dateString ->
                                val parsedDate: Date? = dateFormatter.parse(dateString)
                                Timestamp(parsedDate ?: Date())
                            } ?: Timestamp.now(),
                            expiryDate = expiryDate.takeIf { it.isNotBlank() }?.let { dateString ->
                                val parsedDate: Date? = dateFormatter.parse(dateString)
                                Timestamp(parsedDate ?: Date())
                            },
                            description = description
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (initialCertificate == null) "Thêm" else "Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text(if (initialCertificate == null) "Thêm chứng chỉ" else "Chỉnh sửa chứng chỉ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên chứng chỉ") })
                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text("Tổ chức cấp") }
                )
                OutlinedTextField(
                    value = issueDate,
                    onValueChange = { issueDate = it },
                    label = { Text("Ngày cấp (yyyy-MM-dd)") }
                )
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Ngày hết hạn (yyyy-MM-dd) - Tùy chọn") }
                )
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") })
            }
        }
    )
}

