package com.example.tdtustudentinformationmanagement.data.repository

import com.example.tdtustudentinformationmanagement.data.firebase.FirebaseConfig
import com.example.tdtustudentinformationmanagement.data.model.LoginHistory
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.example.tdtustudentinformationmanagement.data.model.UserStatus
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseConfig: FirebaseConfig
) {
    
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)?.copy(id = document.id)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val document = firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val user = document.toObject(User::class.java)?.copy(id = document.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUser(user: User): Result<String> {
        return try {
            val collection = firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
            val docRef = if (user.id.isNotBlank()) {
                collection.document(user.id).set(user).await()
                user.id
            } else {
                val ref = collection.add(user).await()
                ref.id
            }
            Result.success(docRef)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: String, user: User): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .set(user.copy(id = userId, updatedAt = com.google.firebase.Timestamp.now()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserStatus(userId: String, status: UserStatus): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .update("status", status.name, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .update("role", role.name, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfilePicture(userId: String, imageUrl: String): Result<Unit> {
        return try {
            firebaseConfig.firestore
                .collection(FirebaseConfig.USERS_COLLECTION)
                .document(userId)
                .update("profilePictureUrl", imageUrl, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLoginHistory(userId: String): Result<List<LoginHistory>> {
        return try {
            val snapshot = firebaseConfig.firestore
                .collection(FirebaseConfig.LOGIN_HISTORY_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val loginHistory = snapshot.documents.mapNotNull { document ->
                document.toObject(LoginHistory::class.java)?.copy(id = document.id)
            }
            // Sort by loginTime descending on client side to avoid needing composite index
            val sortedHistory = loginHistory.sortedByDescending { it.loginTime.seconds }
            Result.success(sortedHistory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addLoginHistory(loginHistory: LoginHistory): Result<String> {
        return try {
            val docRef = firebaseConfig.firestore
                .collection(FirebaseConfig.LOGIN_HISTORY_COLLECTION)
                .add(loginHistory)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
