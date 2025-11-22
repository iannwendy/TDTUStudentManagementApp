package com.example.tdtustudentinformationmanagement.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val phoneNumber: String = "",
    val status: UserStatus = UserStatus.NORMAL,
    val role: UserRole = UserRole.EMPLOYEE,
    val profilePictureUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class UserStatus {
    NORMAL,
    LOCKED
}

enum class UserRole {
    ADMIN,
    MANAGER,
    EMPLOYEE
}

data class LoginHistory(
    val id: String = "",
    val userId: String = "",
    val loginTime: Timestamp = Timestamp.now(),
    val deviceInfo: String = "",
    val ipAddress: String = ""
)
