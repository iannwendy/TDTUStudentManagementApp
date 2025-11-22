package com.example.tdtustudentinformationmanagement.ui.screens.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.ui.viewmodel.ProfilePictureState

@Composable
fun ProfileScreen(
    user: User?,
    profilePictureState: ProfilePictureState,
    onChangePhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Hồ sơ cá nhân", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Cập nhật thông tin và ảnh đại diện của bạn",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user?.profilePictureUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = "Ảnh đại diện",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        Text(text = initial, style = MaterialTheme.typography.headlineLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onChangePhoto,
                    enabled = !profilePictureState.isUploading
                ) {
                    Text(if (profilePictureState.isUploading) "Đang tải ảnh..." else "Đổi ảnh đại diện")
                }

                profilePictureState.errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))
                user?.let {
                    InfoRow(label = "Họ tên", value = it.name)
                    InfoRow(label = "Email", value = it.email)
                    InfoRow(label = "Điện thoại", value = it.phoneNumber)
                    InfoRow(label = "Quyền hạn", value = it.role.name)
                    InfoRow(label = "Trạng thái", value = it.status.name)
                }
            }
        }

    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}

