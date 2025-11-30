package com.example.tdtustudentinformationmanagement.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Theo dõi hoạt động",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !dashboardState.isLoading
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Làm mới")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ActivityRow(
                    title = "Người dùng đang hoạt động",
                    value = usersState.users.count { it.status.name == "NORMAL" }.toString(),
                    icon = Icons.Outlined.People,
                    highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    iconTintColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActivityRow(
                    title = "Tài khoản bị khóa",
                    value = dashboardState.lockedUsers.toString(),
                    icon = Icons.Outlined.Lock,
                    highlightColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    iconTintColor = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActivityRow(
                    title = "Sinh viên đang theo học",
                    value = studentsState.students.count { it.status.name == "ACTIVE" }.toString(),
                    icon = Icons.Outlined.School,
                    highlightColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    iconTintColor = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    dashboardState: DashboardState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Tổng số người dùng",
            value = dashboardState.totalUsers.toString(),
            icon = Icons.Outlined.People,
            iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            iconTintColor = MaterialTheme.colorScheme.primary,
            backgroundColor = Color.White,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Sinh viên trong hệ thống",
            value = dashboardState.totalStudents.toString(),
            icon = Icons.Outlined.School,
            iconBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            iconTintColor = MaterialTheme.colorScheme.secondary,
            backgroundColor = Color.White,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Sinh viên tốt nghiệp",
            value = dashboardState.graduatedStudents.toString(),
            icon = Icons.Outlined.TrendingUp,
            iconBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            iconTintColor = MaterialTheme.colorScheme.tertiary,
            backgroundColor = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
            containerColor = Color.White
        ),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(
    title: String,
    value: String,
    icon: ImageVector,
    highlightColor: Color,
    iconTintColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTintColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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

