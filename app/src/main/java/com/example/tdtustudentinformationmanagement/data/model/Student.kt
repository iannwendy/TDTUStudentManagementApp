package com.example.tdtustudentinformationmanagement.data.model

import com.google.firebase.Timestamp

data class Student(
    val id: String = "",
    val studentId: String = "",
    val name: String = "",
    val dateOfBirth: Timestamp = Timestamp.now(),
    val gender: Gender = Gender.MALE,
    val address: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val major: String = "",
    val yearOfStudy: Int = 1,
    val gpa: Double = 0.0,
    val status: StudentStatus = StudentStatus.ACTIVE,
    val profilePictureUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

enum class StudentStatus {
    ACTIVE,
    INACTIVE,
    GRADUATED,
    SUSPENDED
}

data class Certificate(
    val id: String = "",
    val studentId: String = "",
    val name: String = "",
    val issuingOrganization: String = "",
    val issueDate: Timestamp = Timestamp.now(),
    val expiryDate: Timestamp? = null,
    val certificateUrl: String = "",
    val description: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
