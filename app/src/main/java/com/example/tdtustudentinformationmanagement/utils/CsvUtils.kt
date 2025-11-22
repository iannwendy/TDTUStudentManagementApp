package com.example.tdtustudentinformationmanagement.utils

import com.example.tdtustudentinformationmanagement.data.model.Certificate
import com.example.tdtustudentinformationmanagement.data.model.Gender
import com.example.tdtustudentinformationmanagement.data.model.Student
import com.example.tdtustudentinformationmanagement.data.model.StudentStatus
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvUtils {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun studentsToCsv(students: List<Student>): String {
        val header = listOf(
            "studentId",
            "name",
            "dateOfBirth",
            "gender",
            "address",
            "phoneNumber",
            "email",
            "major",
            "yearOfStudy",
            "gpa",
            "status"
        )
        val rows = students.joinToString("\n") { student ->
            listOf(
                student.studentId,
                student.name,
                dateFormatter.format(student.dateOfBirth.toDate()),
                student.gender.name,
                student.address,
                student.phoneNumber,
                student.email,
                student.major,
                student.yearOfStudy.toString(),
                student.gpa.toString(),
                student.status.name
            ).joinToString(",") { escapeCsvValue(it) }
        }
        return header.joinToString(",") + "\n" + rows
    }

    fun parseStudentsCsv(csv: String): List<Student> {
        val lines = csv.trim().lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        return lines.drop(1).mapNotNull { line ->
            val columns = splitCsv(line)
            runCatching {
                Student(
                    studentId = columns.getOrNull(0).orEmpty(),
                    name = columns.getOrNull(1).orEmpty(),
                    dateOfBirth = parseDate(columns.getOrNull(2)),
                    gender = columns.getOrNull(3)?.let { value ->
                        runCatching { Gender.valueOf(value.uppercase(Locale.getDefault())) }.getOrDefault(Gender.OTHER)
                    } ?: Gender.OTHER,
                    address = columns.getOrNull(4).orEmpty(),
                    phoneNumber = columns.getOrNull(5).orEmpty(),
                    email = columns.getOrNull(6).orEmpty(),
                    major = columns.getOrNull(7).orEmpty(),
                    yearOfStudy = columns.getOrNull(8)?.toIntOrNull() ?: 1,
                    gpa = columns.getOrNull(9)?.toDoubleOrNull() ?: 0.0,
                    status = columns.getOrNull(10)?.let { value ->
                        runCatching { StudentStatus.valueOf(value.uppercase(Locale.getDefault())) }.getOrDefault(StudentStatus.ACTIVE)
                    } ?: StudentStatus.ACTIVE
                )
            }.getOrNull()
        }
    }

    fun certificatesToCsv(certificates: List<Certificate>): String {
        val header = listOf(
            "studentId",
            "name",
            "issuingOrganization",
            "issueDate",
            "expiryDate",
            "certificateUrl",
            "description"
        )
        val rows = certificates.joinToString("\n") { certificate ->
            listOf(
                certificate.studentId,
                certificate.name,
                certificate.issuingOrganization,
                dateFormatter.format(certificate.issueDate.toDate()),
                certificate.expiryDate?.toDate()?.let { dateFormatter.format(it) }.orEmpty(),
                certificate.certificateUrl,
                certificate.description
            ).joinToString(",") { escapeCsvValue(it) }
        }
        return header.joinToString(",") + "\n" + rows
    }

    fun parseCertificatesCsv(studentId: String, csv: String): List<Certificate> {
        val lines = csv.trim().lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        return lines.drop(1).mapNotNull { line ->
            val columns = splitCsv(line)
            runCatching {
                Certificate(
                    studentId = studentId,
                    name = columns.getOrNull(1).orEmpty(),
                    issuingOrganization = columns.getOrNull(2).orEmpty(),
                    issueDate = parseDate(columns.getOrNull(3)),
                    expiryDate = columns.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { parseDate(it) },
                    certificateUrl = columns.getOrNull(5).orEmpty(),
                    description = columns.getOrNull(6).orEmpty()
                )
            }.getOrNull()
        }
    }

    private fun parseDate(value: String?): Timestamp {
        return if (value.isNullOrBlank()) {
            Timestamp.now()
        } else {
            val parsed: Date = dateFormatter.parse(value.trim()) ?: Date()
            Timestamp(parsed)
        }
    }

    private fun escapeCsvValue(value: String): String {
        return if (value.contains(",") || value.contains("\"")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun splitCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        val builder = StringBuilder()
        var inQuotes = false
        line.forEach { char ->
            when {
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(builder.toString().trim())
                    builder.clear()
                }
                else -> builder.append(char)
            }
        }
        result.add(builder.toString().trim())
        return result
    }
}

