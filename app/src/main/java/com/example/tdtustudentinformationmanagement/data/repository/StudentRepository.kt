package com.example.tdtustudentinformationmanagement.data.repository

import com.example.tdtustudentinformationmanagement.data.firebase.FirebaseConfig
import com.example.tdtustudentinformationmanagement.data.model.Certificate
import com.example.tdtustudentinformationmanagement.data.model.Student
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) {
    
    suspend fun getAllStudents(): Result<List<Student>> {
        return try {
            val snapshot = firebaseConfig.firestore
                .collection(FirebaseConfig.STUDENTS_COLLECTION)
                .get()
                .await()
            
            val students = snapshot.documents.mapNotNull { document ->
                document.toObject(Student::class.java)?.copy(id = document.id)
            }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStudentById(studentId: String): Result<Student?> {
        return try {
            val document = firebaseConfig.firestore
                .collection(FirebaseConfig.STUDENTS_COLLECTION)
                .document(studentId)
                .get()
                .await()
            
            val student = document.toObject(Student::class.java)?.copy(id = document.id)
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createStudent(student: Student): Result<String> {
        return try {
            val docRef = firebaseConfig.firestore
                .collection(FirebaseConfig.STUDENTS_COLLECTION)
                .add(student)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateStudent(studentId: String, student: Student): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.STUDENTS_COLLECTION)
                .document(studentId)
                .set(student.copy(id = studentId, updatedAt = com.google.firebase.Timestamp.now()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.STUDENTS_COLLECTION)
                .document(studentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchStudents(
        name: String? = null,
        major: String? = null,
        yearOfStudy: Int? = null,
        status: String? = null
    ): Result<List<Student>> {
        return try {
            var query: Query = firebaseConfig.firestore.collection(FirebaseConfig.STUDENTS_COLLECTION)
            
            name?.let { query = query.whereGreaterThanOrEqualTo("name", it).whereLessThan("name", it + "\uf8ff") }
            major?.let { query = query.whereEqualTo("major", it) }
            yearOfStudy?.let { query = query.whereEqualTo("yearOfStudy", it) }
            status?.let { query = query.whereEqualTo("status", it) }
            
            val snapshot = query.get().await()
            
            val students = snapshot.documents.mapNotNull { document ->
                document.toObject(Student::class.java)?.copy(id = document.id)
            }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sortStudents(sortBy: String, ascending: Boolean = true): Result<List<Student>> {
        return try {
            val direction = if (ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING
            val snapshot = firebaseConfig.firestore
                .collection(FirebaseConfig.STUDENTS_COLLECTION)
                .orderBy(sortBy, direction)
                .get()
                .await()
            
            val students = snapshot.documents.mapNotNull { document ->
                document.toObject(Student::class.java)?.copy(id = document.id)
            }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Certificate management
    suspend fun getCertificatesByStudentId(studentId: String): Result<List<Certificate>> {
        return try {
            val snapshot = firebaseConfig.firestore
                .collection(FirebaseConfig.CERTIFICATES_COLLECTION)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            
            val certificates = snapshot.documents.mapNotNull { document ->
                document.toObject(Certificate::class.java)?.copy(id = document.id)
            }
            // Sort by issueDate descending on client side to avoid needing composite index
            val sortedCertificates = certificates.sortedByDescending { it.issueDate.seconds }
            Result.success(sortedCertificates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createCertificate(certificate: Certificate): Result<String> {
        return try {
            val docRef = firebaseConfig.firestore
                .collection(FirebaseConfig.CERTIFICATES_COLLECTION)
                .add(certificate)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCertificate(certificateId: String, certificate: Certificate): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.CERTIFICATES_COLLECTION)
                .document(certificateId)
                .set(certificate.copy(id = certificateId, updatedAt = com.google.firebase.Timestamp.now()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCertificate(certificateId: String): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.CERTIFICATES_COLLECTION)
                .document(certificateId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
