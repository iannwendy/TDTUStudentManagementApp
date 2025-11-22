package com.example.tdtustudentinformationmanagement.data.repository

import com.example.tdtustudentinformationmanagement.data.firebase.FirebaseConfig
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) {

    suspend fun uploadProfilePicture(
        userId: String,
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> {
        return try {
            val reference = firebaseConfig.storage.reference
                .child("profile_pictures/$userId.jpg")
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()
            reference.putBytes(bytes, metadata).await()
            val downloadUrl = reference.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadCertificateFile(
        studentId: String,
        certificateId: String,
        bytes: ByteArray,
        mimeType: String = "application/pdf"
    ): Result<String> {
        return try {
            val reference = firebaseConfig.storage.reference
                .child("certificates/$studentId/$certificateId.pdf")
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .build()
            reference.putBytes(bytes, metadata).await()
            val downloadUrl = reference.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

