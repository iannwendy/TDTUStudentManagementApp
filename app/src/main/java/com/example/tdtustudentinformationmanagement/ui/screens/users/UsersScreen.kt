package com.example.tdtustudentinformationmanagement.ui.screens.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tdtustudentinformationmanagement.data.model.LoginHistory
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.example.tdtustudentinformationmanagement.data.model.UserStatus
import com.example.tdtustudentinformationmanagement.ui.viewmodel.UsersUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UsersScreen(
    currentUser: User?,
    uiState: UsersUiState,
    canManageUsers: Boolean,
    canViewLoginHistory: Boolean,
    onAddUser: (String, String, String, User) -> Unit,
    onUpdateUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    onUpdateStatus: (User, UserStatus) -> Unit,
    onUpdateRole: (User, UserRole) -> Unit,
    onRefresh: () -> Unit,
    onShowHistory: (User) -> Unit,
    onHideHistory: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var roleFilter by rememberSaveable { mutableStateOf<UserRole?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<UserStatus?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<User?>(null) }

    val filteredUsers = remember(uiState.users, searchQuery, roleFilter, statusFilter) {
        uiState.users.filter { user ->
            val queryMatch = user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.phoneNumber.contains(searchQuery, ignoreCase = true)
            val roleMatch = roleFilter?.let { user.role == it } ?: true
            val statusMatch = statusFilter?.let { user.status == it } ?: true
            queryMatch && roleMatch && statusMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Quản lý người dùng", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (canManageUsers) "Thêm, chỉnh sửa và phân quyền tài khoản"
                    else "Chế độ xem thông tin người dùng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canManageUsers) {
                FilledTonalButton(
                    onClick = {
                        editingUser = null
                        isDialogOpen = true
                    }
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm mới")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Tìm kiếm") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleFilterDropdown(selected = roleFilter, onSelected = { roleFilter = it })
            StatusFilterDropdown(selected = statusFilter, onSelected = { statusFilter = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRefresh) {
            Text("Làm mới danh sách")
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        canManage = canManageUsers,
                        canViewHistory = canViewLoginHistory,
                        onEdit = {
                            editingUser = user
                            isDialogOpen = true
                        },
                        onDelete = { showDeleteConfirm = user },
                        onToggleStatus = {
                            val newStatus = if (user.status == UserStatus.NORMAL) UserStatus.LOCKED else UserStatus.NORMAL
                            onUpdateStatus(user, newStatus)
                        },
                        onRoleChange = { onUpdateRole(user, it) },
                        onShowHistory = { onShowHistory(user) }
                    )
                }
            }
        }
    }

    if (isDialogOpen) {
        UserFormDialog(
            initialUser = editingUser,
            onDismiss = {
                isDialogOpen = false
            },
            onSubmit = { adminPassword, name, email, age, phone, role, status, password ->
                val user = User(
                    id = editingUser?.id.orEmpty(),
                    name = name,
                    email = email,
                    age = age,
                    phoneNumber = phone,
                    role = role,
                    status = status
                )
                if (editingUser == null) {
                    if (!adminPassword.isNullOrBlank() && currentUser?.email?.isNotBlank() == true) {
                        onAddUser(adminPassword, email, password.orEmpty(), user)
                    }
                } else {
                    onUpdateUser(user)
                }
                isDialogOpen = false
            }
        )
    }

    showDeleteConfirm?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Xóa người dùng") },
            text = { Text("Bạn có chắc muốn xóa ${user.name}? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteUser(user)
                    showDeleteConfirm = null
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    uiState.historyUser?.let { historyUser ->
        LoginHistoryDialog(
            user = historyUser,
            loginHistory = uiState.loginHistory,
            isLoading = uiState.isHistoryLoading,
            onDismiss = onHideHistory
        )
    }
}

@Composable
private fun RoleFilterDropdown(
    selected: UserRole?,
    onSelected: (UserRole?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected?.name ?: "Quyền hạn")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Tất cả") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.name) },
                    onClick = {
                        onSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusFilterDropdown(
    selected: UserStatus?,
    onSelected: (UserStatus?) -> Unit
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
            UserStatus.entries.forEach { status ->
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
private fun UserCard(
    user: User,
    canManage: Boolean,
    canViewHistory: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onShowHistory: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.name, style = MaterialTheme.typography.titleMedium)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
            Text("Điện thoại: ${user.phoneNumber}")
            Text("Tuổi: ${user.age}")
            Text("Trạng thái: ${user.status.name}")
            Text("Quyền hạn: ${user.role.name}")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (canViewHistory) {
                    TextButton(onClick = onShowHistory) {
                        Icon(Icons.Outlined.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lịch sử đăng nhập")
                    }
                }
                if (canManage) {
                    Row {
                        TextButton(onClick = onEdit) {
                            Text("Chỉnh sửa")
                        }
                        TextButton(onClick = onToggleStatus) {
                            Text(if (user.status == UserStatus.NORMAL) "Khóa" else "Mở khóa")
                        }
                        TextButton(onClick = onDelete) {
                            Text("Xóa")
                        }
                    }
                }
            }
            if (canManage) {
                RoleSelector(currentRole = user.role, onRoleChange = onRoleChange)
            }
        }
    }
}

@Composable
private fun RoleSelector(
    currentRole: UserRole,
    onRoleChange: (UserRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Thay đổi quyền:", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = { expanded = true }) {
            Text(currentRole.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.name) },
                    onClick = {
                        onRoleChange(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UserFormDialog(
    initialUser: User?,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Int, String, UserRole, UserStatus, String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialUser?.name.orEmpty()) }
    var email by rememberSaveable { mutableStateOf(initialUser?.email.orEmpty()) }
    var age by rememberSaveable { mutableStateOf(initialUser?.age?.toString().orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(initialUser?.phoneNumber.orEmpty()) }
    var role by rememberSaveable { mutableStateOf(initialUser?.role ?: UserRole.EMPLOYEE) }
    var status by rememberSaveable { mutableStateOf(initialUser?.status ?: UserStatus.NORMAL) }
    var password by rememberSaveable { mutableStateOf("") }
    var adminPassword by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        adminPassword,
                        name,
                        email,
                        age.toIntOrNull() ?: 0,
                        phone,
                        role,
                        status,
                        password.takeIf { initialUser == null }
                    )
                },
                enabled = name.isNotBlank() && email.isNotBlank() &&
                    (initialUser != null || (password.length >= 6 && adminPassword.isNotBlank()))
            ) {
                Text(if (initialUser == null) "Thêm" else "Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text(if (initialUser == null) "Thêm người dùng" else "Chỉnh sửa người dùng") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Họ tên") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Tuổi") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") })
                RoleSelector(currentRole = role, onRoleChange = { role = it })
                StatusSelector(current = status, onStatusChange = { status = it })
                if (initialUser == null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu (≥6 ký tự)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Nhập lại mật khẩu admin để xác thực") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            }
        }
    )
}

@Composable
private fun StatusSelector(
    current: UserStatus,
    onStatusChange: (UserStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Trạng thái:", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = { expanded = true }) {
            Text(current.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginHistoryDialog(
    user: User,
    loginHistory: List<LoginHistory>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = { Text("Lịch sử đăng nhập - ${user.name}") },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column {
                    loginHistory.takeIf { it.isNotEmpty() }?.forEach { history ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Thời gian: ${formatter.format(history.loginTime.toDate())}")
                                Text("Thiết bị: ${history.deviceInfo}")
                                Text("IP: ${history.ipAddress}")
                            }
                        }
                    } ?: Text("Chưa có dữ liệu lịch sử đăng nhập")
                }
            }
        }
    )
}

