package com.example.tdtustudentinformationmanagement.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.example.tdtustudentinformationmanagement.ui.viewmodel.DashboardState
import com.example.tdtustudentinformationmanagement.ui.viewmodel.StudentsUiState
import com.example.tdtustudentinformationmanagement.ui.viewmodel.UsersUiState

@Composable
fun DashboardScreen(
    user: User?,
    userRole: UserRole?,
    dashboardState: DashboardState,
    studentsState: StudentsUiState,
    usersState: UsersUiState,
    onRefresh: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Xin chào, ${user?.name ?: "Quản trị viên"}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = descriptiveSubtitle(userRole),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        SummaryRow(dashboardState = dashboardState)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(
                title = "Quản lý người dùng",
                description = "Xem, thêm, sửa và khóa tài khoản hệ thống",
                icon = Icons.Outlined.Groups,
                enabled = userRole == UserRole.ADMIN || userRole == UserRole.MANAGER || userRole == UserRole.EMPLOYEE,
                onClick = onNavigateToUsers,
                modifier = Modifier.fillMaxWidth()
            )

            QuickAccessCard(
                title = "Quản lý sinh viên",
                description = "Theo dõi hồ sơ, chứng chỉ và kết quả học tập",
                icon = Icons.Outlined.PersonSearch,
                enabled = true,
                onClick = onNavigateToStudents,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Theo dõi hoạt động",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Button(
                        onClick = onRefresh,
                        enabled = !dashboardState.isLoading
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Làm mới")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ActivityRow(
                    title = "Người dùng đang hoạt động",
                    value = usersState.users.count { it.status.name == "NORMAL" }.toString(),
                    highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )

                ActivityRow(
                    title = "Tài khoản bị khóa",
                    value = dashboardState.lockedUsers.toString(),
                    highlightColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )

                ActivityRow(
                    title = "Sinh viên đang theo học",
                    value = studentsState.students.count { it.status.name == "ACTIVE" }.toString(),
                    highlightColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    dashboardState: DashboardState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Tổng số người dùng",
            value = dashboardState.totalUsers.toString(),
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        SummaryCard(
            title = "Sinh viên trong hệ thống",
            value = dashboardState.totalStudents.toString(),
            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        )
        SummaryCard(
            title = "Sinh viên tốt nghiệp",
            value = dashboardState.graduatedStudents.toString(),
            backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        onClick = onClick,
        enabled = enabled
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityRow(
    title: String,
    value: String,
    highlightColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(highlightColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun descriptiveSubtitle(role: UserRole?): String {
    return when (role) {
        UserRole.ADMIN -> "Bạn có toàn quyền quản trị hệ thống"
        UserRole.MANAGER -> "Quản lý toàn bộ nghiệp vụ sinh viên"
        UserRole.EMPLOYEE -> "Chế độ xem dữ liệu, có thể cập nhật ảnh hồ sơ của bạn"
        else -> "Đang tải thông tin quyền hạn..."
    }
}

